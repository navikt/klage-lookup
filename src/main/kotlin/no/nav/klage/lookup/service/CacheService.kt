package no.nav.klage.lookup.service

import no.nav.klage.lookup.util.getLogger
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Service

@Service
class CacheService(
    private val cacheManager: RedisCacheManager,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun evictSingleCacheValue(cacheName: String, cacheKey: String) {
        if (!cacheManager.cacheNames.contains(cacheName)) {
            throw RuntimeException("Cache $cacheName not found")
        }
        logger.debug("Evicting value $cacheKey in cache $cacheName")
        cacheManager.getCache(cacheName)?.evict(cacheKey)
        logger.debug("Evicted value $cacheKey in cache $cacheName")
    }

    fun evictSingleCache(cacheName: String) {
        if (!cacheManager.cacheNames.contains(cacheName)) {
            throw RuntimeException("Cache $cacheName not found")
        }
        logger.debug("Evicting cache $cacheName")
        cacheManager.getCache(cacheName)?.clear()
        logger.debug("Evicted cache $cacheName")
    }

    fun evictAllCaches() {
        logger.debug("Evicting all caches")
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
            logger.debug("Evicted cache $cacheName")
        }
    }
}