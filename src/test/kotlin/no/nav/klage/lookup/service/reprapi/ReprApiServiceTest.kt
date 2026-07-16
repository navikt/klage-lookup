package no.nav.klage.lookup.service.reprapi

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.lookup.api.external.repr.*
import no.nav.klage.lookup.config.CacheConfiguration.Companion.KAN_REPRESENTERE
import no.nav.klage.lookup.config.reprapi.ReprApiClient
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.get
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
                    leserettigheter = setOf("SYK"),
                    skriverettigheter = setOf("SYK"),
                )
            ),
            vergemaal = listOf(
                VergemaalsforholdDto(
                    vergehaver = "12345678901",
                    verge = "10987654321",
                    leserettigheter = setOf("SYK"),
                    skriverettigheter = setOf("SYK"),
                )
            ),
        )

        every { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() } returns "token-x-obo"
        every { reprApiClient.kanRepresentere("Bearer token-x-obo") } returns expected

        val actual = reprApiService.kanRepresentere()

        assertThat(actual).isEqualTo(expected.toRepresentasjonsforholdView())
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
        AnnotationConfigApplicationContext(CachingTestConfig::class.java).use { context ->
            val service = context.getBean(ReprApiService::class.java)
            val client = context.getBean(ReprApiClient::class.java)
            val tokenUtil = context.getBean(TokenUtil::class.java)
            val cacheManager = context.getBean(CacheManager::class.java)

            val expectedFirstSubjectResponse = RepresentasjonsforholdDto(
                fullmakt = listOf(
                    FullmaktsforholdDto(
                        fullmaktsgiver = "12345678901",
                        fullmektig = "10987654321",
                        leserettigheter = setOf("SYK"),
                        skriverettigheter = setOf("SYK"),
                    )
                ),
                vergemaal = emptyList(),
            )

            val expectedSecondSubjectResponse = RepresentasjonsforholdDto(
                fullmakt = emptyList(),
                vergemaal = listOf(
                    VergemaalsforholdDto(
                        vergehaver = "01987654321",
                        verge = "12012345678",
                        leserettigheter = setOf("FOR"),
                        skriverettigheter = emptySet(),
                    )
                ),
            )

            every { tokenUtil.getSubjectFromTokenXToken() } returns "12345678901"
            every { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() } returns "token-x-obo-1"
            every { client.kanRepresentere("Bearer token-x-obo-1") } returns expectedFirstSubjectResponse

            val firstResponse = service.kanRepresentere()
            assertThat(firstResponse).isEqualTo(expectedFirstSubjectResponse.toRepresentasjonsforholdView())

            val secondResponse = service.kanRepresentere()
            assertThat(secondResponse).isEqualTo(expectedFirstSubjectResponse.toRepresentasjonsforholdView())

            assertThat(
                cacheManager.getCache(KAN_REPRESENTERE)
                    ?.get<RepresentasjonsforholdView>("kanRepresentere:12345678901")
            ).isEqualTo(expectedFirstSubjectResponse.toRepresentasjonsforholdView())

            verify(exactly = 1) { client.kanRepresentere("Bearer token-x-obo-1") }

            every { tokenUtil.getSubjectFromTokenXToken() } returns "01987654321"
            every { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() } returns "token-x-obo-2"
            every { client.kanRepresentere("Bearer token-x-obo-2") } returns expectedSecondSubjectResponse

            val thirdResponse = service.kanRepresentere()
            assertThat(thirdResponse).isEqualTo(expectedSecondSubjectResponse.toRepresentasjonsforholdView())

            val fourthResponse = service.kanRepresentere()
            assertThat(fourthResponse).isEqualTo(expectedSecondSubjectResponse.toRepresentasjonsforholdView())

            assertThat(
                cacheManager.getCache(KAN_REPRESENTERE)
                    ?.get<RepresentasjonsforholdView>("kanRepresentere:01987654321")
            ).isEqualTo(expectedSecondSubjectResponse.toRepresentasjonsforholdView())

            verify(exactly = 1) { client.kanRepresentere("Bearer token-x-obo-2") }

            every { client.kanRepresentere("Bearer token-x-obo-2") } returns expectedSecondSubjectResponse

            verify(exactly = 4) { tokenUtil.getSubjectFromTokenXToken() }
            verify(exactly = 2) { tokenUtil.getOnBehalfOfFromTokenXTokenWithReprApiScope() }
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

