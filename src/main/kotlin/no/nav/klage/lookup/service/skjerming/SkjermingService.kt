package no.nav.klage.lookup.service.skjerming

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.config.CacheConfiguration.Companion.PERSON
import no.nav.klage.lookup.config.CacheConfiguration.Companion.SKJERMET
import no.nav.klage.lookup.config.skjerming.SkjermingBulkRequest
import no.nav.klage.lookup.config.skjerming.SkjermingClient
import no.nav.klage.lookup.config.skjerming.SkjermingRequest
import no.nav.klage.lookup.service.kabalapi.KabalApiService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class SkjermingService(
    private val skjermingClient: SkjermingClient,
    private val kabalApiService: KabalApiService,
    private val cacheManager: CacheManager,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val SKJERMING_TIMER = "skjerming.response.time"
    }

    @Cacheable(SKJERMET)
    @Retryable
    fun skjermet(foedselsnr: String): Boolean =
        meterRegistry.timedCall(SKJERMING_TIMER, "skjermet") {
            skjermingClient.skjermet(
                bearerToken = tokenUtil.getAppAccessTokenWithSkjermingPipScope(),
                personident = SkjermingRequest(personident = foedselsnr),
            )
        }

    @Retryable
    fun skjermetBulk(foedselsnrList: List<String>): Map<String, Boolean> =
        meterRegistry.timedCall(SKJERMING_TIMER, "skjermetBulk") {
            skjermingClient.skjermetBulk(
                bearerToken = tokenUtil.getAppAccessTokenWithSkjermingPipScope(),
                skjermingBulkRequest = SkjermingBulkRequest(personidenter = foedselsnrList),
            )
        }

    fun updateSkjermetPerson(foedselsnr: String, skjermetPerson: SkjermetPerson) {
        logger.debug("Update skjermet person in cache, and notify kabal-api.")
        cacheManager.getCache(SKJERMET)!!.put(foedselsnr, skjermetPerson.skjermet())
        cacheManager.getCache(PERSON)!!.evictIfPresent(foedselsnr)
        kabalApiService.setPersonProtectionChanged(foedselsnr)
    }

    fun removeSkjermetPerson(foedselsnr: String) {
        logger.debug("Removing skjermet person from cache, and notify kabal-api.")
        cacheManager.getCache(SKJERMET)!!.evictIfPresent(foedselsnr)
        kabalApiService.setPersonProtectionChanged(foedselsnr)
    }
}