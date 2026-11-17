package no.nav.klage.lookup.api.access

import io.swagger.v3.oas.annotations.Operation
import no.nav.klage.lookup.service.access.Access
import no.nav.klage.lookup.service.access.AccessToPersonBatchService
import no.nav.klage.lookup.service.access.AccessToPersonService
import no.nav.klage.lookup.util.TokenUtil
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class AccessToPersonController(
    private val accessToPersonService: AccessToPersonService,
    private val accessToPersonBatchService: AccessToPersonBatchService,
    private val tokenUtil: TokenUtil,
) {
    @Operation(summary = "Verify access to person")
    @PostMapping("/access-to-person")
    fun getNavIdentAccessToUser(
        @RequestBody input: AccessRequest,
    ): Access =
        accessToPersonService.getNavIdentAccessToUser(
            brukerId = input.brukerId,
            navIdent =
                input.navIdent ?: tokenUtil.getIdent()
                    ?: throw IllegalArgumentException("navIdent must be provided if no innlogget ident is found"),
        )

    @Operation(
        summary = "Verify access to person for multiple Nav employees",
        description = "Machine-to-machine only. Checks each navIdent with the app token.",
    )
    @PostMapping("/access-to-person/batch")
    fun getNavIdentsAccessToUser(
        @RequestBody input: BatchedAccessRequest,
    ): BatchedAccessResponse {
        // A user token must not be able to look up other employees' access.
        if (tokenUtil.getIdent() != null) {
            throw MissingAppTokenException("Batch access check requires an app token")
        }
        val accessByNavIdent =
            accessToPersonBatchService.getNavIdentsAccessToUser(
                brukerId = input.brukerId,
                navIdentList = input.navIdentList,
            )
        return BatchedAccessResponse(
            results =
                accessByNavIdent.map { (navIdent, access) ->
                    NavIdentAccess(
                        navIdent = navIdent,
                        access = access.access,
                        reason = access.reason,
                    )
                },
        )
    }
}

class MissingAppTokenException(
    msg: String,
) : RuntimeException(msg)
