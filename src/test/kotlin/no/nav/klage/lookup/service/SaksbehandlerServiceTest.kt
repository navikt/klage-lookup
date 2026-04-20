package no.nav.klage.lookup.service

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.lookup.api.user.BatchedGroupsHitResponse
import no.nav.klage.lookup.api.user.ExtendedUserResponse
import no.nav.klage.lookup.config.entraproxy.EntraProxyEnhet
import no.nav.klage.lookup.config.entraproxy.EntraProxyRolle
import no.nav.klage.lookup.config.entraproxy.EntraProxyUtvidetAnsatt
import no.nav.klage.lookup.service.nom.NomFacade
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaksbehandlerServiceTest {

    private val tokenUtil = mockk<TokenUtil>()
    private val microsoftGraphService = mockk<MicrosoftGraphService>(relaxed = true)
    private val entraProxyService = mockk<EntraProxyService>()
    private val nomFacade = mockk<NomFacade>()

    private val saksbehandlerService = SaksbehandlerService(
        tokenUtil = tokenUtil,
        microsoftGraphService = microsoftGraphService,
        entraProxyService = entraProxyService,
        nomFacade = nomFacade,
        kabalOppgavestyringAlleEnheterRoleId = "kabalOppgavestyringAlleEnheterRoleId",
        kabalMaltekstredigeringRoleId = "kabalMaltekstredigeringRoleId",
        kabalSaksbehandlerRoleId = "kabalSaksbehandlerRoleId",
        kabalFagtekstredigeringRoleId = "kabalFagtekstredigeringRoleId",
        kabalInnsynEgenEnhetRoleId = "kabalInnsynEgenEnhetRoleId",
        kabalTilgangsstyringEgenEnhetRoleId = "kabalTilgangsstyringEgenEnhetRoleId",
        fortroligRoleId = "fortroligRoleId",
        strengtFortroligRoleId = "strengtFortroligRoleId",
        egenAnsattRoleId = "egenAnsattRoleId",
        kabalAdminRoleId = "kabalAdminRoleId",
        kabalROLRoleId = "kabalROLRoleId",
        kabalKROLRoleId = "kabalKROLRoleId",
        kabalSvarbrevInnstillingerRoleId = "kabalSvarbrevInnstillingerRoleId",
        alleINavKlageinstansRoleId = "alleINavKlageinstansRoleId",
        kakaKvalitetsvurderingRoleId = "kakaKvalitetsvurderingRoleId",
        kakaKvalitetstilbakemeldingRoleId = "kakaKvalitetstilbakemeldingRoleId",
        kakaTotalstatistikkRoleId = "kakaTotalstatistikkRoleId",
        kakaLederstatistikkRoleId = "kakaLederstatistikkRoleId",
        kakaExcelUttrekkMedFritekstRoleId = "kakaExcelUttrekkMedFritekstRoleId",
        kakaExcelUttrekkUtenFritekstRoleId = "kakaExcelUttrekkUtenFritekstRoleId",
    )

    @Test
    fun `getUserInfoBatched returns hits and misses`() {
        every { tokenUtil.getIdent() } returns null
        every { entraProxyService.getUserInfo("A123") } returns createAnsatt("A123")
        every { entraProxyService.getUserInfo("B456") } throws RuntimeException("Not found")

        val result = saksbehandlerService.getUserInfoBatched(listOf("A123", "B456"))

        assertThat(result.hits).containsExactly(
            ExtendedUserResponse(
                navIdent = "A123",
                fornavn = "Fornavn",
                etternavn = "Etternavn",
                sammensattNavn = "Fornavn Etternavn",
                enhet = no.nav.klage.lookup.api.user.Enhet(
                    enhetNr = "4200",
                    enhetNavn = "Klageenheten",
                ),
            )
        )
        assertThat(result.misses).containsExactly("B456")
    }

    @Test
    fun `getUserInfoBatched deduplicates input before lookup`() {
        every { tokenUtil.getIdent() } returns null
        every { entraProxyService.getUserInfo("A123") } returns createAnsatt("A123")
        every { entraProxyService.getUserInfo("B456") } throws RuntimeException("Not found")

        val result = saksbehandlerService.getUserInfoBatched(listOf("A123", "A123", "B456", "B456"))

        assertThat(result.hits).hasSize(1)
        assertThat(result.misses).containsExactly("B456")

        verify(exactly = 1) { entraProxyService.getUserInfo("A123") }
        verify(exactly = 1) { entraProxyService.getUserInfo("B456") }
        confirmVerified(entraProxyService)
    }

    @Test
    fun `getGroupsForUsersBatched returns hits and misses`() {
        every { tokenUtil.getIdent() } returns null
        every { entraProxyService.getUsersGroups("A123") } returns listOf(
            EntraProxyRolle(AzureGroup.KABAL_ADMIN.reference),
            EntraProxyRolle("unknown-role"),
        )
        every { entraProxyService.getUsersGroups("B456") } throws RuntimeException("Not found")

        val result = saksbehandlerService.getGroupsForUsersBatched(listOf("A123", "B456"))

        assertThat(result.hits).containsExactly(
            BatchedGroupsHitResponse(
                navIdent = "A123",
                groupIds = listOf(AzureGroup.KABAL_ADMIN.id),
            )
        )
        assertThat(result.misses).containsExactly("B456")
    }

    @Test
    fun `getGroupsForUsersBatched deduplicates input before lookup`() {
        every { tokenUtil.getIdent() } returns null
        every { entraProxyService.getUsersGroups("A123") } returns emptyList()
        every { entraProxyService.getUsersGroups("B456") } throws RuntimeException("Not found")

        val result = saksbehandlerService.getGroupsForUsersBatched(listOf("A123", "A123", "B456", "B456"))

        assertThat(result.hits).containsExactly(
            BatchedGroupsHitResponse(
                navIdent = "A123",
                groupIds = emptyList(),
            )
        )
        assertThat(result.misses).containsExactly("B456")

        verify(exactly = 1) { entraProxyService.getUsersGroups("A123") }
        verify(exactly = 1) { entraProxyService.getUsersGroups("B456") }
        confirmVerified(entraProxyService)
    }

    private fun createAnsatt(navIdent: String): EntraProxyUtvidetAnsatt {
        return EntraProxyUtvidetAnsatt(
            navIdent = navIdent,
            visningNavn = "Fornavn Etternavn",
            fornavn = "Fornavn",
            etternavn = "Etternavn",
            enhet = EntraProxyEnhet(
                enhetnummer = "4200",
                navn = "Klageenheten",
            ),
            tIdent = "T123456",
        )
    }
}

