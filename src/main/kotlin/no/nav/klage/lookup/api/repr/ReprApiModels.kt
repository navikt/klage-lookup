package no.nav.klage.lookup.api.repr

import no.nav.klage.kodeverk.Tema
import java.io.Serializable

data class RepresentasjonsforholdDto(
    val fullmakt: List<FullmaktsforholdDto>,
    val vergemaal: List<VergemaalsforholdDto>,
) : Serializable

data class FullmaktsforholdDto(
    val fullmaktsgiver: String,
    val fullmektig: String,
    val leserettigheter: Set<String>,
    val skriverettigheter: Set<String>,
) : Serializable

data class VergemaalsforholdDto(
    val vergehaver: String,
    val verge: String,
    val leserettigheter: Set<String>,
    val skriverettigheter: Set<String>,
) : Serializable

data class RepresentasjonsforholdView(
    val fullmakt: List<FullmaktsforholdView>,
    val vergemaal: List<VergemaalsforholdView>,
) : Serializable

data class FullmaktsforholdView(
    val fullmaktsgiver: String,
    val fullmektig: String,
    val leserettigheter: Set<Tema>,
    val skriverettigheter: Set<Tema>,
) : Serializable

data class VergemaalsforholdView(
    val vergehaver: String,
    val verge: String,
    val leserettigheter: Set<Tema>,
    val skriverettigheter: Set<Tema>,
) : Serializable

fun RepresentasjonsforholdDto.toRepresentasjonsforholdView(): RepresentasjonsforholdView {
    return RepresentasjonsforholdView(
        fullmakt = fullmakt.map { it.toFullmaktsforholdView() },
        vergemaal = vergemaal.map { it.toVergemaalsforholdView() },
    )
}

fun FullmaktsforholdDto.toFullmaktsforholdView(): FullmaktsforholdView {
    return FullmaktsforholdView(
        fullmaktsgiver = fullmaktsgiver,
        fullmektig = fullmektig,
        leserettigheter = leserettigheter.map { Tema.fromNavn(it) }.toSet(),
        skriverettigheter = skriverettigheter.map { Tema.fromNavn(it) }.toSet(),
    )
}

fun VergemaalsforholdDto.toVergemaalsforholdView(): VergemaalsforholdView {
    return VergemaalsforholdView(
        vergehaver = vergehaver,
        verge = verge,
        leserettigheter = leserettigheter.map { Tema.fromNavn(it) }.toSet(),
        skriverettigheter = skriverettigheter.map { Tema.fromNavn(it) }.toSet(),
    )
}