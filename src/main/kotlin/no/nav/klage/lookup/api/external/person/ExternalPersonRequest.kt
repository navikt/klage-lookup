package no.nav.klage.lookup.api.external.person

import no.nav.klage.kodeverk.Tema

data class ExternalPersonRequest(
    val fnr: String,
    val tema: Tema?,
)