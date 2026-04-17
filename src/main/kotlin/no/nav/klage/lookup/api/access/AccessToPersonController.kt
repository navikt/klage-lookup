package no.nav.klage.lookup.api.access

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.access.Access
import no.nav.klage.lookup.service.access.AccessToPersonService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "access", description = "API for verifying access to person")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping("/")
class AccessToPersonController(
    private val accessToPersonService: AccessToPersonService,
    private val tokenUtil: TokenUtil,
) {

    @Operation(summary = "Verify access to person")
    @PostMapping("/access-to-person")
    fun getNavIdentAccessToUser(
        @RequestBody input: AccessRequest,
    ): Access {
        return accessToPersonService.getNavIdentAccessToUser(
            brukerId = input.brukerId,
            navIdent = input.navIdent ?: tokenUtil.getIdent() ?: throw IllegalArgumentException("navIdent must be provided if no innlogget ident is found"),
        )
    }
}