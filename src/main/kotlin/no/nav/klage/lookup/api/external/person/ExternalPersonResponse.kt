package no.nav.klage.lookup.api.external.person

import java.io.Serializable

data class Person(
    val foedselsnr: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val sammensattNavn: String,
) : Serializable