package no.nav.klage.lookup.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.SaksbehandlerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "saksbehandler", description = "API for getting info about saksbehandler")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping("/saksbehandler")
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
) {

    @Operation(summary = "Saksbehandler has Kabal saksbehandler role")
    @GetMapping("/is-kabal-saksbehandler/{navIdent}")
    fun isKabalSaksbehandler(
        @PathVariable("navIdent") navIdent: String? = null,
    ): Boolean {
        return saksbehandlerService.userIsKabalSaksbehandler(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "Saksbehandler has KROL role")
    @PostMapping("/is-krol")
    fun isKROL(
        @RequestBody input: SaksbehandlerRequest,
    ): Boolean {
        return saksbehandlerService.userIsKROL(
            navIdent = input.navIdent,
        )
    }

    @Operation(summary = "Saksbehandler has ROL role")
    @PostMapping("/is-rol")
    fun isROL(
        @RequestBody input: SaksbehandlerRequest,
    ): Boolean {
        return saksbehandlerService.userIsROL(
            navIdent = input.navIdent,
        )
    }

    data class SaksbehandlerRequest(
        val navIdent: String,
    )
}