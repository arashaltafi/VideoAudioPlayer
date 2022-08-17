package com.arash.altafi.instagramexplore.fragment.media

data class MediaResponse(
    val title: String,
    val description: String,
    val url: String,
    val isVideo: Boolean,
    val view: String,
    val like: String,
    val time: String
)
