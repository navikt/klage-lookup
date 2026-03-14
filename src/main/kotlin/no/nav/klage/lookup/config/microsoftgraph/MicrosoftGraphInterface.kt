package no.nav.klage.lookup.config.microsoftgraph

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange


interface MicrosoftGraphInterface {
    @GetExchange("/users")
    fun microsoftGraphQuery(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestHeader("ConsistencyLevel") consistencyLevel: String,
        @RequestParam("\$filter") filter: String,
        @RequestParam("\$select") select: String,
        @RequestParam("\$count") count: Boolean,
        @RequestParam("\$top") top: Int? = null,
    ): MicrosoftGraphUserList
}