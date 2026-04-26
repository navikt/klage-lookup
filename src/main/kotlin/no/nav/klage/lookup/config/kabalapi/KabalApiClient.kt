package no.nav.klage.lookup.config.kabalapi

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.PostExchange

interface KabalApiClient {

    @PostExchange("/api/person-protection/changed")
    fun setPersonProtectionChanged(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody query: KabalApiRequest,
    )
}