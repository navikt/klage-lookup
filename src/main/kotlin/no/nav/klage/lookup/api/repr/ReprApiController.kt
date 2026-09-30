package no.nav.klage.lookup.api.repr

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.api.external.repr.RepresentasjonsforholdView
import no.nav.klage.lookup.api.person.IdentRequest
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.reprapi.ReprApiService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping("/internal")
class ReprApiController(
    private val reprApiService: ReprApiService,
) {
    @Operation(summary = "Hent representasjonsforhold for oppgitt ident, fungerer for saksbehandler med Entra ID-innlogging")
    @PostMapping("/representasjon/representasjonsforhold")
    fun getRepresentasjonsforhold(
        @RequestBody input: IdentRequest,
    ): RepresentasjonsforholdView = reprApiService.internalKanRepresenteresAv(ident = input.ident)
}
