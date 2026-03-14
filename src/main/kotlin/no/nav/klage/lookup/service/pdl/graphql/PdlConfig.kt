package no.nav.klage.lookup.service.pdl.graphql

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class PdlConfig{

    @Bean
    fun pdlService(
        @Value($$"${PDL_BASE_URL}")
        pdlUrl: String
    ): PdlService {
        val restClient = RestClient.create(pdlUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<PdlService>()
    }
}