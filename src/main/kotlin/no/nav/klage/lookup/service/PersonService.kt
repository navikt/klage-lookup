package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.lookup.api.common.Sak
import no.nav.klage.lookup.config.CacheConfiguration.Companion.PERSON
import no.nav.klage.lookup.config.fpsak.FpsakClient
import no.nav.klage.lookup.service.pdl.PdlFacade
import no.nav.klage.lookup.service.pdl.Person
import no.nav.klage.lookup.service.pdl.graphql.PdlPerson
import no.nav.klage.lookup.service.pdl.toPerson
import no.nav.klage.lookup.service.skjerming.SkjermingService
import no.nav.klage.lookup.util.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlFacade: PdlFacade,
    private val fpsakClient: FpsakClient,
    private val tokenUtil: TokenUtil,
    private val skjermingService: SkjermingService,
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
    fun getPerson(fnr: String, sak: Sak?): Person {
        return meterRegistry.timedCall(PERSON_TIMER, "getPerson") {
            val extraUsersToCheck = if (shouldCheckFamilyMembers(sak)) {
                logger.debug("Checking for family members for sak {}", sak!!.sakId)
                val aktoerIdList = fpsakClient.getAktoerForSak(
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
                    logger.debug("Found ${extraUsersFnrList.size} extra ident for sak ${sak.sakId}")
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
                        logger.warn("Person with ident *see team-logs* not found in PDL. This person will be skipped when checking family relations for sak ${sak?.sakId}.")
                        teamLogger.warn("Person with ident ${hentPersonBolkResult.ident} not found in PDL. This person will be skipped when checking family relations for sak ${sak?.sakId}.")
                        null
                    }
                }.toMap()
            } else {
                mapOf(fnr to pdlFacade.getPerson(fnr))
            }

            toPerson(
                person = fnr to people[fnr]!!,
                relevantFamilyMembers = people.filterKeys { it != fnr },
                skjermingService = skjermingService,
            )
        }
    }

    fun getFoedselsnummerFromIdent(ident: String): String {
        return pdlFacade.getFoedselsnummerFromIdent(ident = ident)
    }

    fun getAktoerIdFromIdent(ident: String): String {
        return pdlFacade.getAktoerIdFromIdent(ident = ident)
    }

}