package no.nav.klage.lookup.service.pdl

import no.nav.klage.lookup.service.pdl.graphql.PdlPerson

fun toPerson(
    person: Pair<String, PdlPerson>,
    relevantFamilyMembers: Map<String, PdlPerson>,
): Person {
    val preferredName = preferredName(person.second)

    return Person(
        foedselsnr = person.first,
        fornavn = preferredName.fornavn,
        mellomnavn = preferredName.mellomnavn,
        etternavn = preferredName.etternavn,
        sammensattNavn = preferredName.sammensattNavn(),
        strengtFortrolig = person.second.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG,
        strengtFortroligUtland = person.second.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND,
        fortrolig = person.second.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG,
        kjoenn = person.second.kjoenn.firstOrNull()?.kjoenn?.name,
        vergemaalEllerFremtidsfullmakt = person.second.vergemaalEllerFremtidsfullmakt.isNotEmpty(),
        doed = person.second.doedsfall.firstOrNull()?.doedsdato,
        sikkerhetstiltak = person.second.sikkerhetstiltak.firstOrNull()?.mapToSikkerhetstiltak(),
        egenAnsatt = false, // Dette må settes basert på en egen ansatt-info
        relevantFamily = relevantFamilyMembers.map { familyMember ->
            val preferredFamilyName = preferredName(familyMember.value)
            Person.FamilyMember(
                foedselsnr = familyMember.key,
                fornavn = preferredFamilyName.fornavn,
                mellomnavn = preferredFamilyName.mellomnavn,
                etternavn = preferredFamilyName.etternavn,
                sammensattNavn = preferredFamilyName.sammensattNavn(),
                kjoenn = familyMember.value.kjoenn.firstOrNull()?.kjoenn?.name,
                doed = familyMember.value.doedsfall.firstOrNull()?.doedsdato,
                strengtFortrolig = familyMember.value.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG,
                strengtFortroligUtland = familyMember.value.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND,
                fortrolig = familyMember.value.adressebeskyttelse.firstOrNull()?.gradering == PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG,
                egenAnsatt = false, // Dette må settes basert på en egen ansatt-info
            )
        }
    )
}

private fun preferredName(person: PdlPerson): PdlPerson.Navn {
    val preferredName = if (person.navn.size == 1) {
        person.navn.first()
    } else {
        person.navn.firstOrNull { it.metadata.master.uppercase() == "PDL" } ?: person.navn.first()
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