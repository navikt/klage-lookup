package no.nav.klage.lookup.util

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.auth.TexasClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TokenUtilTest {
    private val texasClient = mockk<TexasClient>()
    private val tokenUtil =
        TokenUtil(
            texasClient = texasClient,
            tilgangsmaskinenScope = "tilgangsmaskinen",
            fpsakScope = "fpsak",
            entraProxyScope = "entraproxy",
            pdlScope = "pdl",
            skjermingPipScope = "skjerming",
            kabalApiScope = "kabal-api",
            nomScope = "nom",
            regoppslagScope = "regoppslag",
            reprApiScope = "repr-api",
            reprApiAudience = "dev-gcp:repr:repr-api",
        )

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `reads NAVident and groups from the current Azure token`() {
        authenticate(claims = mapOf("NAVident" to "Z123456", "groups" to listOf("a", "b")))

        assertThat(tokenUtil.getIdent()).isEqualTo("Z123456")
        assertThat(tokenUtil.getGroups()).containsExactly("a", "b")
    }

    @Test
    fun `machine-to-machine and TokenX tokens have no ident or groups`() {
        authenticate(claims = mapOf("idtyp" to "app"))
        assertThat(tokenUtil.getIdent()).isNull()
        assertThat(tokenUtil.getGroups()).isEmpty()

        authenticate(claims = mapOf("pid" to "12345678910"))
        assertThat(tokenUtil.getIdent()).isNull()
        assertThat(tokenUtil.getGroups()).isEmpty()
    }

    @Test
    fun `reads pid from the current TokenX token`() {
        authenticate(claims = mapOf("pid" to "12345678910"))

        assertThat(tokenUtil.getSubjectFromTokenXToken()).isEqualTo("12345678910")
    }

    @Test
    fun `fails when there is no TokenX token`() {
        assertThrows<IllegalStateException> { tokenUtil.getSubjectFromTokenXToken() }

        authenticate(claims = mapOf("NAVident" to "Z123456"))
        assertThrows<IllegalStateException> { tokenUtil.getSubjectFromTokenXToken() }
    }

    @Test
    fun `asks Texas for tokens with the right targets`() {
        every { texasClient.onBehalfOfCurrentUser(target = any()) } answers { "obo:${firstArg<String>()}" }
        every { texasClient.machineToMachineToken(target = any()) } answers { "m2m:${firstArg<String>()}" }
        every { texasClient.tokenXExchangeForCurrentUser(target = any()) } answers { "tokenx:${firstArg<String>()}" }

        assertThat(tokenUtil.getSaksbehandlerAccessTokenWithTilgangsmaskinenScope()).isEqualTo("obo:tilgangsmaskinen")
        assertThat(tokenUtil.getAppAccessTokenWithTilgangsmaskinenScope()).isEqualTo("m2m:tilgangsmaskinen")
        assertThat(tokenUtil.getAppAccessTokenWithFpsakScope()).isEqualTo("m2m:fpsak")
        assertThat(tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope()).isEqualTo("obo:entraproxy")
        assertThat(tokenUtil.getAppAccessTokenWithEntraProxyScope()).isEqualTo("m2m:entraproxy")
        assertThat(tokenUtil.getSaksbehandlerAccessTokenWithMicrosoftGraphScope()).isEqualTo("obo:https://graph.microsoft.com/.default")
        assertThat(tokenUtil.getAppAccessTokenWithMicrosoftGraphScope()).isEqualTo("m2m:https://graph.microsoft.com/.default")
        assertThat(tokenUtil.getSaksbehandlerAccessTokenWithPdlScope()).isEqualTo("obo:pdl")
        assertThat(tokenUtil.getAppAccessTokenWithPdlScope()).isEqualTo("m2m:pdl")
        assertThat(tokenUtil.getAppAccessTokenWithSkjermingPipScope()).isEqualTo("m2m:skjerming")
        assertThat(tokenUtil.getAppAccessTokenWithKabalApiScope()).isEqualTo("m2m:kabal-api")
        assertThat(tokenUtil.getAppAccessTokenWithNomScope()).isEqualTo("m2m:nom")
        assertThat(tokenUtil.getOnBehalfOfTokenWithRegoppslagScope()).isEqualTo("obo:regoppslag")
        assertThat(tokenUtil.getAppAccessTokenWithRegoppslagScope()).isEqualTo("m2m:regoppslag")
        assertThat(tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope()).isEqualTo("tokenx:dev-gcp:repr:repr-api")
        assertThat(tokenUtil.getOnBehalfOfTokenWithReprApiScope()).isEqualTo("obo:repr-api")
        assertThat(tokenUtil.getAppAccessTokenWithReprApiScope()).isEqualTo("m2m:repr-api")
    }

    private fun authenticate(claims: Map<String, Any>) {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "subject")
                .claims { it.putAll(claims) }
                .build()
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
    }
}
