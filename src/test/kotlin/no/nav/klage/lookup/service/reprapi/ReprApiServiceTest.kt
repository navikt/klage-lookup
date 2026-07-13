package no.nav.klage.lookup.service.reprapi

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.lookup.api.repr.FullmaktsforholdDto
import no.nav.klage.lookup.api.repr.RepresentasjonsforholdDto
import no.nav.klage.lookup.api.repr.VergemaalsforholdDto
import no.nav.klage.lookup.config.CacheConfiguration.Companion.KAN_REPRESENTERE
import no.nav.klage.lookup.config.reprapi.ReprApiClient
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class ReprApiServiceTest {

    private val reprApiClient = mockk<ReprApiClient>()
    private val tokenUtil = mockk<TokenUtil>()
    private val meterRegistry = SimpleMeterRegistry()

    private val reprApiService = ReprApiService(
        reprApiClient = reprApiClient,
        tokenUtil = tokenUtil,
        meterRegistry = meterRegistry,
    )

    @Test
    fun `kanRepresentere uses token exchange from TokenX with repr-api scope`() {
        val expected = RepresentasjonsforholdDto(
            fullmakt = listOf(
                FullmaktsforholdDto(
                    fullmaktsgiver = "12345678901",
                    fullmektig = "10987654321",
                    leserettigheter = setOf("KLAGE"),
                    skriverettigheter = setOf("KLAGE"),
                )
            ),
            vergemaal = listOf(
                VergemaalsforholdDto(
                    vergehaver = "12345678901",
                    verge = "10987654321",
                    leserettigheter = setOf("KLAGE"),
                    skriverettigheter = setOf("KLAGE"),
                )
            ),
        )

        every { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() } returns "token-x-obo"
        every { reprApiClient.kanRepresentere("Bearer token-x-obo") } returns expected

        val actual = reprApiService.kanRepresentere()

        assertThat(actual).isEqualTo(expected)
        verify(exactly = 1) { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() }
        verify(exactly = 1) { reprApiClient.kanRepresentere("Bearer token-x-obo") }
    }

    @Test
    fun `cacheKeyForKanRepresentere includes TokenX subject`() {
        every { tokenUtil.getSubjectFromTokenXToken() } returns "12345678901"

        val cacheKey = reprApiService.cacheKeyForKanRepresentere()

        assertThat(cacheKey).isEqualTo("kanRepresentere:12345678901")
    }

    @Test
    fun `kanRepresentere caches response separately per TokenX subject`() {
        //TODO: Juster denne testen, den er uoversiktlig nå.
        AnnotationConfigApplicationContext(CachingTestConfig::class.java).use { context ->
            val service = context.getBean(ReprApiService::class.java)
            val client = context.getBean(ReprApiClient::class.java)
            val tokenUtil = context.getBean(TokenUtil::class.java)
            val cacheManager = context.getBean(CacheManager::class.java)

            val firstSubjectResponse = RepresentasjonsforholdDto(
                fullmakt = listOf(
                    FullmaktsforholdDto(
                        fullmaktsgiver = "12345678901",
                        fullmektig = "10987654321",
                        leserettigheter = setOf("KLAGE"),
                        skriverettigheter = setOf("KLAGE"),
                    )
                ),
                vergemaal = emptyList(),
            )

            val secondSubjectResponse = RepresentasjonsforholdDto(
                fullmakt = emptyList(),
                vergemaal = listOf(
                    VergemaalsforholdDto(
                        vergehaver = "01987654321",
                        verge = "12012345678",
                        leserettigheter = setOf("INNSYN"),
                        skriverettigheter = emptySet(),
                    )
                ),
            )

            every { tokenUtil.getSubjectFromTokenXToken() } returnsMany listOf(
                "12345678901",
                "12345678901",
                "01987654321",
                "01987654321",
            )
            every { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() } returnsMany listOf(
                "token-x-obo-1",
                "token-x-obo-2",
            )
            every { client.kanRepresentere("Bearer token-x-obo-1") } returns firstSubjectResponse
            every { client.kanRepresentere("Bearer token-x-obo-2") } returns secondSubjectResponse

            val firstSubjectFirstCall = service.kanRepresentere()
            val firstSubjectSecondCall = service.kanRepresentere()
            val secondSubjectFirstCall = service.kanRepresentere()
            val secondSubjectSecondCall = service.kanRepresentere()

            assertThat(firstSubjectFirstCall).isEqualTo(firstSubjectResponse)
            assertThat(firstSubjectSecondCall).isEqualTo(firstSubjectResponse)
            assertThat(secondSubjectFirstCall).isEqualTo(secondSubjectResponse)
            assertThat(secondSubjectSecondCall).isEqualTo(secondSubjectResponse)
            assertThat(
                cacheManager.getCache(KAN_REPRESENTERE)
                    ?.get("kanRepresentere:12345678901", RepresentasjonsforholdDto::class.java)
            ).isEqualTo(firstSubjectResponse)
            assertThat(
                cacheManager.getCache(KAN_REPRESENTERE)
                    ?.get("kanRepresentere:01987654321", RepresentasjonsforholdDto::class.java)
            ).isEqualTo(secondSubjectResponse)

            verify(exactly = 4) { tokenUtil.getSubjectFromTokenXToken() }
            verify(exactly = 2) { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() }
            verify(exactly = 1) { client.kanRepresentere("Bearer token-x-obo-1") }
            verify(exactly = 1) { client.kanRepresentere("Bearer token-x-obo-2") }
        }
    }

    @Configuration
    @EnableCaching
    open class CachingTestConfig {
        @Bean
        open fun reprApiClient(): ReprApiClient = mockk()

        @Bean
        open fun tokenUtil(): TokenUtil = mockk()

        @Bean
        open fun meterRegistry() = SimpleMeterRegistry()

        @Bean
        open fun cacheManager(): CacheManager = ConcurrentMapCacheManager(KAN_REPRESENTERE)

        @Bean
        open fun reprApiService(
            reprApiClient: ReprApiClient,
            tokenUtil: TokenUtil,
        ) = ReprApiService(
            reprApiClient = reprApiClient,
            tokenUtil = tokenUtil,
            meterRegistry = meterRegistry(),
        )
    }
}

