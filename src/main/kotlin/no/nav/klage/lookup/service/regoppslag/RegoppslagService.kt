package no.nav.klage.lookup.service.regoppslag

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.api.postadresse.PostadresseRequest
import no.nav.klage.lookup.api.postadresse.PostadresseResponse
import no.nav.klage.lookup.config.CacheConfiguration.Companion.POSTADRESSE
import no.nav.klage.lookup.config.regoppslag.RegoppslagClient
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class RegoppslagService(
    private val regoppslagClient: RegoppslagClient,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        private const val REGOPPSLAG_TIMER = "regoppslag.response.time"

        private const val ADRESSE_FILTRERT_BORT = "Adresse til adressebeskyttet bruker er filtrert bort."
        private const val UGYLDIG_INPUT = "Ugyldig input. Denne feilen vil returneres hvis det feil i input verdiene."
        private const val INGEN_TILGANG = "Ingen tilgang til postadresse tjenesten."
        private const val TILGANG_AVVIST = "Tilgang til å hente postadresse avvist"
        private const val UKJENT_ADRESSE = "Person / organisasjon har ukjent adresse."
        private const val PERSON_ER_DOED = "Person er død og har ukjent adresse."
        private const val INTERN_TEKNISK_FEIL = "Intern teknisk feil i postadresse tjenesten."
    }

    @Cacheable(POSTADRESSE)
    fun getPostadresse(request: PostadresseRequest): PostadresseResponse {
        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getOnBehalfOfTokenWithRegoppslagScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithRegoppslagScope()}"
        }

        return try {
            meterRegistry.timedCall(REGOPPSLAG_TIMER, ::getPostadresse.name) {
                regoppslagClient.hentPostadresse(
                    bearerToken = bearerToken,
                    request = request,
                )
            } ?: throw RegoppslagAdresseFiltrertException(ADRESSE_FILTRERT_BORT)
        } catch (ex: RestClientResponseException) {
            when (ex.statusCode.value()) {
                HttpStatus.NO_CONTENT.value() -> throw RegoppslagAdresseFiltrertException(ADRESSE_FILTRERT_BORT)
                HttpStatus.BAD_REQUEST.value() -> throw RegoppslagUgyldigInputException(UGYLDIG_INPUT)
                HttpStatus.UNAUTHORIZED.value() -> throw RegoppslagIngenTilgangException(INGEN_TILGANG)
                HttpStatus.FORBIDDEN.value() -> throw RegoppslagTilgangAvvistException(TILGANG_AVVIST)
                HttpStatus.NOT_FOUND.value() -> throw RegoppslagUkjentAdresseException(UKJENT_ADRESSE)
                HttpStatus.GONE.value() -> throw RegoppslagPersonDoedException(PERSON_ER_DOED)
                HttpStatus.INTERNAL_SERVER_ERROR.value() -> throw RegoppslagInternTekniskFeilException(
                    INTERN_TEKNISK_FEIL
                )

                else -> throw ex
            }
        }
    }
}

class RegoppslagAdresseFiltrertException(msg: String) : RuntimeException(msg)

class RegoppslagUgyldigInputException(msg: String) : RuntimeException(msg)

class RegoppslagIngenTilgangException(msg: String) : RuntimeException(msg)

class RegoppslagTilgangAvvistException(msg: String) : RuntimeException(msg)

class RegoppslagUkjentAdresseException(msg: String) : RuntimeException(msg)

class RegoppslagPersonDoedException(msg: String) : RuntimeException(msg)

class RegoppslagInternTekniskFeilException(msg: String) : RuntimeException(msg)

