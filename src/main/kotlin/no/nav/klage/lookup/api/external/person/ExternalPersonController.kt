package no.nav.klage.lookup.api.external.person

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.PersonService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@ProtectedWithClaims(issuer = SecurityConfiguration.TOKEN_X, claimMap = ["acr=Level4"])
@RestController
@RequestMapping
class ExternalPersonController(
    private val personService: PersonService,
) {
    @Operation(summary = "Get person info. Only accessible for reflective operation on logged in user and fullmektige.")
    @PostMapping("/external/person")
    fun getPerson(
        @RequestBody input: ExternalPersonRequest,
    ): Person {
        return personService.getPerson(
            fnr = input.fnr,
            tema = input.tema
        )
    }
}

