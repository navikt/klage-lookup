package no.nav.klage.lookup.api.postadresse

data class PostadresseRequest(
    val ident: String,
)

data class PostadresseResponse(
    val navn: String?,
    val adresse: Postadresse?,
)

data class Postadresse(
    val adresseKilde: String?,
    val type: String?,
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val postnummer: String?,
    val poststed: String?,
    val landkode: String?,
    val land: String?,
)

