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

data class EntraProxyUtvidetAnsatt(
    val navIdent: String,
    val visningNavn: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val enhet: EntraProxyEnhet,
    val tident: String,
)

data class EntraProxyEnhet(
    val enhetnummer: String,
    val navn: String,
)