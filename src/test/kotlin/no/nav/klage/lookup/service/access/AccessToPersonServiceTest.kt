package no.nav.klage.lookup.service.access

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenClient
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AccessToPersonServiceTest {
    private val tilgangsmaskinenClient = mockk<TilgangsmaskinenClient>()
    private val tokenUtil = mockk<TokenUtil>()

    private val service =
        AccessToPersonService(
            tilgangsmaskinenClient = tilgangsmaskinenClient,
            tokenUtil = tokenUtil,
            meterRegistry = SimpleMeterRegistry(),
        )

    @Test
    fun `getNavIdentAccessToUserWithAppToken sjekker oppgitt navIdent med app-token selv om det finnes en innlogget bruker`() {
        every { tokenUtil.getIdent() } returns "INNLOGGET"
        every { tokenUtil.getAppAccessTokenWithTilgangsmaskinenScope() } returns "app-token"
        every {
            tilgangsmaskinenClient.validateAccess(clientBearerToken = any(), brukerId = "12345678910", navIdent = "Z111111")
        } just runs

        val result = service.getNavIdentAccessToUserWithAppToken(brukerId = "12345678910", navIdent = "Z111111")

        assertThat(result.access).isTrue()
        verify(exactly = 1) {
            tilgangsmaskinenClient.validateAccess(clientBearerToken = "Bearer app-token", brukerId = "12345678910", navIdent = "Z111111")
        }
        verify(exactly = 0) { tilgangsmaskinenClient.validateAccessWithObo(oboBearerToken = any(), brukerId = any()) }
        verify(exactly = 0) { tokenUtil.getSaksbehandlerAccessTokenWithTilgangsmaskinenScope() }
    }
}
