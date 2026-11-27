package no.nav.klage.lookup.service.pdl

import no.nav.klage.lookup.api.external.person.Person
import no.nav.klage.lookup.service.pdl.graphql.PdlPerson

fun toPersonWithAllInfo(
    person: Pair<String, PdlPerson>,
    skjermet: Boolean,
): PersonWithAllInfo {
    val preferredName = preferredName(person.second)

    return PersonWithAllInfo(
        foedselsnr = person.first,
        fornavn = preferredName?.fornavn ?: "mangler navn",
        mellomnavn = preferredName?.mellomnavn,
        etternavn = preferredName?.etternavn ?: "mangler etternavn",
        sammensattNavn = preferredName?.sammensattNavn() ?: "mangler navn",
        strengtFortrolig =
            person.second.adressebeskyttelse
                .firstOrNull()
                ?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG,
        strengtFortroligUtland =
            person.second.adressebeskyttelse
                .firstOrNull()
                ?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND,
        fortrolig =
            person.second.adressebeskyttelse
                .firstOrNull()
                ?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG,
        kjoenn = preferredKjoenn(person.second),
        vergemaalEllerFremtidsfullmakt = person.second.vergemaalEllerFremtidsfullmakt.isNotEmpty(),
        doed =
            person.second.doedsfall
                .firstOrNull()
                ?.doedsdato,
        sikkerhetstiltak =
            person.second.sikkerhetstiltak
                .firstOrNull()
                ?.mapToSikkerhetstiltak(),
        falskId = person.second.falskIdentitet?.erFalsk == true,
        egenAnsatt = skjermet,
    )
}

fun PdlPerson.toPerson(fnr: String): Person {
    val preferredName = preferredName(this)
    return Person(
        foedselsnr = fnr,
        fornavn = preferredName?.fornavn ?: "mangler navn",
        mellomnavn = preferredName?.mellomnavn,
        etternavn = preferredName?.etternavn ?: "mangler etternavn",
        sammensattNavn = preferredName?.sammensattNavn() ?: "mangler navn",
    )
}

private data class ResolvedNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)

private fun preferredName(person: PdlPerson): ResolvedNavn? {
    val preferredName =
        if (person.navn.size == 1) {
            person.navn.first()
        } else {
            person.navn.firstOrNull { it.metadata.master.uppercase() == "PDL" } ?: person.navn.firstOrNull()
        }
    if (preferredName != null) {
        return ResolvedNavn(
            fornavn = preferredName.fornavn,
            mellomnavn = preferredName.mellomnavn,
            etternavn = preferredName.etternavn,
        )
    }
    return person.rettIdentitetVedOpplysninger()?.personnavn?.let {
        ResolvedNavn(
            fornavn = it.fornavn,
            mellomnavn = it.mellomnavn,
            etternavn = it.etternavn,
        )
    }
}

private fun preferredKjoenn(person: PdlPerson): String? =
    person.kjoenn
        .firstOrNull()
        ?.kjoenn
        ?.name
        ?: person.rettIdentitetVedOpplysninger()?.kjoenn?.name

private fun PdlPerson.rettIdentitetVedOpplysninger(): PdlPerson.FalskIdentitet.RettIdentitetVedOpplysninger? =
    falskIdentitet?.takeIf { it.erFalsk }?.rettIdentitetVedOpplysninger

private fun ResolvedNavn.sammensattNavn(): String =
    if (mellomnavn != null) {
        "$fornavn $mellomnavn $etternavn"
    } else {
        "$fornavn $etternavn"
    }

private fun PdlPerson.Sikkerhetstiltak.mapToSikkerhetstiltak(): PersonWithAllInfo.Sikkerhetstiltak =
    PersonWithAllInfo.Sikkerhetstiltak(
        tiltakstype = PersonWithAllInfo.Sikkerhetstiltak.Tiltakstype.valueOf(this.tiltakstype.name),
        beskrivelse = this.beskrivelse,
        gyldigFraOgMed = this.gyldigFraOgMed,
        gyldigTilOgMed = this.gyldigTilOgMed,
    )
