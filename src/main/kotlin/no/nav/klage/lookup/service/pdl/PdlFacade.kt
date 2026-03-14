package no.nav.klage.lookup.service.pdl

import no.nav.klage.lookup.service.pdl.graphql.*
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.stereotype.Component

@Component
class PdlFacade(
    private val pdlClient: PdlService,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    fun getPerson(fnr: String): PdlPerson {
        return pdlClient.getPerson(
            bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithPdlScope()}",
            query = hentPersonQuery(fnr)
        ).getPersonOrThrowError(fnr)
    }

    fun getPersonBulk(fnrList: List<String>): List<HentPersonBolkResult> {
        return pdlClient.getPersonBulk(
            bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithPdlScope()}",
            query = hentPersonBulkQuery(fnrList)
        ).getResultsOrLogError()
    }

    fun getFoedselsnummerFromIdent(ident: String): String {
        val query = hentFolkeregisterIdentQuery(ident = ident)
        return getIdent(query = query)
    }

    fun getAktorIdFromIdent(ident: String): String {
        val query = hentAktorIdQuery(ident = ident)
        return getIdent(query = query)
    }

    private fun HentPersonResponse.getPersonOrThrowError(fnr: String): PdlPerson =
        if (this.errors.isNullOrEmpty() && this.data != null && this.data.hentPerson != null) {
            this.data.hentPerson
        } else {
            logger.warn("Errors returned from PDL or person not found. See team-logs for details.")
            teamLogger.warn("Errors returned for hentPerson($fnr) from PDL: ${this.errors}")
            if (this.errors?.any { it.extensions.code == "not_found" } == true) {
                throw PDLPersonNotFoundException("Fant ikke personen i PDL")
            }
            throw PDLErrorException("Klarte ikke å hente person fra PDL")
        }

    private fun HentPersonBolkResponse.getResultsOrLogError(): List<HentPersonBolkResult> =
        if (this.errors.isNullOrEmpty() && this.data != null && this.data.hentPersonBolk != null) {
            this.data.hentPersonBolk
        } else {
            logger.error("Errors returned from PDL or person not found. See team-logs for details.")
            teamLogger.error("Errors returned for hentPersonBolk from PDL: ${this.errors}")
            emptyList()
        }

    private fun getIdent(query: PersonGraphqlQuery): String {
        return pdlClient.getIdent(
            bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithPdlScope()}",
            query = query,
        ).data?.hentIdenter?.identer?.firstOrNull()?.ident
            ?: throw PDLErrorException("Klarte ikke å hente person fra PDL")
    }
}