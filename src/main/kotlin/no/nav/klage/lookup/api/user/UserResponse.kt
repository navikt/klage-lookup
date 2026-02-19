package no.nav.klage.lookup.api.user

data class UsersResponse(
    val users: List<UserResponse>,
)

data class UserResponse (
    val navIdent: String,
    val sammensattNavn: String,
    val fornavn: String,
    val etternavn: String,
)

data class ExtendedUserResponse (
    val navIdent: String,
    val sammensattNavn: String,
    val fornavn: String,
    val etternavn: String,
    val enhet: Enhet,
)

data class Enhet (
    val enhetNr: String,
    val enhetNavn: String,
)

data class GroupsResponse (
    val groupIds: List<String>,
)