package no.nav.klage.lookup.api.person

import no.nav.klage.lookup.api.common.Sak

data class PersonRequest(
    val fnr: String,
    val sak: Sak?,
)