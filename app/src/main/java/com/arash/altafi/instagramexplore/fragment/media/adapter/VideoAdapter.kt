package com.arash.altafi.instagramexplore.fragment.media.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arash.altafi.instagramexplore.databinding.ItemMediaBinding
import com.arash.altafi.instagramexplore.fragment.media.MediaResponse
import kotlinx.coroutines.flow.MutableStateFlow

class VideoAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = ArrayList<VideoViewItem>()

    var isMuted = MutableStateFlow(false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemMediaBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val value = data[position]
        when (holder) {
            is VideoViewHolder -> holder.bind(value as VideoViewItem.VideoItem, this)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].viewType
    }

    fun setData(videos: List<MediaResponse>) {
        data.clear()
        data.addAll(VideoViewItem.buildItems(videos))
        notifyDataSetChanged()
    }

}