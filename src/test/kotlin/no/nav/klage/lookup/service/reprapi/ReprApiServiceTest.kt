package no.nav.klage.lookup.service.reprapi

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.lookup.api.repr.FullmaktsforholdDto
import no.nav.klage.lookup.api.repr.RepresentasjonsforholdDto
import no.nav.klage.lookup.api.repr.VergemaalsforholdDto
import no.nav.klage.lookup.config.reprapi.ReprApiClient
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReprApiServiceTest {

    private val reprApiClient = mockk<ReprApiClient>()
    private val tokenUtil = mockk<TokenUtil>()
    private val meterRegistry = SimpleMeterRegistry()

    private val reprApiService = ReprApiService(
        reprApiClient = reprApiClient,
        tokenUtil = tokenUtil,
        meterRegistry = meterRegistry,
    )

    @Test
    fun `kanRepresentere uses token exchange from TokenX with repr-api scope`() {
        val expected = RepresentasjonsforholdDto(
            fullmakt = listOf(
                FullmaktsforholdDto(
                    fullmaktsgiver = "12345678901",
                    fullmektig = "10987654321",
                    leserettigheter = setOf("KLAGE"),
                    skriverettigheter = setOf("KLAGE"),
                )
            ),
            vergemaal = listOf(
                VergemaalsforholdDto(
                    vergehaver = "12345678901",
                    verge = "10987654321",
                    leserettigheter = setOf("KLAGE"),
                    skriverettigheter = setOf("KLAGE"),
                )
            ),
        )

        every { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() } returns "token-x-obo"
        every { reprApiClient.kanRepresentere("Bearer token-x-obo") } returns expected

        val actual = reprApiService.kanRepresentere()

        assertThat(actual).isEqualTo(expected)
        verify(exactly = 1) { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() }
        verify(exactly = 1) { reprApiClient.kanRepresentere("Bearer token-x-obo") }
    }
}

