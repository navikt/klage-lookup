package no.nav.klage.lookup.config.skjerming

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.PostExchange

interface SkjermingClient {

    @PostExchange("/skjermet")
    fun skjermet(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody personident: SkjermingRequest,
    ): Boolean
}