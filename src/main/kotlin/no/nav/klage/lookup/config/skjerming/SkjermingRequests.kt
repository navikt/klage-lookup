package no.nav.klage.lookup.config.skjerming

data class SkjermingRequest(
    val personident: String,
)

data class SkjermingBulkRequest(
    val personidenter: List<String>,
)