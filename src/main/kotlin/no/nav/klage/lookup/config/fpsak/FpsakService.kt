package no.nav.klage.lookup.config.fpsak

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

interface FpsakService {

    @GetExchange("/fpsak/ekstern/api/pip/aktoer-for-sak")
    fun getAktoerForSak(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestParam saksnummer: String,
    ): List<String>
}
