package no.nav.klage.lookup.config.entraproxy

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class EntraProxyConfig {
    @Bean
    fun entraProxyClient(
        restClientBuilder: RestClient.Builder,
        @Value($$"${ENTRA_PROXY_BASE_URL}")
        entraProxyUrl: String,
    ): EntraProxyClient {
        val restClient = restClientBuilder.baseUrl(entraProxyUrl).build()

        // Create factory for client proxies
        val proxyFactory =
            HttpServiceProxyFactory
                .builder()
                .exchangeAdapter(RestClientAdapter.create(restClient))
                .build()

        return proxyFactory.createClient<EntraProxyClient>()
    }
}
