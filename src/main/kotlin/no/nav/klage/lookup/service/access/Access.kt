package no.nav.klage.lookup.service.access

import java.io.Serializable

data class Access(
    val access: Boolean,
    val reason: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}