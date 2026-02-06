package no.nav.klage.lookup.config.entraproxy

data class EntraProxyAnsatt(
    val navIdent: String,
    val visningNavn: String,
    val fornavn: String,
    val etternavn: String
)

data class EntraProxyRolle(
    val rolle: String,
)