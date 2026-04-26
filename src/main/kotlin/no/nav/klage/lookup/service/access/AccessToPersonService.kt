package no.nav.klage.lookup.service.access

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ACCESS_TO_PERSON
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenErrorResponse
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import tools.jackson.module.kotlin.jacksonObjectMapper

@Service
class AccessToPersonService(
    private val tilgangsmaskinenService: TilgangsmaskinenService,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val TILGANGSMASKINEN_TIMER = "tilgangsmaskinen.response.time"
    }

    @Cacheable(ACCESS_TO_PERSON)
    @Retryable
    fun getNavIdentAccessToUser(
        brukerId: String,
        navIdent: String,
    ): Access {
        val deniedReasons = mutableSetOf<String>()

        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithTilgangsmaskinenScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithTilgangsmaskinenScope()}"
        }

        try {
            if (useObo) {
                meterRegistry.timedCall(TILGANGSMASKINEN_TIMER, "validateAccessWithObo") {
                    tilgangsmaskinenService.validateAccessWithObo(
                        oboBearerToken = bearerToken,
                        brukerId = brukerId,
                    )
                }
            } else {
                meterRegistry.timedCall(TILGANGSMASKINEN_TIMER, "validateAccess") {
                    tilgangsmaskinenService.validateAccess(
                        clientBearerToken = bearerToken,
                        brukerId = brukerId,
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
}