package no.nav.klage.lookup.api

import no.nav.klage.lookup.service.CacheService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile("dev")
@RestController
@RequestMapping("/dev/admin")
@Unprotected
class DevAdminController(
    private val cacheService: CacheService,
    private val tokenUtil: TokenUtil,
) {

    @GetMapping("/evict-cache")
    fun evictCache(
        @RequestParam(value = "cacheName", required = false) cacheName: String?,
        @RequestParam(value = "cacheKey", required = false) cacheKey: String?,
    ) {
        if (cacheKey != null && cacheName == null) {
            throw RuntimeException("If cacheKey is provided, cacheName must also be provided")
        }

        if (cacheKey != null) {
            cacheService.evictSingleCacheValue(cacheName = cacheName!!, cacheKey = cacheKey)
        } else if (cacheName != null) {
            cacheService.evictSingleCache(cacheName = cacheName)
        } else {
            cacheService.evictAllCaches()
        }
    }

    @GetMapping("/mytokens")
    fun getTokens(): Map<String, String> {
        return mapOf(
            "getSaksbehandlerAccessTokenWithEntraProxyScope" to tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope(),
        )
    }
}