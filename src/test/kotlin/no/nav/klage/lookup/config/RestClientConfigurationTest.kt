package no.nav.klage.lookup.config

import no.nav.klage.lookup.config.entraproxy.EntraProxyConfig
import no.nav.klage.lookup.config.fpsak.FpsakConfig
import no.nav.klage.lookup.config.kabalapi.KabalApiConfig
import no.nav.klage.lookup.config.microsoftgraph.MicrosoftGraphConfig
import no.nav.klage.lookup.config.nom.NomConfig
import no.nav.klage.lookup.config.pdl.PdlConfig
import no.nav.klage.lookup.config.regoppslag.RegoppslagConfig
import no.nav.klage.lookup.config.reprapi.ReprApiConfig
import no.nav.klage.lookup.config.skjerming.SkjermingConfig
import no.nav.klage.lookup.config.tilgangsmaskinen.TilgangsmaskinenConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.http.client.HttpClientSettings
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration
import org.springframework.boot.restclient.RestClientCustomizer
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class RestClientConfigurationTest {
    @Test
    fun `global timeout settings are applied to every rest client`() {
        contextRunner().run { context ->
            assertThat(context).hasNotFailed()
            val settings = context.getBean(HttpClientSettings::class.java)
            assertThat(settings.connectTimeout).isEqualTo(Duration.ofSeconds(3))
            assertThat(settings.readTimeout).isEqualTo(Duration.ofSeconds(5))
            assertThat(context.getBean(CountingRestClientCustomizer::class.java).invocations.get()).isEqualTo(10)
        }
    }

    @Test
    fun `timeout settings can be overridden by the environment`() {
        contextRunner()
            .withPropertyValues(
                "spring.http.clients.connect-timeout=4s",
                "spring.http.clients.read-timeout=7s",
            ).run { context ->
                assertThat(context).hasNotFailed()
                val settings = context.getBean(HttpClientSettings::class.java)
                assertThat(settings.connectTimeout).isEqualTo(Duration.ofSeconds(4))
                assertThat(settings.readTimeout).isEqualTo(Duration.ofSeconds(7))
            }
    }

    private fun contextRunner() =
        ApplicationContextRunner()
            .withInitializer(ConfigDataApplicationContextInitializer())
            .withConfiguration(
                AutoConfigurations.of(
                    HttpClientAutoConfiguration::class.java,
                    RestClientAutoConfiguration::class.java,
                ),
            ).withUserConfiguration(
                TestConfiguration::class.java,
                MicrosoftGraphConfig::class.java,
                NomConfig::class.java,
                ReprApiConfig::class.java,
                FpsakConfig::class.java,
                KabalApiConfig::class.java,
                PdlConfig::class.java,
                TilgangsmaskinenConfig::class.java,
                EntraProxyConfig::class.java,
                RegoppslagConfig::class.java,
                SkjermingConfig::class.java,
            ).withPropertyValues(
                "NOM_BASE_URL=http://localhost",
                "FPSAK_BASE_URL=http://localhost",
                "PDL_BASE_URL=http://localhost",
                "ENTRA_PROXY_BASE_URL=http://localhost",
                "REGOPPSLAG_URL=http://localhost",
            )

    class TestConfiguration {
        @org.springframework.context.annotation.Bean
        fun countingRestClientCustomizer() = CountingRestClientCustomizer()
    }

    class CountingRestClientCustomizer : RestClientCustomizer {
        val invocations = AtomicInteger()

        override fun customize(builder: org.springframework.web.client.RestClient.Builder) {
            invocations.incrementAndGet()
        }
    }
}
