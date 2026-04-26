package no.nav.klage.lookup.service.kabalapi

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.config.kabalapi.KabalApiClient
import no.nav.klage.lookup.config.kabalapi.KabalApiRequest
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.timedCall
import org.springframework.stereotype.Service

@Service
class KabalApiService(
    private val kabalApiClient: KabalApiClient,
    private val tokenUtil: TokenUtil,
    private val meterRegistry: MeterRegistry,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val KABAL_API_TIMER = "kabalapi.response.time"
    }

    fun setPersonProtectionChanged(foedselsnr: String) {
        meterRegistry.timedCall(KABAL_API_TIMER, "setPersonProtectionChanged") {
            kabalApiClient.setPersonProtectionChanged(
                bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithKabalApiScope()}",
                query = KabalApiRequest(foedselsnummer = foedselsnr),
            )
        }
    }
}