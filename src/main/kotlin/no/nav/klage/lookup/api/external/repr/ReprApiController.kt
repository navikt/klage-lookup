package no.nav.klage.lookup.api.external.repr

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.reprapi.ReprApiService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.TOKEN_X, claimMap = ["acr=Level4"])
@RestController
@RequestMapping
class ReprApiController(
    private val reprApiService: ReprApiService,
) {

    @Operation(summary = "Hent representasjonsforhold for innlogget bruker, fungerer for sluttbruker med token-x-innlogging")
    @GetMapping("ekstern/representasjon/kan-representere")
    fun getRepresentasjonsforhold(): RepresentasjonsforholdView {
        return reprApiService.kanRepresentere()
    }
}

