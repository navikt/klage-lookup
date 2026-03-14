package no.nav.klage.lookup.api.person

import no.nav.klage.lookup.api.common.Sak

data class PersonRequest(
    val ident: String,
    val sak: Sak?,
)