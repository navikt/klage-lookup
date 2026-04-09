package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ANSATTE_IN_ENHET
import no.nav.klage.lookup.config.EnhetNotFoundException
import no.nav.klage.lookup.config.microsoftgraph.MicrosoftGraphClient
import no.nav.klage.lookup.config.microsoftgraph.MicrosoftGraphUserList
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class MicrosoftGraphService(
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
    private val microsoftGraphClient: MicrosoftGraphClient,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val MICROSOFT_GRAPH_TIMER = "microsoftgraph.response.time"
        private const val USER_SELECT = "userPrincipalName,onPremisesSamAccountName,displayName,givenName,surname"
    }

    @Cacheable(ANSATTE_IN_ENHET)
    @Retryable(
        excludes = [EnhetNotFoundException::class]
    )
    fun getAnsatteInEnhet(enhetsnummer: String): MicrosoftGraphUserList {

        val bearerToken = "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithMicrosoftGraphScope()}"

        val ansattList = try {
            meterRegistry.timedCall(MICROSOFT_GRAPH_TIMER, "ansatteInEnhet") {
                microsoftGraphClient.microsoftGraphQuery(
                    bearerToken = bearerToken,
                    consistencyLevel = "eventual",
                    filter = "streetAddress eq '$enhetsnummer'",
                    select = USER_SELECT,
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
}