package no.nav.klage.lookup.api.person

data class PersonBulkRequest(
    val fnrList: List<String>,
)