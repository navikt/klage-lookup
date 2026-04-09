package no.nav.klage.lookup.service.pdl

/**
 * When PDL-api works, but responds with an error for some reason
 */
class PDLErrorException(msg: String) : RuntimeException(msg)

class PDLPersonNotFoundException(msg: String) : RuntimeException(msg)