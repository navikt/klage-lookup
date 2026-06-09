package no.nav.klage.lookup.config.regoppslag

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class RegoppslagConfig {

    @Bean
    fun regoppslagClient(
        @Value($$"${REGOPPSLAG_URL}")
        regoppslagUrl: String,
        @Value($$"${BEHANDLINGSNUMMER}")
        behandlingsnummer: String,
    ): RegoppslagClient {

        val restClient = RestClient.builder()
            .baseUrl(regoppslagUrl)
            .defaultHeader("behandlingsnummer", behandlingsnummer)
            .build()

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<RegoppslagClient>()
    }
}