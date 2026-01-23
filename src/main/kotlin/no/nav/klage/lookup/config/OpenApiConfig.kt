package no.nav.klage.lookup.config

import no.nav.klage.lookup.api.access.AccessToPersonController
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun api(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .packagesToScan(AccessToPersonController::class.java.packageName)
            .group("access")
            .pathsToMatch("/**")
            .build()
    }
}