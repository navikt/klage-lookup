package no.nav.klage.lookup.service.access

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AccessToPersonBatchServiceTest {
    private val accessToPersonService = mockk<AccessToPersonService>()
    private val batchService = AccessToPersonBatchService(accessToPersonService = accessToPersonService)

    private val brukerId = "12345678910"
    private val granted = Access(access = true, reason = "Access granted")
    private val denied = Access(access = false, reason = "Skjermet")

    @Test
    fun `returnerer tilgang per navIdent`() {
        every { accessToPersonService.getNavIdentAccessToUserWithAppToken(brukerId = brukerId, navIdent = "Z111111") } returns granted
        every { accessToPersonService.getNavIdentAccessToUserWithAppToken(brukerId = brukerId, navIdent = "Z222222") } returns denied

        val result = batchService.getNavIdentsAccessToUser(brukerId = brukerId, navIdentList = listOf("Z111111", "Z222222"))

        assertThat(result).containsExactlyInAnyOrderEntriesOf(mapOf("Z111111" to granted, "Z222222" to denied))
    }

    @Test
    fun `sjekker hver navIdent bare en gang`() {
        every { accessToPersonService.getNavIdentAccessToUserWithAppToken(brukerId = brukerId, navIdent = "Z111111") } returns granted

        val result = batchService.getNavIdentsAccessToUser(brukerId = brukerId, navIdentList = listOf("Z111111", "Z111111"))

        assertThat(result).containsOnlyKeys("Z111111")
        verify(exactly = 1) { accessToPersonService.getNavIdentAccessToUserWithAppToken(brukerId = brukerId, navIdent = "Z111111") }
    }

    @Test
    fun `tom liste gir tomt svar`() {
        assertThat(batchService.getNavIdentsAccessToUser(brukerId = brukerId, navIdentList = emptyList())).isEmpty()
    }

    @Test
    fun `feil i en sjekk feiler hele batchen med opprinnelig exception`() {
        every { accessToPersonService.getNavIdentAccessToUserWithAppToken(brukerId = brukerId, navIdent = "Z111111") } returns granted
        every { accessToPersonService.getNavIdentAccessToUserWithAppToken(brukerId = brukerId, navIdent = "Z222222") } throws
            IllegalStateException("Tilgangsmaskinen nede")

        assertThatThrownBy {
            batchService.getNavIdentsAccessToUser(brukerId = brukerId, navIdentList = listOf("Z111111", "Z222222"))
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Tilgangsmaskinen nede")
    }
}
