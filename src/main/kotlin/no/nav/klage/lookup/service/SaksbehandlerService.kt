package no.nav.klage.lookup.service

import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.lookup.api.user.*
import no.nav.klage.lookup.config.entraproxy.EntraProxyAnsatt
import no.nav.klage.lookup.config.entraproxy.EntraProxyUtvidetAnsatt
import no.nav.klage.lookup.config.microsoftgraph.MicrosoftGraphUser
import no.nav.klage.lookup.service.nom.NomErrorException
import no.nav.klage.lookup.service.nom.NomFacade
import no.nav.klage.lookup.service.nom.graphql.Ansatt
import no.nav.klage.lookup.util.TokenUtil
import no.nav.klage.lookup.util.getLogger
import no.nav.klage.lookup.util.getTeamLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val tokenUtil: TokenUtil,
    private val microsoftGraphService: MicrosoftGraphService,
    private val entraProxyService: EntraProxyService,
    private val nomFacade: NomFacade,
    @Value($$"${KABAL_OPPGAVESTYRING_ALLE_ENHETER_ROLE_ID}")
    private val kabalOppgavestyringAlleEnheterRoleId: String,
    @Value($$"${KABAL_MALTEKSTREDIGERING_ROLE_ID}")
    private val kabalMaltekstredigeringRoleId: String,
    @Value($$"${KABAL_SAKSBEHANDLING_ROLE_ID}")
    private val kabalSaksbehandlerRoleId: String,
    @Value($$"${KABAL_FAGTEKSTREDIGERING_ROLE_ID}")
    private val kabalFagtekstredigeringRoleId: String,
    @Value($$"${KABAL_INNSYN_EGEN_ENHET_ROLE_ID}")
    private val kabalInnsynEgenEnhetRoleId: String,
    @Value($$"${KABAL_TILGANGSSTYRING_EGEN_ENHET_ROLE_ID}")
    private val kabalTilgangsstyringEgenEnhetRoleId: String,
    @Value($$"${FORTROLIG_ROLE_ID}")
    private val fortroligRoleId: String,
    @Value($$"${STRENGT_FORTROLIG_ROLE_ID}")
    private val strengtFortroligRoleId: String,
    @Value($$"${EGEN_ANSATT_ROLE_ID}")
    private val egenAnsattRoleId: String,
    @Value($$"${KABAL_ADMIN_ROLE_ID}")
    private val kabalAdminRoleId: String,
    @Value($$"${KABAL_ROL_ROLE_ID}")
    private val kabalROLRoleId: String,
    @Value($$"${KABAL_KROL_ROLE_ID}")
    private val kabalKROLRoleId: String,
    @Value($$"${KABAL_SVARBREVINNSTILLINGER_ROLE_ID}")
    private val kabalSvarbrevInnstillingerRoleId: String,
    @Value($$"${ALLE_I_NAV_KLAGEINSTANS_ROLE_ID}")
    private val alleINavKlageinstansRoleId: String,
    @Value($$"${KAKA_KVALITETSVURDERING_ROLE_ID}")
    private val kakaKvalitetsvurderingRoleId: String,
    @Value($$"${KAKA_KVALITETSTILBAKEMELDING_ROLE_ID}")
    private val kakaKvalitetstilbakemeldingRoleId: String,
    @Value($$"${KAKA_TOTALSTATISTIKK_ROLE_ID}")
    private val kakaTotalstatistikkRoleId: String,
    @Value($$"${KAKA_LEDERSTATISTIKK_ROLE_ID}")
    private val kakaLederstatistikkRoleId: String,
    @Value($$"${KAKA_EXCEL_UTTREKK_MED_FRITEKST_ROLE_ID}")
    private val kakaExcelUttrekkMedFritekstRoleId: String,
    @Value($$"${KAKA_EXCEL_UTTREKK_UTEN_FRITEKST_ROLE_ID}")
    private val kakaExcelUttrekkUtenFritekstRoleId: String,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    fun loggedInUserIsKlageAdmin(): Boolean {
        if (tokenUtil.getIdent() == null) {
            throw RuntimeException("No logged in user")
        }
        return getGroupsForUser(navIdent = tokenUtil.getIdent()!!).groupIds.contains(AzureGroup.KABAL_ADMIN.id)
    }

    fun getGroupsForUser(navIdent: String): GroupsResponse {
        return if (tokenUtil.getIdent() == navIdent) {
            logger.debug("Getting groups for logged in user with NAVident '{}'", navIdent)
            val userGroups = tokenUtil.getGroups()
            teamLogger.debug("Found groups for logged in user {}: {}", tokenUtil.getIdent(), userGroups)
            userGroups.toGroupsResponse()
        } else {
            logger.debug("Getting groups for user with NAVident '{}'", navIdent)
            val userGroups = entraProxyService.getUsersGroups(navIdent = navIdent)
            GroupsResponse(
                groupIds = userGroups.mapNotNull { userGroup ->
                    AzureGroup.entries.find { it.reference == userGroup.rolle }?.id
                }
            )
        }
    }

    fun getUserInfo(navIdent: String): ExtendedUserResponse {
        return if (tokenUtil.getIdent() == navIdent) {
            logger.debug("Getting user info for logged in user with NAVident '{}'", navIdent)
            entraProxyService.getUserInfo(tokenUtil.getIdent()!!).toUserResponse()
        } else {
            logger.debug("Getting user info for user with NAVident '{}'", navIdent)
            entraProxyService.getUserInfo(navIdent).toUserResponse()
        }
    }

    fun getUserInfoBatched(navIdentList: List<String>): ExtendedUsersResponse {
        val lookupResults = navIdentList
            .distinct()
            .map { navIdent -> navIdent to runCatching { getUserInfo(navIdent) } }

        val (successfulLookups, failedLookups) = lookupResults.partition { (_, result) -> result.isSuccess }

        return ExtendedUsersResponse(
            hits = successfulLookups.map { (_, result) -> result.getOrThrow() },
            misses = failedLookups.map { (navIdent, _) -> navIdent },
        )
    }

    fun getGroupsForUsersBatched(navIdentList: List<String>): BatchedGroupsResponse {
        val lookupResults = navIdentList
            .distinct()
            .associateWith { navIdent -> runCatching { getGroupsForUser(navIdent) } }

        return BatchedGroupsResponse(
            hits = lookupResults.mapNotNull { (navIdent, result) ->
                result.getOrNull()?.let { groupsResponse ->
                    BatchedGroupsHitResponse(
                        navIdent = navIdent,
                        groupIds = groupsResponse.groupIds,
                    )
                }
            },
            misses = lookupResults.filterValues { it.isFailure }.keys.toList(),
        )
    }

    fun getUsersInEnhet(enhetsnummer: String): UsersResponse {
        return UsersResponse(
            microsoftGraphService.getAnsatteInEnhet(enhetsnummer = enhetsnummer).value?.map { it.toUserResponse() }
                .orEmpty(),
        )
    }

    fun getUsersInGroup(azureGroup: AzureGroup): UsersResponse {
        return UsersResponse(
            users = entraProxyService.getGroupMembers(gruppeNavn = azureGroup.reference).map { it.toUserResponse() }
        )
    }

    fun getSluttdatoForUser(navIdent: String): SluttdatoResponse {
        return nomFacade.getAnsattInfoFromNom(navIdent = navIdent).toSluttdatoResponse()
    }

    fun getSluttdatoForUsers(navIdentList: List<String>): BatchedSluttdatoResponse {
        val response = nomFacade.getAnsatteInfoFromNom(navIdentList = navIdentList.distinct())
        if (response.data == null) {
            throw NomErrorException("Klarte ikke å hente ansatte fra NOM")
        }
        val ressurser = response.data.ressurser

        return BatchedSluttdatoResponse(
            hits = ressurser.mapNotNull { it.ressurs?.toSluttdatoResponse() },
            misses = ressurser.filter { it.ressurs == null }.map { it.id },
        )
    }

    private fun Ansatt.toSluttdatoResponse(): SluttdatoResponse {
        return SluttdatoResponse(
            navIdent = navident,
            sluttdato = sluttdato,
        )
    }

    private fun EntraProxyUtvidetAnsatt.toUserResponse(): ExtendedUserResponse {
        return ExtendedUserResponse(
            navIdent = this.navIdent,
            fornavn = this.fornavn,
            etternavn = this.etternavn,
            sammensattNavn = this.visningNavn,
            enhet = Enhet(
                enhetNr = this.enhet.enhetnummer,
                enhetNavn = this.enhet.navn,
            ),
        )
    }

    private fun MicrosoftGraphUser.toUserResponse(): UserResponse {
        return UserResponse(
            navIdent = this.onPremisesSamAccountName,
            fornavn = this.givenName,
            etternavn = this.surname,
            sammensattNavn = this.displayName,
        )
    }

    private fun EntraProxyAnsatt.toUserResponse(): UserResponse {
        return UserResponse(
            navIdent = this.navIdent,
            fornavn = this.fornavn,
            etternavn = this.etternavn,
            sammensattNavn = this.visningNavn,
        )
    }

    private fun List<String>.toGroupsResponse(): GroupsResponse {
        return GroupsResponse(
            groupIds = this.mapNotNull {
                getAzureGroupFromGroupId(it)?.id
            }
        )
    }

    fun getAzureGroupFromGroupId(groupId: String): AzureGroup? {
        return when (groupId) {
            kabalOppgavestyringAlleEnheterRoleId -> AzureGroup.KABAL_OPPGAVESTYRING_ALLE_ENHETER
            kabalMaltekstredigeringRoleId -> AzureGroup.KABAL_MALTEKSTREDIGERING
            kabalSaksbehandlerRoleId -> AzureGroup.KABAL_SAKSBEHANDLING
            kabalFagtekstredigeringRoleId -> AzureGroup.KABAL_FAGTEKSTREDIGERING
            kabalInnsynEgenEnhetRoleId -> AzureGroup.KABAL_INNSYN_EGEN_ENHET
            kabalTilgangsstyringEgenEnhetRoleId -> AzureGroup.KABAL_TILGANGSSTYRING_EGEN_ENHET
            fortroligRoleId -> AzureGroup.FORTROLIG
            strengtFortroligRoleId -> AzureGroup.STRENGT_FORTROLIG
            egenAnsattRoleId -> AzureGroup.EGEN_ANSATT
            kabalAdminRoleId -> AzureGroup.KABAL_ADMIN
            kabalROLRoleId -> AzureGroup.KABAL_ROL
            kabalKROLRoleId -> AzureGroup.KABAL_KROL
            kabalSvarbrevInnstillingerRoleId -> AzureGroup.KABAL_SVARBREVINNSTILLINGER
            alleINavKlageinstansRoleId -> AzureGroup.ALLE_I_NAV_KLAGEINSTANS
            kakaKvalitetsvurderingRoleId -> AzureGroup.KAKA_KVALITETSVURDERING
            kakaKvalitetstilbakemeldingRoleId -> AzureGroup.KAKA_KVALITETSTILBAKEMELDINGER
            kakaTotalstatistikkRoleId -> AzureGroup.KAKA_TOTALSTATISTIKK
            kakaLederstatistikkRoleId -> AzureGroup.KAKA_LEDERSTATISTIKK
            kakaExcelUttrekkMedFritekstRoleId -> AzureGroup.KAKA_EXCEL_UTTREKK_MED_FRITEKST
            kakaExcelUttrekkUtenFritekstRoleId -> AzureGroup.KAKA_EXCEL_UTTREKK_UTEN_FRITEKST
            else -> null
        }
    }
}
