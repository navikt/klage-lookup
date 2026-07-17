package no.nav.klage.lookup.api.person

import no.nav.klage.lookup.service.pdl.PersonWithAllInfo

data class PersonBulkResponse(
    val hits: List<PersonWithAllInfo>,
    val misses: List<String>,
)