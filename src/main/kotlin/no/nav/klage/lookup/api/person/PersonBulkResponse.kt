package no.nav.klage.lookup.api.person

import no.nav.klage.lookup.service.pdl.Person

data class PersonBulkResponse(
    val hits: List<Person>,
    val misses: List<String>,
)