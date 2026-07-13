package no.nav.klage.lookup.api.repr

import java.io.Serializable

//TODO: Håndter tema
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

