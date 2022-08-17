package com.arash.altafi.instagramexplore.fragment.media

data class MediaResponse(
    val title: String,
    val description: String,
    val url: String,
    val type: TypeMedia,
    val view: String,
    val like: String,
    val time: String,
    val imageUrl: String? = null
)

enum class TypeMedia {
    VIDEO,
    MUSIC,
    IMAGE
}