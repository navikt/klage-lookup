package no.nav.klage.lookup.api.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.SaksbehandlerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "user", description = "API for getting info about users")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping
class UserController(

    private val saksbehandlerService: SaksbehandlerService,
) {

    @Operation(summary = "Get info about user")
    @GetMapping("/users/{navIdent}")
    fun getUserInfo(
        @PathVariable navIdent: String,
    ): ExtendedUserResponse {
        return saksbehandlerService.getUserInfo(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "Get group memberships for user")
    @GetMapping("/users/{navIdent}/group-memberships")
    fun getGroupMemberships(
        @PathVariable navIdent: String,
    ): GroupMembershipsResponse {
        return saksbehandlerService.getGroupMemberships(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "Get users in given enhet")
    @GetMapping("/enheter/{enhetsnummer}/users-in-enhet")
    fun getUsersInEnhet(
        @PathVariable enhetsnummer: String,
    ): List<UserResponse> {
        return saksbehandlerService.getUsersInEnhet(
            enhetsnummer = enhetsnummer,
        )
    }
}