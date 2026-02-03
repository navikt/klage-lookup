package no.nav.klage.lookup.api

import no.nav.klage.lookup.config.SecurityConfiguration
import no.nav.klage.lookup.service.CacheService
import no.nav.klage.lookup.service.SaksbehandlerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@RequestMapping("/admin")
class AdminController(
    private val saksbehandlerService: SaksbehandlerService,
    private val cacheService: CacheService,
) {

    @GetMapping("/evict-cache")
    fun evictCache(
        @RequestParam(value = "cacheName", required = false) cacheName: String?,
        @RequestParam(value = "cacheKey", required = false) cacheKey: String?,
    ) {
        requireAdminAccess()
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


    private fun requireAdminAccess() {
        if (!saksbehandlerService.loggedInUserIsKabalAdmin()) {
            throw RuntimeException("Not an admin")
        }
    }
}