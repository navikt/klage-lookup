package no.nav.klage.lookup.config.regoppslag

import no.nav.klage.lookup.api.postadresse.PostadresseRequest
import no.nav.klage.lookup.api.postadresse.PostadresseResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.PostExchange

interface RegoppslagClient {

    @PostExchange("/rest/postadresse")
    fun hentPostadresse(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody request: PostadresseRequest,
    ): PostadresseResponse?
}