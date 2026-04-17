package no.nav.klage.lookup.service.nom

import no.nav.klage.lookup.config.CacheConfiguration.Companion.USER_SLUTTDATO
import no.nav.klage.lookup.config.nom.NomClient
import no.nav.klage.lookup.service.nom.graphql.*
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component

@Component
class NomFacade(
    private val nomClient: NomClient,
    private val tokenUtil: TokenUtil,
    private val cacheManager: CacheManager,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @Cacheable(USER_SLUTTDATO)
    @Retryable(
        excludes = [NomAnsattNotFoundException::class]
    )
    fun getAnsattInfoFromNom(navIdent: String): Ansatt {
        val ansatt = nomClient.hentAnsatt(
            bearerToken = tokenUtil.getAppAccessTokenWithNomScope(),
            getAnsattQuery(navIdent = navIdent)
        )
        return ansatt.getRessursOrThrowError(navIdent)
    }

    fun getAnsatteInfoFromNom(navIdentList: List<String>): GetAnsatteResponse {
        val cache = cacheManager.getCache(USER_SLUTTDATO) ?: return fetchAnsatteFromNom(navIdentList)

        val cached = mutableMapOf<String, Ansatt>()
        val uncached = mutableListOf<String>()

        // Separate cached and uncached idents
        for (navIdent in navIdentList.distinct()) {
            val cachedValue = cache.get(navIdent, Ansatt::class.java)
            if (cachedValue != null) {
                cached[navIdent] = cachedValue
            } else {
                uncached.add(navIdent)
            }
        }

        // Fetch uncached from NOM and update cache
        val newlyFetched = if (uncached.isNotEmpty()) {
            val response = fetchAnsatteFromNom(uncached)
            response.data?.ressurser?.forEach { ressurs ->
                ressurs.ressurs?.let { ansatt ->
                    cache.put(ressurs.id, ansatt)
                }
            }
            response
        } else {
            GetAnsatteResponse(data = GetAnsatteDataWrapper(ressurser = emptyList()), errors = null)
        }

        // Combine cached and newly fetched
        val combinedRessurser = mutableListOf<Ressurs>()
        cached.forEach { (navIdent, ansatt) ->
            combinedRessurser.add(Ressurs(id = navIdent, ressurs = ansatt))
        }
        newlyFetched.data?.ressurser?.let { combinedRessurser.addAll(it) }

        return GetAnsatteResponse(
            data = GetAnsatteDataWrapper(ressurser = combinedRessurser),
            errors = newlyFetched.errors
        )
    }

    private fun fetchAnsatteFromNom(navIdentList: List<String>): GetAnsatteResponse {
        return nomClient.hentAnsatte(
            bearerToken = tokenUtil.getAppAccessTokenWithNomScope(),
            getAnsatteQuery(navIdenter = navIdentList)
        )
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