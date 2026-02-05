package no.nav.klage.lookup.service.access

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse.*
import no.nav.klage.lookup.api.access.AccessRequest.Sak
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ACCESS_TO_PERSON
import no.nav.klage.lookup.config.fpsak.FpsakService
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenErrorResponse
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import tools.jackson.module.kotlin.jacksonObjectMapper

@Service
class AccessToPersonService(
    private val tilgangsmaskinenService: TilgangsmaskinenService,
    private val fpsakService: FpsakService,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val TILGANGSMASKINEN_TIMER = "tilgangsmaskinen.response.time"
        private const val FPSAK_TIMER = "fpsak.response.time"
    }

    @Cacheable(ACCESS_TO_PERSON)
    @Retryable
    fun getNavIdentAccessToUser(
        brukerId: String,
        navIdent: String,
        sak: Sak?,
    ): Access {
        val usersToCheck = if (sak != null && sak.ytelse in listOf(FOR_FOR, FOR_ENG, FOR_SVA) && sak.fagsystem == Fagsystem.FS36) {
            val aktoerIds = timedCall(FPSAK_TIMER, "getAktoerForSak") {
                fpsakService.getAktoerForSak(
                    bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithFpsakScope()}",
                    saksnummer = sak.sakId,
                )
            }
            if (aktoerIds.isEmpty()) {
                logger.error("No aktør IDs found for sak ${sak.sakId}. Will use brukerId as a fallback.")
                listOf(brukerId)
            } else {
                logger.debug("Found ${aktoerIds.size} aktør IDs for sak ${sak.sakId}")
                aktoerIds
            }
        } else {
            listOf(brukerId)
        }

        val deniedReasons = mutableSetOf<String>()

        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithTilgangsmaskinenScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithTilgangsmaskinenScope()}"
        }

        for (userToCheck in usersToCheck) {
            try {
                if (useObo) {
                    timedCall(TILGANGSMASKINEN_TIMER, "validateAccessWithObo") {
                        tilgangsmaskinenService.validateAccessWithObo(
                            oboBearerToken = bearerToken,
                            brukerId = userToCheck,
                        )
                    }
                } else {
                    timedCall(TILGANGSMASKINEN_TIMER, "validateAccess") {
                        tilgangsmaskinenService.validateAccess(
                            clientBearerToken = bearerToken,
                            brukerId = userToCheck,
                            navIdent = navIdent,
                        )
                    }
                }
            } catch (ex: RestClientResponseException) {
                if (ex.statusCode == HttpStatus.FORBIDDEN) {
                    val reason = try {
                        val errorResponse = jacksonObjectMapper().readValue(
                            ex.responseBodyAsString,
                            TilgangsmaskinenErrorResponse::class.java
                        )
                        errorResponse.begrunnelse
                    } catch (parseEx: Exception) {
                        logger.warn("Could not parse Tilgangsmaskinen error. See team-logs for details.")
                        teamLogger.warn("Could not parse Tilgangsmaskinen error.", parseEx)
                        "Kunne ikke verifisere tilgang - kontakt Team Klage."
                    }
                    deniedReasons.add(reason)
                } else {
                    logger.error("Unexpected error while calling Tilgangsmaskinen: ${ex.statusCode}")
                    teamLogger.error("Unexpected error while calling Tilgangsmaskinen.", ex)
                    throw ex
                }
            }
        }

        return if (deniedReasons.isEmpty()) {
            Access(
                access = true,
                reason = "Access granted",
            )
        } else {
            Access(
                access = false,
                reason = deniedReasons.joinToString("; ")
            )
        }
    }

    private fun <T> timedCall(timerName: String, method: String, block: () -> T): T {
        return Timer.builder(timerName)
            .tag("method", method)
            .register(meterRegistry)
            .recordCallable(block)!!
    }
}