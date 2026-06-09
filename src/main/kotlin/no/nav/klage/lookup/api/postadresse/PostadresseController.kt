package no.nav.klage.lookup.api.postadresse

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.regoppslag.RegoppslagService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping
class PostadresseController(
    private val regoppslagService: RegoppslagService,
) {

    @Operation(summary = "Hent postadresse for oppgitt bruker")
    @PostMapping("/postadresse")
    fun getPostadresse(
        @RequestBody input: PostadresseRequest,
    ): PostadresseResponse {
        return regoppslagService.getPostadresse(input)
    }
}

