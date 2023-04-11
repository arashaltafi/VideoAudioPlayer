package com.arash.altafi.instagramexplore.fragment.music

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.*

class MusicFragment : Fragment() {

    private lateinit var binding: FragmentMusicBinding
    private val args by navArgs<MusicFragmentArgs>()
    private var toolbarView: CustomToolbar? = null
    private var player: ExoPlayer? = null
    private val fftAudioProcessor = FFTAudioProcessor()

    private var retryJob: Job? = null
    private var timeJob: Job? = null
    private var jobBackward: Job? = null
    private var jobForward: Job? = null
    private var timeLongPress = 1000L
    private var timeFast = 0L
    private val animationBlink by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
    }

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
        handleRetry()
        handleSound()
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleSound() = binding.apply {
        clSwipe.splitScreenForSoundAndBrightness(
            requireContext(),
            requireActivity()
        ) { isVolume, isDragging, percentage ->
            if (isVolume && isDragging) {
                llBrightness.toGone()
                tvVolume.text = percentage.plus("%")
                llVolume.toShow()
                bvVolume.value = percentage.toInt()
            } else if (isVolume.not() && isDragging) {
                llVolume.toGone()
                tvBrightness.text = percentage.plus("%")
                llBrightness.toShow()
                bvBrightness.value = percentage.toInt()
            } else {
                root.postDelayed({
                    llVolume.toGone()
                    llBrightness.toGone()
                }, 1000)
            }
        }
    }

    private fun setupToolbar(title: String) {
        toolbarView = binding.toolbar
        toolbarView?.initToolbar(title = title)
        toolbarView?.onBackClickToolbar = {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor", "SetTextI18n")
    private fun initializeMusic(title: String, url: String, background: String = "") {
        binding.apply {
            context?.sendBroadcast(
                Intent(
                    "PAUSE_PIP"
                )
            )

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

            val speedBtn: AppCompatImageView =
                musicPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_playback_speed)
            val btnPause: AppCompatImageView =
                musicPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_pause)
            val btnPlay: AppCompatImageView =
                musicPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_play)

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

            player?.initialize(musicPlayer = musicPlayer, title = title, url = url)
            player?.seekTo(args.duration)

            if (musicPlayer.player?.isPlaying == true) {
                btnPause.toShow()
                btnPlay.toGone()
            } else {
                btnPause.toGone()
                btnPlay.toShow()
            }

            llFastBackwardClick.setOnTouchListener { _, event ->
                val currentTime = player?.currentPosition ?: 0L
                if (event.action == MotionEvent.ACTION_DOWN) {
                    jobBackward = CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        tvFastBackward.toShow()
                        ivFastBackward.toShow()
                        ivFastBackward.startAnimation(animationBlink)
                        llFastBackward.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.background_fast_backward
                        )
                        timeJob = CoroutineScope(Dispatchers.Main).launch {
                            repeat(Int.MAX_VALUE) {
                                delay(500)
                                timeFast += 500
                                timeLongPress += timeFast
                                player?.seekTo(currentTime - timeLongPress)
                                tvFastBackward.text = if ((player?.currentPosition ?: 0L) <= 0L) {
                                    ivFastBackward.clearAnimation()
                                    requireContext().getString(R.string.second_fast)
                                        .applyValue("0")
                                } else {
                                    ivFastBackward.startAnimation(animationBlink)
                                    requireContext().getString(R.string.second_fast)
                                        .applyValue(timeLongPress / 1000)
                                }
                            }
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    llFastBackward.setBackgroundColor(0)
                    timeJob?.cancel()
                    jobBackward?.cancel()
                    timeLongPress = 1000L
                    timeFast = 0L
                    tvFastBackward.toHide()
                    ivFastBackward.toHide()
                    ivFastBackward.clearAnimation()
                }
                true
            }

            llFastForwardClick.setOnTouchListener { _, event ->
                val currentTime = player?.currentPosition ?: 0L
                if (event.action == MotionEvent.ACTION_DOWN) {
                    jobForward = CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        tvFastForward.toShow()
                        ivFastForward.toShow()
                        ivFastForward.startAnimation(animationBlink)
                        llFastForward.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.background_fast_forward
                        )
                        timeJob = CoroutineScope(Dispatchers.Main).launch {
                            repeat(Int.MAX_VALUE) {
                                delay(500)
                                timeFast += 500
                                timeLongPress += timeFast
                                player?.seekTo(currentTime + timeLongPress)
                                tvFastForward.text =
                                    if ((player?.currentPosition ?: 0L) >= ((player?.duration
                                            ?: 0L) - 1L)
                                    ) {
                                        player?.seekTo(player?.duration ?: 0L)
                                        ivFastForward.clearAnimation()
                                        requireContext().getString(R.string.second_fast)
                                            .applyValue("0")
                                    } else {
                                        ivFastForward.startAnimation(animationBlink)
                                        requireContext().getString(R.string.second_fast)
                                            .applyValue((timeLongPress / 1000))
                                    }
                            }
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    llFastForward.setBackgroundColor(0)
                    timeJob?.cancel()
                    jobForward?.cancel()
                    timeLongPress = 1000L
                    timeFast = 0L
                    tvFastForward.toHide()
                    ivFastForward.toHide()
                    ivFastForward.clearAnimation()
                }
                true
            }

            /*btnMute.setOnClickListener {
                if (player?.volume != 0f) {
                    player?.volume = 0f
                    btnMute.setImageResource(R.drawable.ic_sound_off_player)
                } else {
                    player?.volume = 1f
                    btnMute.setImageResource(R.drawable.ic_sound_on_player)
                }
            }*/

            btnPause.setOnClickListener {
                player?.pause()
            }

            btnPlay.setOnClickListener {
                musicPlayer.player?.playWhenReady
                player?.play()
            }

            speedBtn.setOnClickListener {
                player?.speedDialog(requireContext())
            }

            visualizer.processor = fftAudioProcessor

            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        "isPlaying = true".logE(TAG)
                        btnPause.toShow()
                        btnPlay.toGone()
                    } else {
                        "isPlaying = false".logE(TAG)
                        btnPause.toGone()
                        btnPlay.toShow()
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            progressBar.toGone()
                            player?.playWhenReady = true
                            "STATE_READY".logE(TAG)
                        }
                        Player.STATE_BUFFERING -> {
                            progressBar.toShow()
                            musicPlayer.keepScreenOn = true
                            "STATE_BUFFERING".logE(TAG)
                        }
                        Player.STATE_IDLE -> {
                            "STATE_IDLE".logE(TAG)
                            popError("Connection Error")
                            retryJob?.start()
//                            findNavController().navigateUp()
                        }
                        Player.STATE_ENDED -> {
                            "STATE_ENDED".logE(TAG)
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

    private fun handleRetry() {
        retryJob = CoroutineScope(Dispatchers.Main).launch {
            repeat(Int.MAX_VALUE) {
                player?.prepare()
                delay(10000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        timeJob?.cancel()
        jobForward?.cancel()
        jobBackward?.cancel()
        binding.apply {
            llFastBackward.setBackgroundColor(0)
            llFastForward.setBackgroundColor(0)
            timeLongPress = 1000L
            timeFast = 0L
            tvFastBackward.toHide()
            ivFastBackward.toHide()
            tvFastForward.toHide()
            ivFastForward.toHide()
            ivFastBackward.clearAnimation()
            ivFastForward.clearAnimation()
        }
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

    private companion object {
        const val TAG = "MusicFragment"
    }

}