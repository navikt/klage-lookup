package no.nav.klage.lookup.api.persongalleri

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.api.common.Sak
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.persongalleri.PersongalleriService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping
class PersongalleriController(
    private val persongalleriService: PersongalleriService,
) {

    @Operation(summary = "Get persongalleri for a sak")
    @PostMapping("/persongalleri")
    fun getPersongalleri(
        @RequestBody input: Sak,
    ): PersongalleriResponse {
        return persongalleriService.getPersongalleri(
            sak = input,
        )
    }
}