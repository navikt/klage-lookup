package no.nav.klage.lookup.config.microsoftgraph

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphUserList(val value: List<MicrosoftGraphUser>?) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphUser(
    val userPrincipalName: String,
    val onPremisesSamAccountName: String,
    val displayName: String,
    val givenName: String,
    val surname: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}