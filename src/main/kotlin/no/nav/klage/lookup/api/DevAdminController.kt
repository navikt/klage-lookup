package no.nav.klage.lookup.api

import no.nav.klage.lookup.api.user.BatchedSluttdatoResponse
import no.nav.klage.lookup.config.entraproxy.EntraProxyRolle
import no.nav.klage.lookup.service.CacheService
import no.nav.klage.lookup.service.EntraProxyService
import no.nav.klage.lookup.service.SaksbehandlerService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile("dev")
@RestController
@RequestMapping("/dev/admin")
@Unprotected
class DevAdminController(
    private val cacheService: CacheService,
    private val tokenUtil: TokenUtil,
    private val entraProxyService: EntraProxyService,
    private val saksbehandlerService: SaksbehandlerService
) {

    @GetMapping("/evict-cache")
    fun evictCache(
        @RequestParam(value = "cacheName", required = false) cacheName: String?,
        @RequestParam(value = "cacheKey", required = false) cacheKey: String?,
    ) {
        if (cacheKey != null && cacheName == null) {
            throw IllegalArgumentException("If cacheKey is provided, cacheName must also be provided")
        }

        if (cacheKey != null) {
            cacheService.evictSingleCacheValue(cacheName = cacheName!!, cacheKey = cacheKey)
        } else if (cacheName != null) {
            cacheService.evictSingleCache(cacheName = cacheName)
        } else {
            cacheService.evictAllCaches()
        }
    }

    @GetMapping("/sluttdato/{navIdent}")
    fun getSluttdatoTest(
        @PathVariable navIdent: String,
    ): BatchedSluttdatoResponse {
        return saksbehandlerService.getSluttdatoForUsers(navIdentList = listOf(navIdent))
    }

    @GetMapping("/mygroups")
    fun getUserGroups(): List<EntraProxyRolle> {
        return entraProxyService.getUsersGroups(tokenUtil.getIdent()!!)
    }

    @GetMapping("/mytokens")
    fun getTokens(): Map<String, String> {
        return mapOf(
            "getSaksbehandlerAccessTokenWithEntraProxyScope" to tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope(),
            "getAppAccessTokenWithEntraProxyScope" to tokenUtil.getAppAccessTokenWithEntraProxyScope(),
        )
    }
}