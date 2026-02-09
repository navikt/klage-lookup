package no.nav.klage.lookup.config


import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfiguration {

    companion object {
        const val ACCESS_TO_PERSON = "accessToPerson"
        const val USERS_GROUPS = "usersGroups"

        internal fun createRedisSerializer(): GenericJacksonJsonRedisSerializer {
            val typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("no.nav")
                .allowIfSubType("java.time")
                .allowIfSubType("java.math")
                .allowIfSubType("java.util.ArrayList")
                .allowIfBaseType(Collection::class.java)
                .allowIfBaseType(Map::class.java)
                .build()

            return GenericJacksonJsonRedisSerializer
                .builder()
                .enableDefaultTyping(typeValidator)
                .build()
        }
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        val serializer = createRedisSerializer()

        val standardConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(ACCESS_TO_PERSON, standardConfig)
            .withCacheConfiguration(USERS_GROUPS, standardConfig)
            .build()
    }
}