package no.nav.klage.lookup.api.access

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.lookup.service.access.Access
import no.nav.klage.lookup.service.access.AccessToPersonBatchService
import no.nav.klage.lookup.service.access.AccessToPersonService
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class AccessToPersonControllerTest {
    private val accessToPersonBatchService = mockk<AccessToPersonBatchService>()
    private val tokenUtil = mockk<TokenUtil>()

    private val controller =
        AccessToPersonController(
            accessToPersonService = mockk<AccessToPersonService>(),
            accessToPersonBatchService = accessToPersonBatchService,
            tokenUtil = tokenUtil,
        )

    private val input = BatchedAccessRequest(brukerId = "12345678910", navIdentList = listOf("Z111111", "Z222222"))

    @Test
    fun `batch avvises med brukertoken`() {
        every { tokenUtil.getIdent() } returns "INNLOGGET"

        assertThatThrownBy { controller.getNavIdentsAccessToUser(input) }
            .isInstanceOfSatisfying(ResponseStatusException::class.java) {
                assertThat(it.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
        verify(exactly = 0) { accessToPersonBatchService.getNavIdentsAccessToUser(brukerId = any(), navIdentList = any()) }
    }

    @Test
    fun `batch med app-token returnerer resultat per navIdent`() {
        every { tokenUtil.getIdent() } returns null
        every {
            accessToPersonBatchService.getNavIdentsAccessToUser(
                brukerId = "12345678910",
                navIdentList = listOf("Z111111", "Z222222"),
            )
        } returns
            mapOf(
                "Z111111" to Access(access = true, reason = "Access granted"),
                "Z222222" to Access(access = false, reason = "Skjermet"),
            )

        val result = controller.getNavIdentsAccessToUser(input)

        assertThat(result.results).containsExactlyInAnyOrder(
            NavIdentAccess(navIdent = "Z111111", access = true, reason = "Access granted"),
            NavIdentAccess(navIdent = "Z222222", access = false, reason = "Skjermet"),
        )
    }
}
