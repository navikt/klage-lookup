package no.nav.klage.lookup.config.reprapi

import no.nav.klage.lookup.api.repr.RepresentasjonsforholdDto
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange

interface ReprApiClient {

    @GetExchange("/api/v2/eksternbruker/kan-representere")
    fun kanRepresentere(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
    ): RepresentasjonsforholdDto
}

