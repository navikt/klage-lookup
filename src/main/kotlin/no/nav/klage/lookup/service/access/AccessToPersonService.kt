package no.nav.klage.lookup.service.access

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ACCESS_TO_PERSON
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenErrorResponse
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
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
    private val environment: Environment,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val TILGANGSMASKINEN_TIMER = "tilgangsmaskinen.response.time"
    }

    @Cacheable(ACCESS_TO_PERSON)
    @Retryable
    fun getNavIdentAccessToUser(
        brukerId: String,
        navIdent: String,
    ): Access {
        return try {
            if (tokenUtil.getIdent() != null) {
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
                if (environment.activeProfiles.contains("dev")) {
                    logger.debug("response body from Tilgangsmaskinen when forbidden: ${ex.responseBodyAsString}")
                }
                val reason = try {
                    val errorResponse = jacksonObjectMapper().readValue(
                        ex.responseBodyAsString,
                        TilgangsmaskinenErrorResponse::class.java
                    )
                    errorResponse.begrunnelse
                } catch (parseEx: Exception) {
                    logger.warn("Could not parse Tilgangsmaskinen error")
                    "Access denied"
                }
                Access(
                    access = false,
                    reason = reason
                )
            } else {
                logger.error("Error while calling Tilgangsmaskinen: ${ex.statusCode} - ${ex.responseBodyAsString}")
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