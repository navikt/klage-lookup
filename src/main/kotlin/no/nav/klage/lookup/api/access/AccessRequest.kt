package no.nav.klage.lookup.api.access

import no.nav.klage.lookup.api.common.Sak

data class AccessRequest(
    val brukerId: String,
    val navIdent: String?,
    @Deprecated("Sak is being removed. Clients need to call api once for every person in sak instead.")
    val sak: Sak?,
)