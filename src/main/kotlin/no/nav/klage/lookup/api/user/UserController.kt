package no.nav.klage.lookup.api.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
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

    @Operation(summary = "Get groups for user")
    @GetMapping("/users/{navIdent}/groups")
    fun getGroupsForUser(
        @PathVariable navIdent: String,
    ): GroupsResponse {
        return saksbehandlerService.getGroupsForUser(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "Get users in given enhet")
    @GetMapping("/enheter/{enhetsnummer}/users")
    fun getUsersInEnhet(
        @PathVariable enhetsnummer: String,
    ): UsersResponse {
        return saksbehandlerService.getUsersInEnhet(
            enhetsnummer = enhetsnummer,
        )
    }

    @Operation(summary = "Get users in given Azure group")
    @GetMapping("/groups/{groupId}/users")
    fun getUsersInGroup(
        @PathVariable groupId: String,
    ): UsersResponse {
        return saksbehandlerService.getUsersInGroup(
            azureGroup = AzureGroup.of(groupId),
        )
    }
}