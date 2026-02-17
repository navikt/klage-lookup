package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.lookup.config.CacheConfiguration.Companion.ANSATTE_IN_ENHET
import no.nav.klage.lookup.config.CacheConfiguration.Companion.GROUP_MEMBERS
import no.nav.klage.lookup.config.CacheConfiguration.Companion.USERS_GROUPS
import no.nav.klage.lookup.config.CacheConfiguration.Companion.USER_INFO
import no.nav.klage.lookup.config.EnhetNotFoundException
import no.nav.klage.lookup.config.GroupNotFoundException
import no.nav.klage.lookup.config.UserNotFoundException
import no.nav.klage.lookup.config.entraproxy.EntraProxyAnsatt
import no.nav.klage.lookup.config.entraproxy.EntraProxyInterface
import no.nav.klage.lookup.config.entraproxy.EntraProxyRolle
import no.nav.klage.lookup.config.entraproxy.EntraProxyUtvidetAnsatt
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class EntraProxyService(
    private val entraProxyInterface: EntraProxyInterface,
    private val meterRegistry: MeterRegistry,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val ENTRAPROXY_TIMER = "entraproxy.response.time"
    }

    @Cacheable(GROUP_MEMBERS)
    @Retryable(
        excludes = [GroupNotFoundException::class]
    )
    fun getGroupMembers(gruppeNavn: String): List<EntraProxyAnsatt> {
        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithEntraProxyScope()}"
        }
        val groupMembers = try {
            timedCall(ENTRAPROXY_TIMER, "getGroupMembers") {
                entraProxyInterface.getGroupMembers(
                    bearerToken = bearerToken,
                    gruppeNavn = gruppeNavn
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve members of group '$gruppeNavn'", e)
            throw GroupNotFoundException("Could not find members in group '$gruppeNavn'")
        }

        return groupMembers
    }

    @Cacheable(USER_INFO)
    @Retryable(
        excludes = [UserNotFoundException::class]
    )
    fun getUserInfo(navIdent: String): EntraProxyUtvidetAnsatt {
        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithEntraProxyScope()}"
        }

        val userInfo = try {
            timedCall(ENTRAPROXY_TIMER, "getUserInfo") {
                entraProxyInterface.getAnsattInfo(
                    bearerToken = bearerToken,
                    navIdent = navIdent
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to retrieve user info for navIdent '$navIdent'", e)
            throw UserNotFoundException("User info for navIdent '$navIdent' could not be found")
        }

        return userInfo ?: throw UserNotFoundException("User info for navIdent '$navIdent' not found")
    }

    @Cacheable(ANSATTE_IN_ENHET)
    @Retryable(
        excludes = [EnhetNotFoundException::class]
    )
    fun getAnsatteInEnhet(enhetsnummer: String): List<EntraProxyAnsatt> {
        val useObo = tokenUtil.getIdent() != null
        val bearerToken = if (useObo) {
            "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope()}"
        } else {
            "Bearer ${tokenUtil.getAppAccessTokenWithEntraProxyScope()}"
        }

        val ansattList = try {
            timedCall(ENTRAPROXY_TIMER, "ansatteInEnhet") {
                entraProxyInterface.getAnsatteInEnhet(
                    bearerToken = bearerToken,
                    enhetsnummer = enhetsnummer
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve ansatte in enhet '$enhetsnummer'", e)
            throw EnhetNotFoundException("Ansatte in enhet '$enhetsnummer' could not be found")
        }

        return ansattList
    }

    @Cacheable(USERS_GROUPS)
    @Retryable(
        excludes = [UserNotFoundException::class]
    )
    fun getUsersGroups(navIdent: String): List<EntraProxyRolle> {
        val bearerToken = "Bearer ${tokenUtil.getAppAccessTokenWithEntraProxyScope()}"
        val usersRoles = try {
            timedCall(ENTRAPROXY_TIMER, "getUsersRoles") {
                entraProxyInterface.getAnsattTilganger(
                    bearerToken = bearerToken,
                    navIdent = navIdent,
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve roles for navIdent $navIdent", e)
            throw UserNotFoundException("User info for navIdent '$navIdent' not found")
        }
        return usersRoles
    }

    private fun <T> timedCall(timerName: String, method: String, block: () -> T): T {
        return Timer.builder(timerName)
            .tag("method", method)
            .register(meterRegistry)
            .recordCallable(block)!!
    }
}