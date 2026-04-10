package no.nav.klage.lookup.api.user

data class BatchedUserRequest (
    val navIdentList: List<String>
)