package no.nav.klage.lookup.service

import no.nav.klage.lookup.util.TokenUtil
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
) {
    fun loggedInUserIsKlageAdmin(): Boolean {
        val navIdent = tokenUtil.getIdent()
        return entraProxyService.getGroupMembers(klageAdminGroupName).map { it.navIdent }.contains(navIdent)
    }

    fun userIsKabalSaksbehandler(navIdent: String?): Boolean {
        return entraProxyService.getUsersGroups(navIdent = navIdent).any { it.rolle == kabalSaksbehandlerGroupName }
    }

    fun userIsROL(navIdent: String): Boolean {
        return entraProxyService.getUsersGroups(navIdent = navIdent).any { it.rolle == kabalROLGroupName }
    }

    fun userIsKROL(navIdent: String): Boolean {
        return entraProxyService.getUsersGroups(navIdent = navIdent).any { it.rolle == kabalKROLGroupName }
    }
}