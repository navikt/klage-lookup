package no.nav.klage.lookup.config.nom

import no.nav.klage.lookup.service.nom.graphql.AnsattGraphqlQuery
import no.nav.klage.lookup.service.nom.graphql.AnsatteGraphqlQuery
import no.nav.klage.lookup.service.nom.graphql.GetAnsattResponse
import no.nav.klage.lookup.service.nom.graphql.GetAnsatteResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.PostExchange

interface NomClient {
    @PostExchange
    fun hentAnsatt(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody query: AnsattGraphqlQuery,
    ): GetAnsattResponse

    @PostExchange
    fun hentAnsatte(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestBody query: AnsatteGraphqlQuery,
    ): GetAnsatteResponse
}
