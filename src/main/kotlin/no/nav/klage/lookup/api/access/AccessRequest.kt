package no.nav.klage.lookup.api.access

import no.nav.klage.lookup.api.common.Sak

data class AccessRequest(
    val brukerId: String,
    val navIdent: String?,
    val sak: Sak?,
)