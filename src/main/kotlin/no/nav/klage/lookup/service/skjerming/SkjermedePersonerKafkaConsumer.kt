package no.nav.klage.lookup.service.skjerming

import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

@Component
class SkjermedePersonerKafkaConsumer(
    private val skjermingService: SkjermingService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private val mapper =
            JsonMapper.builder().addModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
            ).build()
    }

    @KafkaListener(
        id = "klageSkjermedePersonerListener",
        idIsGroup = false,
        containerFactory = "skjermedePersonerKafkaListenerContainerFactory",
        topics = [$$"${SKJERMEDE_PERSONER_KAFKA_TOPIC}"],
    )
    fun listen(skjermetPersonRecord: ConsumerRecord<String, String?>) {
        runCatching {
            val foedselsnr = skjermetPersonRecord.key()
            //Handle tombstone
            if (skjermetPersonRecord.value() == null) {
                skjermingService.removeSkjermetPerson(foedselsnr)
            } else {
                val skjermetPerson = skjermetPersonRecord.value()!!.toSkjermetPerson()
                skjermingService.updateSkjermetPerson(foedselsnr, skjermetPerson)
            }
        }.onFailure {
            teamLogger.error("Failed to process skjermet person record", it)
            throw RuntimeException("Could not process skjermet person record. See more details in team-logs.")
        }
    }

    private fun String.toSkjermetPerson() = mapper.readValue(this, SkjermetPerson::class.java)
}
