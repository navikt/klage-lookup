package no.nav.klage.lookup.service.nom

/**
 * When NOM-api works, but responds with an error for some reason.
 */
class NomErrorException(msg: String) : RuntimeException(msg)

class NomAnsattNotFoundException(msg: String) : RuntimeException(msg)
