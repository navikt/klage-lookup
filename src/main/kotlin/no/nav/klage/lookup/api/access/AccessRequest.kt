package no.nav.klage.lookup.api.access

data class AccessRequest(
    val brukerId: String,
    val navIdent: String?,
)