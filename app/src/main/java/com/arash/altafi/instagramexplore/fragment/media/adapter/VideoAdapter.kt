package com.arash.altafi.instagramexplore.fragment.media.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arash.altafi.instagramexplore.databinding.ItemImageBinding
import com.arash.altafi.instagramexplore.databinding.ItemMusicBinding
import com.arash.altafi.instagramexplore.databinding.ItemVideoBinding
import com.arash.altafi.instagramexplore.fragment.media.MediaResponse
import com.arash.altafi.instagramexplore.fragment.media.TypeMedia
import com.arash.altafi.instagramexplore.fragment.media.adapter.viewHolder.ImageViewHolder
import com.arash.altafi.instagramexplore.fragment.media.adapter.viewHolder.MusicViewHolder
import com.arash.altafi.instagramexplore.fragment.media.adapter.viewHolder.VideoViewHolder
import kotlinx.coroutines.flow.MutableStateFlow

class VideoAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list = ArrayList<MediaResponse>()
    var isMuted = MutableStateFlow(false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val bindingVideo = ItemVideoBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        val bindingImage = ItemImageBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        val bindingMusic = ItemMusicBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            TYPE_VIDEO -> VideoViewHolder(bindingVideo)
            TYPE_IMAGE -> ImageViewHolder(bindingImage)
            TYPE_MUSIC -> MusicViewHolder(bindingMusic)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val value = list[position]
        when (holder.itemViewType) {
            TYPE_VIDEO -> (holder as VideoViewHolder).bind(value,this)
            TYPE_MUSIC -> (holder as MusicViewHolder).bind(value,this)
            TYPE_IMAGE -> (holder as ImageViewHolder).bind(value,this)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return when(list[position].type) {
            TypeMedia.VIDEO -> TYPE_VIDEO
            TypeMedia.LIVE -> TYPE_VIDEO
            TypeMedia.MUSIC -> TYPE_MUSIC
            TypeMedia.IMAGE -> TYPE_IMAGE
        }
    }

    fun setData(videos: List<MediaResponse>) {
        list.clear()
        list.addAll(videos)
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_VIDEO = 1
        const val TYPE_IMAGE = 2
        const val TYPE_MUSIC = 3
    }

}