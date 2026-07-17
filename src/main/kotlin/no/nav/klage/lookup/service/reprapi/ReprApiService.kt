package no.nav.klage.lookup.service.reprapi

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.api.external.repr.RepresentasjonsforholdView
import no.nav.klage.lookup.api.external.repr.toRepresentasjonsforholdView
import no.nav.klage.lookup.config.CacheConfiguration.Companion.KAN_REPRESENTERE
import no.nav.klage.lookup.config.reprapi.ReprApiClient
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ReprApiService(
    private val reprApiClient: ReprApiClient,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        private const val REPR_API_TIMER = "reprapi.response.time"
    }

    @Cacheable(value = [KAN_REPRESENTERE], key = "@tokenUtil.getSubjectFromTokenXToken()")
    fun kanRepresentere(): RepresentasjonsforholdView {
        return meterRegistry.timedCall(REPR_API_TIMER, ::kanRepresentere.name) {
            reprApiClient.kanRepresentere(
                bearerToken = "Bearer ${tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope()}",
            )
        }.toRepresentasjonsforholdView()
    }
}


