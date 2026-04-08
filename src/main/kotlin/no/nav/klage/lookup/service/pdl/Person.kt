package no.nav.klage.lookup.service.pdl

import java.io.Serializable
import java.time.LocalDate

data class Person(
    val foedselsnr: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val sammensattNavn: String,
    val kjoenn: String?,
    val doed: LocalDate?,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    val fortrolig: Boolean,
    val egenAnsatt: Boolean,
    val vergemaalEllerFremtidsfullmakt: Boolean,
    val sikkerhetstiltak: Sikkerhetstiltak?,
    val protectedFamilyMembers: List<ProtectedFamilyMember>,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    data class ProtectedFamilyMember(
        val foedselsnr: String,
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val sammensattNavn: String,
        val kjoenn: String?,
        val doed: LocalDate?,
        val strengtFortrolig: Boolean,
        val strengtFortroligUtland: Boolean,
        val fortrolig: Boolean,
        val egenAnsatt: Boolean,
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }

    data class Sikkerhetstiltak(
        val tiltakstype: Tiltakstype,
        val beskrivelse: String,
        val gyldigFraOgMed: LocalDate,
        val gyldigTilOgMed: LocalDate,
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }

        enum class Tiltakstype {
            FYUS,
            TFUS,
            FTUS,
            DIUS,
            TOAN,
        }
    }
}