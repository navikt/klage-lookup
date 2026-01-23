package no.nav.klage.lookup.config.tilgangsmaskinen

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.PostExchange

interface TilgangsmaskinenService {

    //TODO make contenttype be default application/json in some global config

    @PostExchange("/api/v1/komplett")
    fun validateAccessWithObo(
        @RequestHeader(AUTHORIZATION) oboBearerToken: String,
        @RequestBody brukerId: String,
    )

    @PostExchange("/api/v1/ccf/komplett/{navIdent}")
    fun validateAccess(
        @RequestHeader(AUTHORIZATION) clientBearerToken: String,
        @RequestBody brukerId: String,
        @PathVariable navIdent: String,
    )
}