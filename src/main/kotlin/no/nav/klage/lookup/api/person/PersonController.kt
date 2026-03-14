package no.nav.klage.lookup.api.person

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.PersonService
import no.nav.klage.lookup.service.pdl.Person
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping
class PersonController(
    private val personService: PersonService
) {

    @Operation(summary = "Get person. Word of caution: make sure in the client that proper access control is in place.")
    @PostMapping("/person")
    fun getPerson(
        @RequestBody input: PersonRequest,
    ): Person {
        return personService.getPerson(
            fnr = input.ident,
            sak = input.sak,
        )
    }

}