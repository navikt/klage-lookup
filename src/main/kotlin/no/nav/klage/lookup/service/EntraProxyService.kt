package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.lookup.config.CacheConfiguration.Companion.USERS_GROUPS
import no.nav.klage.lookup.config.entraproxy.EntraProxyAnsatt
import no.nav.klage.lookup.config.entraproxy.EntraProxyInterface
import no.nav.klage.lookup.config.entraproxy.EntraProxyRolle
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
            throw e
        }

        return groupMembers
    }

    //TODO: Skal dette caches? Trenger vi annen innstilling enn standard?
    @Cacheable(USERS_GROUPS)
    @Retryable
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
            throw e
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