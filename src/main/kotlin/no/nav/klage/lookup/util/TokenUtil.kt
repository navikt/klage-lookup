package no.nav.klage.lookup.util

import no.nav.klage.auth.TexasClient
import no.nav.klage.auth.currentAzureToken
import no.nav.klage.auth.currentTokenXToken
import no.nav.klage.auth.groups
import no.nav.klage.auth.navIdent
import no.nav.klage.auth.pid
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TokenUtil(
    private val texasClient: TexasClient,
    @Value($$"api://${TILGANGSMASKINEN_SCOPE}/.default")
    private val tilgangsmaskinenScope: String,
    @Value($$"api://${FPSAK_SCOPE}/.default")
    private val fpsakScope: String,
    @Value($$"api://${ENTRA_PROXY_SCOPE}/.default")
    private val entraProxyScope: String,
    @Value($$"api://${PDL_SCOPE}/.default")
    private val pdlScope: String,
    @Value($$"api://${SKJERMING_PIP_SCOPE}/.default")
    private val skjermingPipScope: String,
    @Value($$"api://${KABAL_API_SCOPE}/.default")
    private val kabalApiScope: String,
    @Value($$"api://${NOM_SCOPE}/.default")
    private val nomScope: String,
    @Value($$"api://${REGOPPSLAG_SCOPE}/.default")
    private val regoppslagScope: String,
    @Value($$"api://${REPR_API_SCOPE}/.default")
    private val reprApiScope: String,
    @Value($$"${REPR_API_AUDIENCE}")
    private val reprApiAudience: String,
) {
    companion object {
        private const val MICROSOFT_GRAPH_SCOPE = "https://graph.microsoft.com/.default"
    }

    /** The fødselsnummer of the logged in citizen. Only on the request thread of a TokenX request. */
    fun getSubjectFromTokenXToken(): String =
        currentTokenXToken()?.pid ?: throw IllegalStateException("No logged in citizen in this request.")

    fun getSaksbehandlerAccessTokenWithTilgangsmaskinenScope(): String = texasClient.onBehalfOfCurrentUser(tilgangsmaskinenScope)

    fun getAppAccessTokenWithTilgangsmaskinenScope(): String = texasClient.machineToMachineToken(tilgangsmaskinenScope)

    fun getAppAccessTokenWithFpsakScope(): String = texasClient.machineToMachineToken(fpsakScope)

    fun getSaksbehandlerAccessTokenWithEntraProxyScope(): String = texasClient.onBehalfOfCurrentUser(entraProxyScope)

    fun getAppAccessTokenWithEntraProxyScope(): String = texasClient.machineToMachineToken(entraProxyScope)

    fun getSaksbehandlerAccessTokenWithMicrosoftGraphScope(): String = texasClient.onBehalfOfCurrentUser(MICROSOFT_GRAPH_SCOPE)

    fun getAppAccessTokenWithMicrosoftGraphScope(): String = texasClient.machineToMachineToken(MICROSOFT_GRAPH_SCOPE)

    fun getSaksbehandlerAccessTokenWithPdlScope(): String = texasClient.onBehalfOfCurrentUser(pdlScope)

    fun getAppAccessTokenWithPdlScope(): String = texasClient.machineToMachineToken(pdlScope)

    fun getAppAccessTokenWithSkjermingPipScope(): String = texasClient.machineToMachineToken(skjermingPipScope)

    fun getAppAccessTokenWithKabalApiScope(): String = texasClient.machineToMachineToken(kabalApiScope)

    fun getAppAccessTokenWithNomScope(): String = texasClient.machineToMachineToken(nomScope)

    fun getOnBehalfOfTokenWithRegoppslagScope(): String = texasClient.onBehalfOfCurrentUser(regoppslagScope)

    fun getAppAccessTokenWithRegoppslagScope(): String = texasClient.machineToMachineToken(regoppslagScope)

    fun getOnBehalfOfFromTokenXTokenWithReprApiScope(): String = texasClient.tokenXExchangeForCurrentUser(reprApiAudience)

    fun getOnBehalfOfTokenWithReprApiScope(): String = texasClient.onBehalfOfCurrentUser(reprApiScope)

    fun getAppAccessTokenWithReprApiScope(): String = texasClient.machineToMachineToken(reprApiScope)

    /** NAVident of the logged in Nav employee, or null for machine-to-machine and TokenX requests. */
    fun getIdent(): String? = currentAzureToken()?.navIdent

    fun getGroups(): List<String> = currentAzureToken()?.groups.orEmpty()
}
