package com.arash.altafi.instagramexplore.fragment.media.adapter.viewHolder

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.ItemImageBinding
import com.arash.altafi.instagramexplore.ext.*
import com.arash.altafi.instagramexplore.fragment.media.MediaResponse
import com.arash.altafi.instagramexplore.fragment.media.TypeMedia
import com.arash.altafi.instagramexplore.fragment.media.adapter.VideoAdapter
import com.arash.altafi.instagramexplore.fragment.media.adapter.VideoPlayerEventListener
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer

class ImageViewHolder(bindingMedia: ItemImageBinding) :
    RecyclerView.ViewHolder(bindingMedia.root),
    VideoPlayerEventListener {

    private lateinit var item: MediaResponse
    private lateinit var typeMedia: TypeMedia
    private var isLike = false
    private val binding = bindingMedia
    private val descriptionMaxLine = 3
    private var videoAdapter: VideoAdapter? = null

    @SuppressLint("ClickableViewAccessibility")
    fun bind(videoItem: MediaResponse, adapter: VideoAdapter) {
        item = videoItem
        videoAdapter = adapter

        with(item) {

            typeMedia = type

            binding.apply {
                val context = itemView.context

                Glide.with(context).load(url)
                    .thumbnail(Glide.with(context).load(url))
                    .into(ivBackground)

                tvTitle.text = title
                tvDescription.setText(description, true)
                tvTime.text = time
                tvLike.text = like
                tvView.text = view

                ivShare.setOnClickListener {
                    context.share(url)
                }

                ivDownload.setOnClickListener {
                    context.openDownloadURL(url)
                }

                ivLike.setOnClickListener {
                    if (isLike.not()) {
                        like(context)
                    } else {
                        unlike(context)
                    }
                }

                rlMedia.doubleClick(context) {
                    like(context)
                }

                tvDescription.setOnClickListener {
                    if (tvDescription.maxLines == descriptionMaxLine)
                        tvDescription.maxLines = Integer.MAX_VALUE
                    else {
                        tvDescription.maxLines = descriptionMaxLine
                        tvDescription.text = description
                    }
                }
            }
        }
    }

    private fun like(context: Context) {
        context.toast(context.getString(R.string.liked))
        if (isLike)
            return
        Glide.with(context).load(R.drawable.ic_like_full).into(binding.ivLike)
        isLike = true
        // TODO: like API
    }

    private fun unlike(context: Context) {
        context.toast(context.getString(R.string.unlike))
        if (isLike.not())
            return

        Glide.with(context).load(R.drawable.ic_like).into(binding.ivLike)
        isLike = false
        // TODO: like API
    }

    override fun onPrePlay(player: ExoPlayer) {}

    override fun onPlayCanceled() {}

    override fun onPlay() {}

}
