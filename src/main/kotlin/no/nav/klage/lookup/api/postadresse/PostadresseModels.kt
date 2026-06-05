package no.nav.klage.lookup.api.postadresse

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class PostadresseRequest(
    val ident: String,
    val filtrerAdressebeskyttelse: List<Adressebeskyttelse> = emptyList(),
)

enum class Adressebeskyttelse(
    @get:JsonValue
    val value: String,
) {
    FORTROLIG("fortrolig"),
    STRENGT_FORTROLIG("strengt_fortrolig"),
    STRENGT_FORTROLIG_UTLAND("strengt_fortrolig_utland");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): Adressebeskyttelse {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Ugyldig verdi for filtrerAdressebeskyttelse: $value")
        }
    }
}

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

