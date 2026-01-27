package no.nav.klage.lookup.api.access

import no.nav.klage.kodeverk.ytelse.Ytelse

data class AccessRequest(
    val brukerId: String,
    val navIdent: String?,
    val sak: Sak?,
) {
    data class Sak(
        val sakId: String,
        val ytelse: Ytelse,
    )
}