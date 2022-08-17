package com.arash.altafi.instagramexplore.fragment.music

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.FragmentMusicBinding
import com.arash.altafi.instagramexplore.ext.*
import com.arash.altafi.instagramexplore.widget.CustomToolbar
import com.arash.altafi.instagramexplore.widget.visualizer.FFTAudioProcessor
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.audio.*
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

class MusicFragment : Fragment() {

    private lateinit var binding: FragmentMusicBinding
    private val args by navArgs<MusicFragmentArgs>()
    private var toolbarView: CustomToolbar? = null
    private var player: ExoPlayer? = null
    private val fftAudioProcessor = FFTAudioProcessor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater, container, false)
        setupToolbar(args.title)
        initializeMusic(
            args.title,
            args.url,
            args.background,
        )
        return binding.root
    }

    private fun setupToolbar(title: String) {
        toolbarView = binding.toolbar
        toolbarView?.initToolbar(title = title)
        toolbarView?.onBackClickToolbar = {
            findNavController().navigateUp()
        }
    }

    private fun initializeMusic(title: String, url: String, background: String = "") {
        binding.apply {

            val renderersFactory = object : DefaultRenderersFactory(requireContext()) {
                override fun buildAudioRenderers(
                    context: Context,
                    extensionRendererMode: Int,
                    mediaCodecSelector: MediaCodecSelector,
                    enableDecoderFallback: Boolean,
                    audioSink: AudioSink,
                    eventHandler: Handler,
                    eventListener: AudioRendererEventListener,
                    out: ArrayList<Renderer>
                ) {
                    out.add(
                        MediaCodecAudioRenderer(
                            context,
                            mediaCodecSelector,
                            enableDecoderFallback,
                            eventHandler,
                            eventListener,
                            DefaultAudioSink(
                                AudioCapabilities.getCapabilities(context),
                                arrayOf(fftAudioProcessor)
                            )
                        )
                    )
                    super.buildAudioRenderers(
                        context,
                        extensionRendererMode,
                        mediaCodecSelector,
                        enableDecoderFallback,
                        audioSink,
                        eventHandler,
                        eventListener,
                        out
                    )
                }
            }

            val fullScreen: AppCompatImageView =
                musicPlayer.findViewById(R.id.exo_fullscreen_button)
            val speedBtn: AppCompatImageView = musicPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_playback_speed)
            val btnPause: AppCompatImageView = musicPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_pause)
            val btnPlay: AppCompatImageView = musicPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_play)

            fullScreen.toGone()
            ivMusic.toShow()
            ivBackground.toShow()
            ivMusic.setImage(background)
            ivBackground.setBlurImage(background)

            val trackSelector = DefaultTrackSelector(requireContext())
            player = ExoPlayer.Builder(requireContext(), renderersFactory)
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(10000)
                .build()

            player?.initialize(musicPlayer, title, url)
            player?.seekTo(args.duration)

            if (musicPlayer.player?.isPlaying == true) {
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

            speedBtn.setOnClickListener {
                player?.speedDialog(requireContext())
            }

            visualizer.processor = fftAudioProcessor

            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        "isPlaying = true".logE("MusicFragment")
                        btnPause.toShow()
                        btnPlay.toGone()
                    } else {
                        "isPlaying = false".logE("MusicFragment")
                        btnPause.toGone()
                        btnPlay.toShow()
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            progressBar.toGone()
                            player?.playWhenReady = true
                            "STATE_READY".logE("MusicFragment")
                        }
                        Player.STATE_BUFFERING -> {
                            progressBar.toShow()
                            musicPlayer.keepScreenOn = true
                            "STATE_BUFFERING".logE("MusicFragment")
                        }
                        Player.STATE_IDLE -> {
                            findNavController().navigateUp()
                            "STATE_IDLE".logE("MusicFragment")
                        }
                        Player.STATE_ENDED -> {
                            "STATE_ENDED".logE("MusicFragment")
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
        binding.musicPlayer.player?.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.musicPlayer.player?.playWhenReady
        player?.play()
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
    }

}