package no.nav.klage.lookup.util

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse.*
import no.nav.klage.lookup.api.common.Sak
import java.net.URL

fun shouldCheckFamilyMembers(sak: Sak?): Boolean =
    sak != null && sak.ytelse in listOf(FOR_FOR, FOR_ENG, FOR_SVA) && sak.fagsystem == Fagsystem.FS36

fun URL.cleanForGraphql() = readText().replace("[\n\r]", "")