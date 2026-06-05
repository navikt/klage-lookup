package no.nav.klage.lookup.service.regoppslag

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.lookup.api.postadresse.Postadresse
import no.nav.klage.lookup.api.postadresse.PostadresseRequest
import no.nav.klage.lookup.api.postadresse.PostadresseResponse
import no.nav.klage.lookup.config.regoppslag.RegoppslagClient
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class RegoppslagServiceTest {

    private val regoppslagClient = mockk<RegoppslagClient>()
    private val tokenUtil = mockk<TokenUtil>()
    private val meterRegistry = SimpleMeterRegistry()

    private val regoppslagService = RegoppslagService(
        regoppslagClient = regoppslagClient,
        tokenUtil = tokenUtil,
        meterRegistry = meterRegistry,
    )

    @Test
    fun `getPostadresse uses on-behalf-of token when user is logged in`() {
        val request = PostadresseRequest(ident = "889640782")
        val expected = PostadresseResponse(
            navn = "ARBEIDS- OG VELFERDSETATEN",
            adresse = Postadresse(
                adresseKilde = "Bostedsadresse",
                type = "NorskPostadresse",
                adresselinje1 = "Postboks 5 St Olavs Plass",
                adresselinje2 = null,
                adresselinje3 = null,
                postnummer = "0130",
                poststed = "OSLO",
                landkode = "NO",
                land = "Norge",
            ),
        )

        every { tokenUtil.getIdent() } returns "A123456"
        every { tokenUtil.getOnBehalfOfTokenWithRegoppslagScope() } returns "obo-token"
        every { regoppslagClient.hentPostadresse("Bearer obo-token", request) } returns expected

        val actual = regoppslagService.getPostadresse(request)

        assertThat(actual).isEqualTo(expected)
        verify(exactly = 0) { tokenUtil.getAppAccessTokenWithRegoppslagScope() }
    }

    @Test
    fun `getPostadresse maps no-content response to RegoppslagAdresseFiltrertException`() {
        val request = PostadresseRequest(ident = "889640782")

        every { tokenUtil.getIdent() } returns null
        every { tokenUtil.getAppAccessTokenWithRegoppslagScope() } returns "app-token"
        every { regoppslagClient.hentPostadresse("Bearer app-token", request) } returns null

        assertThatThrownBy { regoppslagService.getPostadresse(request) }
            .isInstanceOf(RegoppslagAdresseFiltrertException::class.java)
            .hasMessage("Adresse til adressebeskyttet bruker er filtrert bort.")
    }

    @Test
    fun `getPostadresse maps 404 from Regoppslag to RegoppslagUkjentAdresseException`() {
        val request = PostadresseRequest(ident = "889640782")

        every { tokenUtil.getIdent() } returns null
        every { tokenUtil.getAppAccessTokenWithRegoppslagScope() } returns "app-token"
        every {
            regoppslagClient.hentPostadresse("Bearer app-token", request)
        } throws HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "Not Found",
            HttpHeaders.EMPTY,
            ByteArray(0),
            Charsets.UTF_8,
        )

        assertThatThrownBy { regoppslagService.getPostadresse(request) }
            .isInstanceOf(RegoppslagUkjentAdresseException::class.java)
            .hasMessage("Person / organisasjon har ukjent adresse.")
    }
}

