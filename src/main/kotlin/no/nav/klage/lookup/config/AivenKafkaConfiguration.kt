package no.nav.klage.lookup.config

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.nav.klage.lookup.util.getLogger
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.CommonLoggingErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import java.time.Duration

@EnableKafka
@Configuration
class AivenKafkaConfiguration(
    @Value($$"${KAFKA_BROKERS}")
    private val kafkaBrokers: String,
    @Value($$"${KAFKA_TRUSTSTORE_PATH}")
    private val kafkaTruststorePath: String,
    @Value($$"${KAFKA_CREDSTORE_PASSWORD}")
    private val kafkaCredstorePassword: String,
    @Value($$"${KAFKA_KEYSTORE_PATH}")
    private val kafkaKeystorePath: String,
    @Value($$"${KAFKA_SCHEMA_REGISTRY}")
    private val kafkaSchemaRegistryUrl: String,
    @Value($$"${KAFKA_SCHEMA_REGISTRY_USER}")
    private val schemaRegistryUsername: String,
    @Value($$"${KAFKA_SCHEMA_REGISTRY_PASSWORD}")
    private val schemaRegistryPassword: String,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //Common config
    fun commonKafkaConfig() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers
    ) + securityConfig()

    //Consumer beans
    @Bean
    fun skjermedePersonerKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(skjermedePersonerConsumerFactory())
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.idleEventInterval = 3000L
        factory.setCommonErrorHandler(CommonLoggingErrorHandler())
        //Retry consumer/listener even if authorization fails at first
        factory.setContainerCustomizer { container ->
            container.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(10L))
        }

        return factory
    }

    @Bean
    fun leesahKafkaListenerContainerFactory(
        aivenSchemaRegistryClient: SchemaRegistryClient,
    ): ConcurrentKafkaListenerContainerFactory<String, GenericRecord> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, GenericRecord>()
        factory.setConsumerFactory(leesahConsumerFactory(aivenSchemaRegistryClient = aivenSchemaRegistryClient))
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.setCommonErrorHandler(CommonLoggingErrorHandler())
        factory.containerProperties.idleEventInterval = 3000L
        //Retry consumer/listener even if authorization fails at first
        factory.setContainerCustomizer { container ->
            container.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(10L))
        }

        return factory
    }

    @Bean
    fun skjermedePersonerConsumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(skjermedePersonerConsumerProps())
    }

    @Bean
    fun leesahConsumerFactory(aivenSchemaRegistryClient: SchemaRegistryClient): ConsumerFactory<String, Any> {
        return DefaultKafkaConsumerFactory(
            getAvroConsumerProps(),
            StringDeserializer(),
            KafkaAvroDeserializer(aivenSchemaRegistryClient)
        )
    }

    private fun skjermedePersonerConsumerProps(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to "klage-lookup-skjermede-personer",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            "spring.deserializer.key.delegate.class" to StringDeserializer::class.java,
            "spring.deserializer.value.delegate.class" to StringDeserializer::class.java
        ) + commonKafkaConfig()
    }

    private fun getAvroConsumerProps(): Map<String, Any> {
        return mapOf(
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to kafkaSchemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to false,
            ConsumerConfig.GROUP_ID_CONFIG to "klage-lookup-leesah",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
        ) + commonKafkaConfig()
    }

    @Bean
    fun aivenSchemaRegistryClient(): SchemaRegistryClient =
        CachedSchemaRegistryClient(
            kafkaSchemaRegistryUrl,
            20,
            mapOf(
                KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
                KafkaAvroDeserializerConfig.USER_INFO_CONFIG to "$schemaRegistryUsername:$schemaRegistryPassword",
            ),
        )

    private fun securityConfig() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to kafkaTruststorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to kafkaKeystorePath,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to kafkaCredstorePassword,
    )
}