package com.rickandmorty.app.features.episodes.domain.model

data class Episode(
    val id: Int,
    val name: String,
    val airDate: String,
    val episodeCode: String,
    val characterIds: List<Int>
) {
    val seasonNumber: Int?
        get() = runCatching {
            if (episodeCode.startsWith("S", ignoreCase = true) && episodeCode.length >= 3) {
                episodeCode.substring(1, 3).toIntOrNull()
            } else null
        }.getOrNull()

    val episodeNumber: Int?
        get() = runCatching {
            val eIndex = episodeCode.indexOfAny(charArrayOf('E', 'e'))
            if (eIndex != -1 && episodeCode.length >= eIndex + 3) {
                episodeCode.substring(eIndex + 1, eIndex + 3).toIntOrNull()
            } else null
        }.getOrNull()

    val formattedSeasonEpisode: String
        get() = if (seasonNumber != null && episodeNumber != null) {
            "Season $seasonNumber • Episode $episodeNumber"
        } else {
            episodeCode
        }
}
