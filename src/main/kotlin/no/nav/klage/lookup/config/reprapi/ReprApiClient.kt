package no.nav.klage.lookup.config.reprapi

import no.nav.klage.lookup.api.external.repr.RepresentasjonsforholdDto
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange

interface ReprApiClient {
    @GetExchange("/api/v2/eksternbruker/kan-representere")
    fun externalKanRepresentere(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
    ): RepresentasjonsforholdDto

    @PostExchange("/api/v2/internbruker/kan-representeres-av")
    fun internalKanRepresenteresAv(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody request: IdentRequest,
    ): RepresentasjonsforholdDto
}

data class IdentRequest(
    val ident: String,
)
