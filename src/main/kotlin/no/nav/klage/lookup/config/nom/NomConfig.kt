package no.nav.klage.lookup.config.nom

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class NomClientConfiguration {

    @Bean
    fun nomWebClient(
        @Value($$"${NOM_BASE_URL}")
        nomUrl: String
    ): NomClient {
        val restClient = RestClient.create(nomUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<NomClient>()
    }
}
