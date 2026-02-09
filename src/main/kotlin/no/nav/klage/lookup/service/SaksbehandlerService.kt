package no.nav.klage.lookup.service

import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val tokenUtil: TokenUtil,
    private val entraProxyService: EntraProxyService,
    @Value($$"${KLAGE_ADMIN_GROUP_NAME}")
    private val klageAdminGroupName: String,
    @Value($$"${KLAGE_KABAL_SAKSBEHANDLER_GROUP_NAME}")
    private val kabalSaksbehandlerGroupName: String,
    @Value($$"${KLAGE_KABAL_ROL_GROUP_NAME}")
    private val kabalROLGroupName: String,
    @Value($$"${KLAGE_KABAL_KROL_GROUP_NAME}")
    private val kabalKROLGroupName: String,

    @Value($$"${KABAL_SAKSBEHANDLING_ROLE_ID}")
    private val kabalSaksbehandlerRoleId: String,
    @Value($$"${KABAL_ROL_ROLE_ID}")
    private val kabalROLRoleId: String,
    @Value($$"${KABAL_KROL_ROLE_ID}")
    private val kabalKROLRoleId: String,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    fun loggedInUserIsKlageAdmin(): Boolean {
        val navIdent = tokenUtil.getIdent()
        return entraProxyService.getGroupMembers(klageAdminGroupName).map { it.navIdent }.contains(navIdent)
    }

    fun userIsKabalSaksbehandler(navIdent: String): Boolean {
        return entraProxyService.getUsersGroups(navIdent = navIdent).any { it.rolle == kabalSaksbehandlerGroupName }
    }

    fun userIsROL(navIdent: String): Boolean {
        return entraProxyService.getUsersGroups(navIdent = navIdent).any { it.rolle == kabalROLGroupName }
    }

    fun userIsKROL(navIdent: String): Boolean {
        return entraProxyService.getUsersGroups(navIdent = navIdent).any { it.rolle == kabalKROLGroupName }
    }

    fun loggedInUserIsKabalSaksbehandler(): Boolean {
        if (tokenUtil.getIdent() == null) {
            logger.warn("No NAVident found in token, cannot determine if user is Kabal Saksbehandler")
            return false
        }
        return tokenUtil.getGroups().contains(kabalSaksbehandlerRoleId)
    }

    fun loggedInUserIsKROL(): Boolean {
        if (tokenUtil.getIdent() == null) {
            logger.warn("No NAVident found in token, cannot determine if user is Kabal KROL")
            return false
        }
        return tokenUtil.getGroups().contains(kabalKROLRoleId)
    }

    fun loggedInUserIsROL(): Boolean {
        if (tokenUtil.getIdent() == null) {
            logger.warn("No NAVident found in token, cannot determine if user is Kabal ROL")
            return false
        }
        return tokenUtil.getGroups().contains(kabalROLRoleId)
    }
}