package no.nav.klage.lookup.service.fpsak

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.config.CacheConfiguration.Companion.AKTOER_FOR_SAK
import no.nav.klage.lookup.config.fpsak.FpsakClient
import no.nav.klage.lookup.service.pdl.PdlFacade
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class FpsakService(
    private val fpsakClient: FpsakClient,
    private val pdlFacade: PdlFacade,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val FPSAK_TIMER = "fpsak.response.time"
    }

    @Cacheable(AKTOER_FOR_SAK)
    @Retryable
    fun getPersongalleriFnrListForSak(saksnummer: String): List<String> {
        val aktoerIdList = meterRegistry.timedCall(FPSAK_TIMER, ::getPersongalleriFnrListForSak.name) {
            fpsakClient.getAktoerForSak(
                bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithFpsakScope()}",
                saksnummer = saksnummer,
            )
        }
        return aktoerIdList.map { pdlFacade.getFoedselsnummerFromIdent(it) }
    }
}