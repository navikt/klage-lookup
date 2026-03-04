package no.nav.klage.lookup.config.microsoftgraph

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphUserList(val value: List<MicrosoftGraphUser>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphUser(
    val userPrincipalName: String,
    val onPremisesSamAccountName: String,
    val displayName: String,
    val givenName: String,
    val surname: String,
)