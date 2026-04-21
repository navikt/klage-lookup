package no.nav.klage.lookup.service.pdl.kafka

import no.nav.klage.lookup.service.PersonService
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class LeesahConsumer(
    private val personService: PersonService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @KafkaListener(
        id = "klageLookupLeesahListener",
        idIsGroup = false,
        containerFactory = "leesahKafkaListenerContainerFactory",
        topics = [$$"${LEESAH_KAFKA_TOPIC}"],
    )
    fun listen(
        cr: ConsumerRecord<String, GenericRecord>,
    ) {
        processPersonhendelse(
            personhendelse = cr.value(),
        )
    }

    private fun processPersonhendelse(
        personhendelse: GenericRecord,
    ) {
        val fnr = personhendelse.fnr
        if (personhendelse.isRelevantForOurCache) {
            logger.debug("Personhendelse is possibly relevant for our cache. Evicting person from cache.")
            personService.evictPerson(fnr)
        }
    }
}