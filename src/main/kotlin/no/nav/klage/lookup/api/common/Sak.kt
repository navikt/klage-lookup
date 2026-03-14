package no.nav.klage.lookup.api.common

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse

data class Sak(
    val sakId: String,
    val ytelse: Ytelse,
    val fagsystem: Fagsystem?,
)