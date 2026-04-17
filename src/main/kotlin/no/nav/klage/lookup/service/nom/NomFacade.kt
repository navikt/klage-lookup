package no.nav.klage.lookup.service.nom

import no.nav.klage.lookup.config.nom.NomClient
import no.nav.klage.lookup.service.nom.graphql.*
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.stereotype.Component

@Component
class NomFacade(
    private val nomClient: NomClient,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    fun getAnsattInfoFromNom(navIdent: String): Ansatt {
        val ansatt = nomClient.hentAnsatt(
            bearerToken = tokenUtil.getAppAccessTokenWithNomScope(),
            getAnsattQuery(navIdent = navIdent)
        )
        return ansatt.getRessursOrThrowError(navIdent)
    }

    fun getAnsatteInfoFromNom(navIdentList: List<String>): GetAnsatteResponse {
        val ansatte = nomClient.hentAnsatte(
            bearerToken = tokenUtil.getAppAccessTokenWithNomScope(),
            getAnsatteQuery(navIdenter = navIdentList)
        )
        return ansatte
    }

    private fun GetAnsattResponse.getRessursOrThrowError(navIdent: String): Ansatt =
        if (this.errors.isNullOrEmpty() && this.data != null && this.data.ressurs != null) {
            this.data.ressurs
        } else {
            logger.warn("Errors returned from Nom or person not found. See team-logs for details.")
            teamLogger.warn("Errors returned for getAnsattInfoFromNom($navIdent) from NOM: ${this.errors}")
            if (this.errors?.any { it.extensions.code == "not_found" } == true) {
                throw NomAnsattNotFoundException("Fant ikke ansatt i NOM")
            }
            throw NomErrorException("Klarte ikke aa hente ansatt fra NOM")
        }
}