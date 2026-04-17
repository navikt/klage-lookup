package no.nav.klage.lookup.service.persongalleri

import no.nav.klage.lookup.api.common.Sak
import no.nav.klage.lookup.api.persongalleri.PersongalleriResponse
import no.nav.klage.lookup.service.PersonService
import no.nav.klage.lookup.service.fpsak.FpsakService
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.shouldCheckFamilyMembers
import org.springframework.stereotype.Service

@Service
class PersongalleriService(
    private val fpsakService: FpsakService,
    private val personService: PersonService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getPersongalleri(sak: Sak): PersongalleriResponse {
        if (!shouldCheckFamilyMembers(sak)) {
            logger.debug("Sak ${sak.sakId} does not require checking for family members. Returning empty persongalleri.")
            return PersongalleriResponse(foedselsnummerList = emptyList())
        }

        val aktoerIdList = fpsakService.getAktoerForSak(saksnummer = sak.sakId)
        val foedselsnummerList = aktoerIdList.map { personService.getFoedselsnummerFromIdent(it) }
        return PersongalleriResponse(foedselsnummerList = foedselsnummerList)
    }

}