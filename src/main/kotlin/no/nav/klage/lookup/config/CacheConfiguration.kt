package no.nav.klage.lookup.config


import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfiguration(
    @Value($$"${STANDARD_TTL_SECONDS}")
    private val standardTTLSeconds: Int,
) {

    companion object {
        const val ACCESS_TO_PERSON = "accessToPerson"
        const val USERS_GROUPS = "usersGroups"
        const val USER_INFO = "userInfo"
        const val ANSATTE_IN_ENHET = "ansatteInEnhet"
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()

        val standardConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(standardTTLSeconds.toLong()))

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(ACCESS_TO_PERSON, standardConfig)
            .withCacheConfiguration(USERS_GROUPS, standardConfig)
            .withCacheConfiguration(USER_INFO, standardConfig)
            .withCacheConfiguration(ANSATTE_IN_ENHET, standardConfig)
            .build()
    }
}