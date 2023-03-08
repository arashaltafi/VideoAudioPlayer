package com.arash.altafi.instagramexplore.fragment.video.floatingWindow.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoItem(
    val title: String,
    val videoUrl: String,
    val duration: Long,
    val isLive: Boolean,
    val videoHeight: Int,
    val videoWidth: Int
) : Parcelable