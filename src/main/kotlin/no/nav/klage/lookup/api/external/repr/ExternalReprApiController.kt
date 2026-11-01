package no.nav.klage.lookup.api.external.repr

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.service.reprapi.ReprApiService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/external")
class ExternalReprApiController(
    private val reprApiService: ReprApiService,
) {
    @Operation(summary = "Hent representasjonsforhold for innlogget bruker, fungerer for sluttbruker med token-x-innlogging")
    @GetMapping("/representasjon/kan-representere")
    fun getRepresentasjonsforhold(): RepresentasjonsforholdView = reprApiService.externalKanRepresentere()
}
