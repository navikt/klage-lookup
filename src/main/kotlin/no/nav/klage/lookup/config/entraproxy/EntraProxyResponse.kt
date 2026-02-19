package no.nav.klage.lookup.config.entraproxy

import java.io.Serializable

data class EntraProxyAnsatt(
    val navIdent: String,
    val visningNavn: String,
    val fornavn: String,
    val etternavn: String
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

data class EntraProxyRolle(
    val rolle: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

data class EntraProxyUtvidetAnsatt(
    val navIdent: String,
    val visningNavn: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String? = null,
    val enhet: EntraProxyEnhet,
    val tIdent: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

data class EntraProxyEnhet(
    val enhetnummer: String,
    val navn: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

