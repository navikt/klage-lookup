package no.nav.klage.lookup.util

import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TokenUtil(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getSaksbehandlerAccessTokenWithTilgangsmaskinenScope(): String {
        val clientProperties = clientConfigurationProperties.registration["tilgangsmaskinen-onbehalfof"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getAppAccessTokenWithTilgangsmaskinenScope(): String {
        val clientProperties = clientConfigurationProperties.registration["tilgangsmaskinen-maskintilmaskin"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getAppAccessTokenWithFpsakScope(): String {
        val clientProperties = clientConfigurationProperties.registration["fpsak-maskintilmaskin"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getSaksbehandlerAccessTokenWithEntraProxyScope(): String {
        val clientProperties = clientConfigurationProperties.registration["entraproxy-onbehalfof"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getAppAccessTokenWithEntraProxyScope(): String {
        val clientProperties = clientConfigurationProperties.registration["entraproxy-maskintilmaskin"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getIdent(): String? =
        tokenValidationContextHolder.getTokenValidationContext().getJwtToken(SecurityConfiguration.ISSUER_AAD)
            ?.jwtTokenClaims?.get("NAVident")?.toString()

    fun getGroups(): Array<String>? =
        tokenValidationContextHolder.getTokenValidationContext().getJwtToken(SecurityConfiguration.ISSUER_AAD)
            ?.jwtTokenClaims?.get("groups")?.let {
                if (it is Array<*>) {
                    @Suppress("UNCHECKED_CAST")
                    it as Array<String>
                } else {
                    logger.warn("Groups claim is not an array, cannot parse groups from token")
                    null
                }
            }
}