package com.arash.altafi.instagramexplore.fragment.media.adapter

import com.arash.altafi.instagramexplore.fragment.media.MediaResponse


sealed class VideoViewItem(val viewType: Int) {
    data class VideoItem(val video: MediaResponse) : VideoViewItem(ITEM_VIEW_TYPE_VIDEO)
    companion object {
        const val ITEM_VIEW_TYPE_VIDEO = 1

        fun buildItems(videos: List<MediaResponse>): ArrayList<VideoViewItem> {
            return arrayListOf<VideoViewItem>().apply {
                videos.map { add(VideoItem(it)) }
            }
        }
    }
}


