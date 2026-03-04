package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ANSATTE_IN_ENHET
import no.nav.klage.lookup.config.EnhetNotFoundException
import no.nav.klage.lookup.config.microsoftgraph.MicrosoftGraphInterface
import no.nav.klage.lookup.config.microsoftgraph.MicrosoftGraphUserList
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class MicrosoftGraphService(
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
    private val microsoftGraphInterface: MicrosoftGraphInterface,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val MICROSOFT_GRAPH_TIMER = "microsoftgraph.response.time"
        private const val userSelect = "userPrincipalName,onPremisesSamAccountName,displayName,givenName,surname"
    }

    @Cacheable(ANSATTE_IN_ENHET)
    @Retryable(
        excludes = [EnhetNotFoundException::class]
    )
    fun getAnsatteInEnhet(enhetsnummer: String): MicrosoftGraphUserList {
        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithEntraProxyScope()}"
        }

        val ansattList = try {
            timedCall(MICROSOFT_GRAPH_TIMER, "ansatteInEnhet") {
                microsoftGraphInterface.microsoftGraphQuery(
                    bearerToken = bearerToken,
                    consistencyLevel = "eventual",
                    filter = "streetAddress eq '$enhetsnummer'",
                    select = userSelect,
                    count = true,
                    top = 500
                )
            }

        } catch (e: HttpClientErrorException) {
            logger.debug("Failed to retrieve ansatte in enhet '$enhetsnummer'", e)
            throw EnhetNotFoundException("Ansatte in enhet '$enhetsnummer' could not be found")
        } catch (e: Exception) {
            logger.error("Unexpected error when retrieving ansatte in enhet '$enhetsnummer'", e)
            throw e
        }

        return ansattList
    }

    private fun <T> timedCall(timerName: String, method: String, block: () -> T): T {
        return Timer.builder(timerName)
            .tag("method", method)
            .register(meterRegistry)
            .recordCallable(block)
    }
}