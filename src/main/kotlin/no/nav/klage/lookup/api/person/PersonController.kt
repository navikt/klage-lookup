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

    @Operation(summary = "Get person. Word of caution: client needs to have proper access control.")
    @PostMapping("/person")
    fun getPerson(
        @RequestBody input: PersonRequest,
    ): Person {
        return personService.getPerson(
            fnr = input.fnr,
            sak = input.sak,
        )
    }

    @Operation(summary = "Get fødselsnummer from ident.")
    @PostMapping("/foedselsnummer")
    fun getFoedselsnummerFromIdent(
        @RequestBody input: IdentRequest,
    ): FnrResponse {
        return FnrResponse(personService.getFoedselsnummerFromIdent(ident = input.ident))
    }

    @Operation(summary = "Get aktør ID from ident.")
    @PostMapping("/aktoerid")
    fun getAktoerIdFromIdent(
        @RequestBody input: IdentRequest,
    ): AktoerIdResponse {
        return AktoerIdResponse(personService.getAktoerIdFromIdent(ident = input.ident))
    }

}