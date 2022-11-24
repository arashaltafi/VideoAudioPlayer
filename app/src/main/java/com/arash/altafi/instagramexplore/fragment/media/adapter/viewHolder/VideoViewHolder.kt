package com.arash.altafi.instagramexplore.fragment.media.adapter.viewHolder

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.ItemVideoBinding
import com.arash.altafi.instagramexplore.ext.*
import com.arash.altafi.instagramexplore.fragment.media.InstagramFragmentDirections
import com.arash.altafi.instagramexplore.fragment.media.MediaResponse
import com.arash.altafi.instagramexplore.fragment.media.TypeMedia
import com.arash.altafi.instagramexplore.fragment.media.adapter.VideoAdapter
import com.arash.altafi.instagramexplore.fragment.media.adapter.VideoPlayerEventListener
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoViewHolder(bindingMedia: ItemVideoBinding) :
    RecyclerView.ViewHolder(bindingMedia.root),
    VideoPlayerEventListener {

    companion object {
        private const val TAG = "VideoViewHolder"
    }

    private lateinit var item: MediaResponse
    private lateinit var typeMedia: TypeMedia
    private var isLike = false
    private val binding = bindingMedia
    private var timeVideo: Long = 0
    private var timeNow: Long = 0
    private var playerJob: Job? = null
    private val descriptionMaxLine = 3
    private var videoAdapter: VideoAdapter? = null

    @SuppressLint("ClickableViewAccessibility")
    fun bind(videoItem: MediaResponse, adapter: VideoAdapter) {
        item = videoItem
        videoAdapter = adapter

        with(item) {
            runJob()

            typeMedia = type

            binding.apply {
                val context = itemView.context

                setSound(context, adapter.isMuted.value, false)

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

                ivSound.setOnClickListener {
                    setSound(context, adapter.isMuted.value.not(), true)
                }

                tvDescription.setOnClickListener {
                    if (tvDescription.maxLines == descriptionMaxLine)
                        tvDescription.maxLines = Integer.MAX_VALUE
                    else {
                        tvDescription.maxLines = descriptionMaxLine
                        tvDescription.text = description
                    }
                }

                val gestureDetector =
                    GestureDetectorCompat(itemView.context, object :
                        GestureDetector.SimpleOnGestureListener() {

                        override fun onDown(e: MotionEvent): Boolean {
                            return true
                        }

                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            if (typeMedia == TypeMedia.VIDEO || typeMedia == TypeMedia.LIVE)
                                Navigation.findNavController(videoPlayer).navigate(
                                    InstagramFragmentDirections.actionInstagramFragmentToVideoFragment(
                                        title,
                                        url,
                                        videoPlayer.player?.currentPosition ?: 0
                                    )
                                )
                            return typeMedia == TypeMedia.VIDEO || typeMedia == TypeMedia.LIVE
                        }

                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            like(context)
                            return true
                        }

                    })

                rlMedia.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_BUTTON_RELEASE)
                        v.performClick()

                    return@setOnTouchListener gestureDetector.onTouchEvent(event)
                }

                tvTimeVideo.text = videoPlayer.player?.duration?.convertDurationToTime()
            }
        }
    }

    private fun setSound(context: Context, mute: Boolean, notify: Boolean) = binding.apply {
        if (mute) {
            Glide.with(context).load(R.drawable.ic_sound_off).into(ivSound)
            if (notify)
                context.toast(context.getString(R.string.sound_muted))
            videoPlayer.player?.volume = 0f
        } else {
            Glide.with(context).load(R.drawable.ic_sound_on).into(ivSound)
            if (notify)
                context.toast(context.getString(R.string.sound_un_mute))
            videoPlayer.player?.volume = 1f
        }

        videoAdapter?.isMuted?.tryEmit(mute)
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

    private fun setupVideoDuration() {
        binding.apply {

            itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launchWhenResumed {
                "launchWhenResumed @$absoluteAdapterPosition".logI(TAG)

                playerJob = superLaunch {
                    launch {
                        while (true) {
                            "launchWhenResumed @$absoluteAdapterPosition launch".logI(TAG)
                            timeNow = videoPlayer.player?.currentPosition ?: 0
                            if (timeNow >= 0) {
                                cvTimeVideo.toShow()
                                tvTimeVideo.text = (timeVideo - timeNow).convertDurationToTime()
                                "videoDuration: $timeNow".logE(TAG)
                            } else {
                                cvTimeVideo.toGone()
                            }
                            delay(1000)
                        }
                    }

                    launch {
                        videoAdapter?.isMuted?.collect {
                            "listenMuteState isMuted @$absoluteAdapterPosition => $it".logD(TAG)
                            setSound(itemView.context, it, false)
                        }
                    }
                }

            }

        }
    }

    override fun onPrePlay(player: ExoPlayer) {
        "onPrePlay @$absoluteAdapterPosition".logD(TAG)
        binding.apply {
            videoPlayer.toGone()
            ivBackground.toShow()
            if (typeMedia == TypeMedia.VIDEO || typeMedia == TypeMedia.LIVE) progressBar.toShow()
            else progressBar.toGone()
            //play video
            with(player) {
                playVideo()
                videoPlayer.player = this
            }
        }
    }

    override fun onPlayCanceled() {
        "onPlayCanceled @$absoluteAdapterPosition".logD(TAG)
        binding.apply {
            videoPlayer.player = null
            videoPlayer.toGone()
            ivBackground.toShow()
            if (typeMedia == TypeMedia.VIDEO || typeMedia == TypeMedia.LIVE) progressBar.toShow()
            else progressBar.toGone()
        }

        playerJob?.cancel()
    }

    override fun onPlay() {
        "onPlay @$absoluteAdapterPosition".logD(TAG)
        binding.apply {
            setSound(binding.root.context, videoAdapter?.isMuted?.value == true, false)

            if (videoPlayer.player != null) {
                if (typeMedia == TypeMedia.VIDEO || typeMedia == TypeMedia.LIVE) videoPlayer.toShow()
                else videoPlayer.toGone()
                ivBackground.toGone()
                progressBar.toGone()
                timeVideo = videoPlayer.player?.duration ?: 0
            }
            runJob()
        }
    }

    private fun runJob() = binding.apply {
        if (item.type == TypeMedia.LIVE) {
            cvTimeVideo.toGone()
        } else {
            if (playerJob == null || playerJob?.isCancelled == true) {
                setupVideoDuration().also {
                    playerJob?.start()
                }
            }
        }
    }

    private fun ExoPlayer.playVideo() {
        stop()
        initialize(videoPlayer = binding.videoPlayer, title = "title", url = item.url)
        binding.videoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    }
}
