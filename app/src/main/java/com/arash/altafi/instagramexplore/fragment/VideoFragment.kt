package com.arash.altafi.instagramexplore.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.FragmentVideoBinding
import com.arash.altafi.instagramexplore.ext.*
import com.arash.altafi.instagramexplore.widget.CustomToolbar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

class VideoFragment : Fragment() {

    private lateinit var binding: FragmentVideoBinding
    private lateinit var fullScreen: AppCompatImageView
    private var toolbarView: CustomToolbar? = null
    private var player: ExoPlayer? = null
    private var isFullScreen = false
    private val args by navArgs<VideoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        setupToolbar(args.title)
        initializeVideo(
            args.title,
            args.url
        )
        return binding.root
    }

    private fun setupToolbar(title: String) {
        toolbarView = binding.toolbar
        toolbarView?.initToolbar(title = title)
    }

    private fun initializeVideo(title: String, url: String) {
        binding.apply {

            fullScreen = videoPlayer.findViewById(R.id.exo_fullscreen_button)
            val btnSpeed: AppCompatImageView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_playback_speed)
            val btnPause: AppCompatImageView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_pause)
            val btnPlay: AppCompatImageView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_play)

            val trackSelector = DefaultTrackSelector(requireContext())
            player = ExoPlayer.Builder(requireContext())
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(10000)
                .build()

            player?.initialize(videoPlayer, title, url)
            player?.seekTo(args.duration)

            if (videoPlayer.player?.isPlaying == true) {
                btnPause.toShow()
                btnPlay.toGone()
            } else {
                btnPause.toGone()
                btnPlay.toShow()
            }

            btnPause.setOnClickListener {
                onPause()
            }

            btnPlay.setOnClickListener {
                onResume()
            }

            @Suppress("DEPRECATION")
            fullScreen.setOnClickListener {
                if (isFullScreen) {
                    toolbar.toShow()
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    requireActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    requireActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    )
                    requireActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                    requireActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    )
                    isFullScreen = false
                } else {
                    toolbar.toGone()
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    )
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    )
                    isFullScreen = true
                }
            }

            btnSpeed.setOnClickListener {
                player?.speedDialog(requireContext())
            }

            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        btnPause.toShow()
                        btnPlay.toGone()
                    } else {
                        btnPause.toGone()
                        btnPlay.toShow()
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            progressBar.toGone()
                            player?.playWhenReady = true
                            "STATE_READY".logE("VideoPlayerFragment")
                        }
                        Player.STATE_BUFFERING -> {
                            progressBar.toShow()
                            videoPlayer.keepScreenOn = true
                            "STATE_BUFFERING".logE("VideoPlayerFragment")
                        }
                        Player.STATE_IDLE -> {
                            findNavController().navigateUp()
                            "STATE_IDLE".logE("VideoPlayerFragment")
                        }
                        Player.STATE_ENDED -> {
                            "STATE_ENDED".logE("VideoPlayerFragment")
                        }
                        else -> {
                            progressBar.toGone()
                            player?.playWhenReady = true
                        }
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onStop() {
        super.onStop()
        binding.videoPlayer.player?.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoPlayer.player?.playWhenReady
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player?.pause()
        if (isFullScreen) fullScreen.performClick()
    }

}