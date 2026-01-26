package no.nav.klage.lookup.service.access

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ACCESS_TO_PERSON
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
        return try {
            if (tokenUtil.getIdent() == null) {
                timedCall("validateAccess") {
                    tilgangsmaskinenService.validateAccess(
                        clientBearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithTilgangsmaskinenScope()}",
                        brukerId = brukerId,
                        navIdent = navIdent,
                    )
                }
            } else {
                timedCall("validateAccessWithObo") {
                    tilgangsmaskinenService.validateAccessWithObo(
                        oboBearerToken = "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithTilgangsmaskinenScope()}",
                        brukerId = brukerId,
                    )
                }
            }
            Access(
                access = true,
                reason = "Access granted",
            )
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
                Access(
                    access = false,
                    reason = reason
                )
            } else {
                logger.error("Unexpected error while calling Tilgangsmaskinen: ${ex.statusCode}")
                teamLogger.error("Unexpected error while calling Tilgangsmaskinen.", ex)
                throw ex
            }
        }
    }

    private fun <T> timedCall(method: String, block: () -> T): T {
        return Timer.builder(TILGANGSMASKINEN_TIMER)
            .tag("method", method)
            .register(meterRegistry)
            .recordCallable(block)!!
    }
}