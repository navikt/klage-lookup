package no.nav.klage.lookup.config.kabalapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class KabalApiConfig {

    @Bean
    fun kabalApiClient(
        @Value($$"${KABAL_API_BASE_URL}")
        kabalApiUrl: String
    ): KabalApiClient {
        val restClient = RestClient.create(kabalApiUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<KabalApiClient>()
    }
}