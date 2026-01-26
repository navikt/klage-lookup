package no.nav.klage.lookup.config.fpsak

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class FpsakConfig {

    @Bean
    fun fpsakService(
        @Value("\${FPSAK_BASE_URL}")
        fpsakUrl: String
    ): FpsakService {
        val restClient = RestClient.create(fpsakUrl)

        // Create factory for client proxies
        val proxyFactory = HttpServiceProxyFactory.builder()
            .exchangeAdapter(RestClientAdapter.create(restClient))
            .build()

        return proxyFactory.createClient<FpsakService>()
    }
}
