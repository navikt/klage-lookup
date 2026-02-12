package no.nav.klage.lookup.config

import no.nav.klage.lookup.config.entraproxy.EntraProxyEnhet
import no.nav.klage.lookup.config.entraproxy.EntraProxyUtvidetAnsatt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest
import org.springframework.cache.get
import org.springframework.context.annotation.Import
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ActiveProfiles("local")
@DataRedisTest
@Testcontainers
@Import(CacheConfiguration::class)
class CacheConfigurationTest {

    companion object {
        @Container
        val redis: GenericContainer<*> = GenericContainer(DockerImageName.parse("valkey/valkey:8"))
            .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }

    @Autowired
    private lateinit var cacheManager: RedisCacheManager

    @Test
    @Order(1)
    fun `cacheManager is configured as RedisCacheManager`() {
        assertThat(cacheManager).isInstanceOf(RedisCacheManager::class.java)
    }

    @Test
    @Order(2)
    fun `all expected caches are configured`() {
        val cacheNames = cacheManager.cacheNames

        assertThat(cacheNames).containsExactlyInAnyOrder(
            CacheConfiguration.ACCESS_TO_PERSON,
            CacheConfiguration.USERS_GROUPS,
            CacheConfiguration.USER_INFO,
            CacheConfiguration.ANSATTE_IN_ENHET
        )
    }

    @Test
    @Order(3)
    fun `cache stores and retrieves values`() {
        val cache = cacheManager.getCache(CacheConfiguration.ANSATTE_IN_ENHET)
        assertThat(cache).isNotNull

        val key = "testKey"
        val value = "testValue"

        cache!!.put(key, value)
        val retrieved = cache.get<String>(key)

        assertThat(retrieved).isEqualTo(value)
    }

    @Test
    @Order(4)
    fun `serialization works as expected`() {
        val cache = cacheManager.getCache(CacheConfiguration.USER_INFO)
        assertThat(cache).isNotNull

        val key = "currentTestKey"
        val value = EntraProxyUtvidetAnsatt(
            navIdent = "navIdent",
            visningNavn = "visningNavn",
            fornavn = "fornavn",
            etternavn = "etternavn",
            epost = "epost",
            enhet = EntraProxyEnhet(
                enhetnummer = "enhetnummer",
                navn = "navn"
            ),
            tIdent = "tIdent"
        )
        cache!!.put(key, value)
        assertThat(cache.get<EntraProxyUtvidetAnsatt>(key)).isEqualTo(value)
    }

    @Test
    @Order(5)
    fun `cache evicts values after TTL`() {
        val cache = cacheManager.getCache(CacheConfiguration.ACCESS_TO_PERSON)
        assertThat(cache).isNotNull

        val key = "testKey"
        val value = "testValue"
        cache!!.evict(key)
        cache.put(key, value)
        assertThat(cache.get<String>(key)).isEqualTo(value)

        Thread.sleep(2200)
        assertNull(cache.get<String>(key))
    }
}