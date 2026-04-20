package no.nav.klage.lookup.config.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class PdlConfig {

    @Bean
    fun pdlClient(
        @Value($$"${PDL_BASE_URL}")
        pdlUrl: String,
    ): PdlClient {
        //https://behandlingskatalog.ansatt.nav.no/process/system/KABAL/478cac88-3dde-4abe-aab9-c0bb5c06c083?
        val restClient = RestClient.builder()
            .baseUrl(pdlUrl)
            .defaultHeader("TEMA", "KLA")
            .defaultHeader("behandlingsnummer", "B392")
            .build()

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<PdlClient>()
    }
}