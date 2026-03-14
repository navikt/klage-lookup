package no.nav.klage.lookup.service.pdl.graphql

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.PostExchange

interface PdlService {

    @PostExchange
    fun getPerson(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody query: PersonGraphqlQuery,
    ): HentPersonResponse

    @PostExchange
    fun getPersonBulk(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody query: PersonBulkGraphqlQuery,
    ): HentPersonBolkResponse

    @PostExchange
    fun getIdent(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody query: PersonGraphqlQuery,
    ): HentIdenterResponse
}