package no.nav.klage.lookup.config.entraproxy

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

interface EntraProxyService {
    @GetExchange("/api/v1/gruppe/medlemmer")
    fun getGroupMembersWithObo(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @RequestParam gruppeNavn: String,
    ): List<EntraProxyAnsatt>
}