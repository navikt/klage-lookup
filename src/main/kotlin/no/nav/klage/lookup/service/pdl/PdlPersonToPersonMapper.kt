package no.nav.klage.lookup.service.pdl

import no.nav.klage.lookup.api.external.person.ExternalPersonResponse
import no.nav.klage.lookup.service.pdl.graphql.PdlPerson

fun toPerson(
    person: Pair<String, PdlPerson>,
    skjermet: Boolean,
): Person {
    val preferredName = preferredName(person.second)

    return Person(
        foedselsnr = person.first,
        fornavn = preferredName?.fornavn ?: "mangler navn",
        mellomnavn = preferredName?.mellomnavn,
        etternavn = preferredName?.etternavn ?: "mangler etternavn",
        sammensattNavn = preferredName?.sammensattNavn() ?: "mangler navn",
        strengtFortrolig = person.second.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG,
        strengtFortroligUtland = person.second.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND,
        fortrolig = person.second.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG,
        kjoenn = person.second.kjoenn.firstOrNull()?.kjoenn?.name,
        vergemaalEllerFremtidsfullmakt = person.second.vergemaalEllerFremtidsfullmakt.isNotEmpty(),
        doed = person.second.doedsfall.firstOrNull()?.doedsdato,
        sikkerhetstiltak = person.second.sikkerhetstiltak.firstOrNull()?.mapToSikkerhetstiltak(),
        egenAnsatt = skjermet,
    )
}

fun PdlPerson.toExternalPersonResponse(fnr: String): ExternalPersonResponse {
    val preferredName = preferredName(this)
    return ExternalPersonResponse(
        foedselsnr = fnr,
        fornavn = preferredName?.fornavn ?: "mangler navn",
        mellomnavn = preferredName?.mellomnavn,
        etternavn = preferredName?.etternavn ?: "mangler etternavn",
        sammensattNavn = preferredName?.sammensattNavn() ?: "mangler navn",
    )
}

private fun preferredName(person: PdlPerson): PdlPerson.Navn? {
    val preferredName = if (person.navn.size == 1) {
        person.navn.first()
    } else {
        person.navn.firstOrNull { it.metadata.master.uppercase() == "PDL" } ?: person.navn.firstOrNull()
    }
    return preferredName
}

private fun PdlPerson.Navn.sammensattNavn(): String =
    if (mellomnavn != null) {
        "$fornavn $mellomnavn $etternavn"
    } else {
        "$fornavn $etternavn"
    }

private fun PdlPerson.Sikkerhetstiltak.mapToSikkerhetstiltak(): Person.Sikkerhetstiltak =
    Person.Sikkerhetstiltak(
        tiltakstype = Person.Sikkerhetstiltak.Tiltakstype.valueOf(this.tiltakstype.name),
        beskrivelse = this.beskrivelse,
        gyldigFraOgMed = this.gyldigFraOgMed,
        gyldigTilOgMed = this.gyldigTilOgMed
    )