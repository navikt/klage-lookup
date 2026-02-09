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

@Tag(name = "saksbehandler", description = "API for getting info about saksbehandler")
@ProtectedWithClaims(issuer = SecurityConfiguration.Companion.ISSUER_AAD)
@RestController
@RequestMapping("/user")
class UserController(
    private val saksbehandlerService: SaksbehandlerService,
) {

    @Operation(summary = "Get info about user")
    @GetMapping("/info/{navIdent}")
    fun getUserInfo(
        @PathVariable navIdent: String,
    ): UserResponse {
        return saksbehandlerService.getUserInfo(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "User has Kabal saksbehandler role")
    @GetMapping("/is-kabal-saksbehandler/{navIdent}")
    fun isKabalSaksbehandler(
        @PathVariable navIdent: String,
    ): Boolean {
        return saksbehandlerService.userIsKabalSaksbehandler(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "User has KROL role")
    @GetMapping("/is-krol/{navIdent}")
    fun isKROL(
        @PathVariable navIdent: String,
    ): Boolean {
        return saksbehandlerService.userIsKROL(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "User has ROL role")
    @GetMapping("/is-rol/{navIdent}")
    fun isROL(
        @PathVariable navIdent: String,
    ): Boolean {
        return saksbehandlerService.userIsROL(
            navIdent = navIdent,
        )
    }

    @Operation(summary = "Logged in user has Kabal saksbehandler role")
    @GetMapping("/me/is-kabal-saksbehandler")
    fun loggedInUserIsKabalSaksbehandler(): Boolean {
        return saksbehandlerService.loggedInUserIsKabalSaksbehandler()
    }

    @Operation(summary = "Logged in user has KROL role")
    @GetMapping("/me/is-krol")
    fun loggedInUserIsKROL(): Boolean {
        return saksbehandlerService.loggedInUserIsKROL()
    }

    @Operation(summary = "Logged in user has ROL role")
    @GetMapping("/me/is-rol")
    fun loggedInUserIsROL(): Boolean {
        return saksbehandlerService.loggedInUserIsROL()
    }

    @Operation(summary = "Get info about logged in user")
    @GetMapping("/me/info")
    fun getLoggedInUserInfo(): UserResponse {
        return saksbehandlerService.getUserInfoForLoggedInUser()
    }
}