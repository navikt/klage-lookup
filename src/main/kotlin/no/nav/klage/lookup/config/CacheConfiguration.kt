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
        const val USER_SLUTTDATO = "userSluttdato"
        const val GROUP_MEMBERS = "groupMembers"
        const val ANSATTE_IN_ENHET = "ansatteInEnhet"
        const val PERSON = "person"
        const val SKJERMET = "skjermet"
        const val AKTOER_FOR_SAK = "aktoerForSak"
        const val IDENT_TO_FNR = "aktoerIdToFnr"
        const val IDENT_TO_AKTOER_ID = "identToAktoerId"
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()

        val standardConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(standardTTLSeconds.toLong()))

        val fourHoursConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(4))

        val oneWeekConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofDays(7))

        val oneMonthConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofDays(30))

        return RedisCacheManager.builder(redisConnectionFactory)
            .enableStatistics()
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(ACCESS_TO_PERSON, standardConfig)
            .withCacheConfiguration(USERS_GROUPS, standardConfig)
            .withCacheConfiguration(USER_INFO, standardConfig)
            .withCacheConfiguration(USER_SLUTTDATO, standardConfig)
            .withCacheConfiguration(GROUP_MEMBERS, standardConfig)
            .withCacheConfiguration(ANSATTE_IN_ENHET, standardConfig)
            .withCacheConfiguration(PERSON, fourHoursConfig)
            .withCacheConfiguration(IDENT_TO_FNR, oneWeekConfig)
            .withCacheConfiguration(IDENT_TO_AKTOER_ID, oneWeekConfig)
            .withCacheConfiguration(AKTOER_FOR_SAK, oneMonthConfig)
            .withCacheConfiguration(SKJERMET, oneMonthConfig)
            .build()
    }
}