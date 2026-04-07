package no.nav.klage.lookup.config.skjerming

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class SkjermingConfig {

    @Bean
    fun skjermingService(
        @Value($$"${SKJERMING_BASE_URL}")
        skjermingUrl: String
    ): SkjermingClient {
        val restClient = RestClient.create(skjermingUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<SkjermingClient>()
    }
}