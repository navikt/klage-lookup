package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.config.CacheConfiguration.Companion.IDENT_TO_AKTOER_ID
import no.nav.klage.lookup.config.CacheConfiguration.Companion.IDENT_TO_FNR
import no.nav.klage.lookup.config.CacheConfiguration.Companion.PERSON
import no.nav.klage.lookup.service.kabalapi.KabalApiService
import no.nav.klage.lookup.service.pdl.PdlFacade
import no.nav.klage.lookup.service.pdl.Person
import no.nav.klage.lookup.service.pdl.toPerson
import no.nav.klage.lookup.service.skjerming.SkjermingService
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import no.nav.klage.lookup.util.timedCall
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlFacade: PdlFacade,
    private val skjermingService: SkjermingService,
    private val kabalApiService: KabalApiService,
    private val cacheManager: CacheManager,
    private val meterRegistry: MeterRegistry,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val PERSON_TIMER = "person.response.time"
    }

    @Cacheable(PERSON)
    @Retryable
    fun getPerson(fnr: String): Person {
        return meterRegistry.timedCall(PERSON_TIMER, "getPerson") {
            toPerson(
                person = fnr to pdlFacade.getPerson(fnr),
                skjermingService = skjermingService,
            )
        }
    }

    @Cacheable(IDENT_TO_FNR)
    @Retryable
    fun getFoedselsnummerFromIdent(ident: String): String {
        return pdlFacade.getFoedselsnummerFromIdent(ident = ident)
    }

    @Cacheable(IDENT_TO_AKTOER_ID)
    @Retryable
    fun getAktoerIdFromIdent(ident: String): String {
        return pdlFacade.getAktoerIdFromIdent(ident = ident)
    }

    fun evictPerson(fnr: String) {
        cacheManager.getCache(PERSON)!!.evict(fnr)
        kabalApiService.setPersonProtectionChanged(fnr)
    }
}