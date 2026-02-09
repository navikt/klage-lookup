package no.nav.klage.lookup.config


import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfiguration {

    companion object {
        const val ACCESS_TO_PERSON = "accessToPerson"
        const val USERS_GROUPS = "usersGroups"
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()

        val standardConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(fromSerializer(GenericJacksonJsonRedisSerializer(jacksonObjectMapper())))

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(ACCESS_TO_PERSON, standardConfig)
            .withCacheConfiguration(USERS_GROUPS, standardConfig)
            .build()
    }
}