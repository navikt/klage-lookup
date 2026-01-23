package no.nav.klage.lookup.config.tilgangsmaskinen

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class TilgangsmaskinenConfig{

    @Bean
    fun tilgangsmaskinenService(
        @Value($$"${TILGANGSMASKINEN_BASE_URL}")
        tilgangsmaskinenUrl: String
    ): TilgangsmaskinenService {
        val restClient = RestClient.create(tilgangsmaskinenUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<TilgangsmaskinenService>()
    }
}