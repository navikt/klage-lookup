package no.nav.klage.lookup.config.microsoftgraph

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class MicrosoftGraphClientConfig {

    @Bean
    fun microsoftGraphClient(
        @Value($$"${MICROSOFT_GRAPH_URL}")
        microsoftGraphUrl: String
    ): MicrosoftGraphInterface {
        val restClient = RestClient.create(microsoftGraphUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<MicrosoftGraphInterface>()
    }
}