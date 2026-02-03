package no.nav.klage.lookup.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.klage.lookup.config.entraproxy.EntraProxyService
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val tokenUtil: TokenUtil,
    private val entraProxyService: EntraProxyService,
    private val meterRegistry: MeterRegistry,
    @Value("\${KLAGE_ADMIN_GROUP_NAME}")
    private val klageAdminGroupName: String,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
        private const val ENTRAPROXY_TIMER = "entraproxy.response.time"
    }

    fun loggedInUserIsKabalAdmin(): Boolean {
        val navIdent = tokenUtil.getIdent()
        val adminRoleMembers = timedCall(ENTRAPROXY_TIMER, "getGroupMembersWithObo") {
            entraProxyService.getGroupMembersWithObo(
                bearerToken = "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithEntraProxyScope()}",
                gruppeNavn = klageAdminGroupName
            )
        }
        logger.debug("Got ${adminRoleMembers.size} members of admin group '$klageAdminGroupName'")
        return adminRoleMembers.map { it.navIdent }.contains(navIdent)
    }

    private fun <T> timedCall(timerName: String, method: String, block: () -> T): T {
        return Timer.builder(timerName)
            .tag("method", method)
            .register(meterRegistry)
            .recordCallable(block)!!
    }
}