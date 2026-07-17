package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.kodeverk.Tema
import no.nav.klage.lookup.api.external.person.Person
import no.nav.klage.lookup.api.person.PersonBulkResponse
import no.nav.klage.lookup.config.CacheConfiguration.Companion.AKTOER_ID_TO_FNR
import no.nav.klage.lookup.config.CacheConfiguration.Companion.IDENT_TO_AKTOER_ID
import no.nav.klage.lookup.config.CacheConfiguration.Companion.PERSON
import no.nav.klage.lookup.config.CacheConfiguration.Companion.PERSON_EXTERNAL
import no.nav.klage.lookup.service.kabalapi.KabalApiService
import no.nav.klage.lookup.service.pdl.PdlFacade
import no.nav.klage.lookup.service.pdl.PersonWithAllInfo
import no.nav.klage.lookup.service.pdl.toPerson
import no.nav.klage.lookup.service.pdl.toPersonWithAllInfo
import no.nav.klage.lookup.service.reprapi.ReprApiService
import no.nav.klage.lookup.service.skjerming.SkjermingService
import no.nav.klage.lookup.util.TokenUtil
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
    private val tokenUtil: TokenUtil,
    private val reprApiService: ReprApiService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val PERSON_TIMER = "person.response.time"
    }

    @Cacheable(PERSON)
    @Retryable
    fun getPersonWithAllInfo(fnr: String): PersonWithAllInfo {
        return meterRegistry.timedCall(PERSON_TIMER, "getPerson") {
            toPersonWithAllInfo(
                person = fnr to pdlFacade.getPerson(fnr),
                skjermet = skjermingService.skjermet(foedselsnr = fnr),
            )
        }
    }

    @Cacheable(
        cacheNames = [PERSON_EXTERNAL],
        key = "{#fnr, #tema, @tokenUtil.getSubjectFromTokenXToken()}"
    )
    @Retryable
    fun getPerson(fnr: String, tema: Tema?): Person {
        val currentUser = tokenUtil.getSubjectFromTokenXToken()
        if (currentUser != fnr) {
            if (tema == null) {
                throw FullmaktInputException("Trenger tema for å verifisere fullmakt")
            }

            if (!representasjonIsValid(
                    representasjonsgiverFnr = fnr,
                    tema = tema
                )
            ) {
                throw FullmaktMissingAccessException("Mangler fullmakt for oppgitt bruker")
            }
        }

        val pdlResults = pdlFacade.getPerson(fnr)
        return pdlResults.toPerson(fnr)
    }

    @Retryable
    fun getPersonBulk(fnrList: List<String>): PersonBulkResponse {
        return meterRegistry.timedCall(PERSON_TIMER, "getPersonBulk") {
            val pdlResults = pdlFacade.getPersonBulk(fnrList = fnrList)
            val resolvedIdents = pdlResults.mapNotNull { if (it.person != null) it.ident else null }
            val skjermingMap = if (resolvedIdents.isNotEmpty()) {
                skjermingService.skjermetBulk(foedselsnrList = resolvedIdents)
            } else {
                emptyMap()
            }

            val missingFromSkjerming = resolvedIdents.filterNot { skjermingMap.containsKey(it) }
            if (missingFromSkjerming.isNotEmpty()) {
                logger.warn("Skjerming bulk response missing ${missingFromSkjerming.size} of ${resolvedIdents.size} idents. See team-logs for details.")
                teamLogger.warn("Skjerming bulk response missing idents: $missingFromSkjerming")
            }

            val hits = mutableListOf<PersonWithAllInfo>()
            val misses = mutableListOf<String>()
            pdlResults.forEach { result ->
                val pdlPerson = result.person
                if (pdlPerson == null) {
                    logger.warn("No person returned from PDL for an ident in bulk request. See team-logs for details.")
                    teamLogger.warn("No person returned from PDL for ident=${result.ident}, code=${result.code}")
                    misses += result.ident
                } else {
                    hits += toPersonWithAllInfo(
                        person = result.ident to pdlPerson,
                        skjermet = skjermingMap[result.ident] ?: false,
                    )
                }
            }
            PersonBulkResponse(hits = hits, misses = misses)
        }
    }

    @Cacheable(AKTOER_ID_TO_FNR)
    @Retryable
    fun getFoedselsnummerFromIdent(ident: String): String {
        return pdlFacade.getFoedselsnummerFromIdent(ident = ident)
    }

    @Cacheable(IDENT_TO_AKTOER_ID)
    @Retryable
    fun getAktoerIdFromIdent(ident: String): String {
        return pdlFacade.getAktoerIdFromIdent(ident = ident)
    }

    fun evictPerson(fnr: String, protectionChange: Boolean) {
        val foundAndEvicted = cacheManager.getCache(PERSON)!!.evictIfPresent((fnr))

        if (foundAndEvicted) {
            logger.debug("Evicted person from cache.")
        }

        if (protectionChange) {
            logger.debug("Notify kabal-api about person changed due to 'protection' changed")
            kabalApiService.setPersonProtectionChanged(fnr)
        }
    }

    private fun representasjonIsValid(representasjonsgiverFnr: String, tema: Tema): Boolean {
        val usersRepresentasjonsforhold = reprApiService.kanRepresentere()
        val vergemaalExists = usersRepresentasjonsforhold.vergemaal.any {
            it.vergehaver == representasjonsgiverFnr && tema in it.skriverettigheter
        }
        val fullmaktExists = usersRepresentasjonsforhold.fullmakt.any {
            it.fullmaktsgiver == representasjonsgiverFnr && tema in it.skriverettigheter
        }

        return vergemaalExists || fullmaktExists
    }

    class FullmaktMissingAccessException(msg: String) : RuntimeException(msg)

    class FullmaktInputException(msg: String) : RuntimeException(msg)
}