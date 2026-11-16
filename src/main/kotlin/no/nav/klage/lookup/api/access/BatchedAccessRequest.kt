package no.nav.klage.lookup.api.access

data class BatchedAccessRequest(
    /** fnr, dnr or aktorId */
    val brukerId: String,
    val navIdentList: List<String>,
)

data class BatchedAccessResponse(
    val results: List<NavIdentAccess>,
)

data class NavIdentAccess(
    val navIdent: String,
    val access: Boolean,
    val reason: String,
)
