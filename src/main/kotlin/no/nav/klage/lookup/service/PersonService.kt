package no.nav.klage.lookup.service

import no.nav.klage.lookup.api.common.Sak
import no.nav.klage.lookup.config.CacheConfiguration
import no.nav.klage.lookup.config.fpsak.FpsakService
import no.nav.klage.lookup.service.pdl.PdlFacade
import no.nav.klage.lookup.service.pdl.Person
import no.nav.klage.lookup.service.pdl.graphql.PdlPerson
import no.nav.klage.lookup.service.pdl.toPerson
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import no.nav.klage.lookup.util.shouldCheckFamilyMembers
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlFacade: PdlFacade,
    private val fpsakService: FpsakService,
//    private val cacheManager: RedisCacheManager,
    private val tokenUtil: TokenUtil,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }
    @Cacheable(CacheConfiguration.PERSON)
    fun getPerson(fnr: String, sak: Sak?): Person {
        val extraUsersToCheck = if (shouldCheckFamilyMembers(sak)) {
            logger.debug("Checking for family members for sak {}", sak!!.sakId)
            val aktoerIdList = fpsakService.getAktoerForSak(
                bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithFpsakScope()}",
                saksnummer = sak.sakId,
            )

            val fnrList = aktoerIdList.map { pdlFacade.getFoedselsnummerFromIdent(it) }

            if (fnrList.size != aktoerIdList.size) {
                logger.error("Number of aktør IDs (${aktoerIdList.size}) does not match number of fnr IDs (${fnrList.size}) for sak ${sak.sakId}. This may indicate an issue with the data.")
            }

            val extraUsersFnrList = fnrList - fnr
            if (extraUsersFnrList.isEmpty()) {
                logger.debug("No extra users found for sak ${sak.sakId}.")
            } else {
                logger.debug("Found ${fnrList.size} extra ident for sak ${sak.sakId}")
            }
            extraUsersFnrList
        } else {
            emptyList()
        }

        val people: Map<String, PdlPerson> = if (extraUsersToCheck.isNotEmpty()) {
            //fetch all in bulk, including the main person, to minimize number of calls to PDL
            val fnrList = extraUsersToCheck + fnr
            val pdlOutput = pdlFacade.getPersonBulk(fnrList = fnrList)
            pdlOutput.mapNotNull { hentPersonBolkResult ->
                val pdlPerson = hentPersonBolkResult.person
                if (pdlPerson != null) {
                    hentPersonBolkResult.ident to pdlPerson
                } else {
                    logger.warn("Person with ident ${hentPersonBolkResult.ident} not found in PDL. This person will be skipped when checking family relations for sak ${sak?.sakId}.")
                    null
                }
            }.toMap()
        } else {
            mapOf(fnr to pdlFacade.getPerson(fnr))
        }

        return toPerson(
            person = fnr to people[fnr]!!,
            relevantFamilyMembers = people.filterKeys { it != fnr }
        )
    }

//    fun fillPersonCache(fnrList: List<String>) {
//        val personCache = cacheManager.getCache(CacheConfiguration.PERSON)
//        fnrList.chunked(1000).forEach { fnrListChunk ->
//            val pdlOutput = pdlFacade.getPersonBulk(fnrList = fnrListChunk)
//            pdlOutput.forEach { hentPersonBolkResult ->
//                val pdlPerson = hentPersonBolkResult.person
//                if (pdlPerson != null) {
//                    val person = pdlPerson.toPerson(hentPersonBolkResult.ident)
//                    personCache?.put(hentPersonBolkResult.ident, person)
//                }
//            }
//        }
//    }

    fun getFoedselsnummerFromIdent(ident: String): String {
        return pdlFacade.getFoedselsnummerFromIdent(ident = ident)
    }

    fun getAktorIdFromIdent(ident: String): String {
        return pdlFacade.getAktorIdFromIdent(ident = ident)
    }

}