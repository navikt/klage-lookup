package no.nav.klage.lookup.service.skjerming

import java.time.LocalDateTime

data class SkjermetPerson(val skjermetFra: LocalDateTime, val skjermetTil: LocalDateTime?) {

    fun skjermet(): Boolean {
        val now = LocalDateTime.now()
        return skjermetFra.isBefore(now) && (skjermetTil ?: LocalDateTime.MAX).isAfter(now)
    }
}