package no.nav.klage.lookup.config.reprapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class ReprApiConfig {

    @Bean
    fun reprApiClient(
        @Value($$"${REPR_API_BASE_URL}")
        reprApiBaseUrl: String,
    ): ReprApiClient {
        val restClient = RestClient.create(reprApiBaseUrl)

        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<ReprApiClient>()
    }
}

