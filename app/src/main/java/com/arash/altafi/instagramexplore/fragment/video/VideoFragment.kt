package com.arash.altafi.instagramexplore.fragment.video

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.FragmentVideoBinding
import com.arash.altafi.instagramexplore.ext.*
import com.arash.altafi.instagramexplore.fragment.video.floatingWindow.VideoFloatingService
import com.arash.altafi.instagramexplore.fragment.video.floatingWindow.model.VideoItem
import com.arash.altafi.instagramexplore.widget.CustomToolbar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*

class VideoFragment : Fragment() {

    private lateinit var binding: FragmentVideoBinding
    private lateinit var fullScreen: AppCompatImageView
    private var toolbarView: CustomToolbar? = null
    private var player: ExoPlayer? = null
    private var isFullScreen = false
    private val args by navArgs<VideoFragmentArgs>()

    private var timeJob: Job? = null
    private var jobBackward: Job? = null
    private var jobForward: Job? = null
    private var timeLongPress = 1000L
    private var timeFast = 0L
    private var backFromPipPermission = false
    private val animationBlink by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private var overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(context)) {
            backFromPipPermission = true
            binding.videoPlayer.findViewById<AppCompatImageView>(R.id.exo_picture_in_picture_button)
                ?.performClick()

        } else {
            backFromPipPermission = false
            Log.e(TAG, "permission_floating_window_message")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        setupToolbar(args.title)
        initializeVideo(
            args.title,
            args.url,
            args.isLive
        )

        handleBackPress()
        handleSound()

        //Add Flag Screen On
        requireActivity().window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        return binding.root
    }

    private fun playVideo(
        title: String,
        url: String,
        duration: Long,
        isLive: Boolean,
        videoHeight: Int,
        videoWidth: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        if (!Settings.canDrawOverlays(requireActivity())) {
            askForDrawOverlayPermission()
            return
        }

        VideoFloatingService.play(
            requireContext(),
            VideoItem(title, url, duration, isLive, videoHeight, videoWidth)
        )
    }

    private fun askForDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(requireContext())) {
            return
        }

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${requireContext().packageName}")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isFullScreen) fullScreen.performClick()
                    findNavController().popBackStack()
                }
            })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleSound() = binding.apply {
        llSwipe.splitScreenForSoundAndBrightness(
            requireContext(),
            requireActivity()
        ) { isVolume, isDragging, percentage ->
            if (isVolume && isDragging) {
                llBrightness.toGone()
                tvVolume.text = percentage.plus("%")
                llVolume.toShow()
            } else if (isVolume.not() && isDragging) {
                llVolume.toGone()
                tvBrightness.text = percentage.plus("%")
                llBrightness.toShow()
            } else {
                videoPlayer.performClick()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeVideo(
        title: String, url: String, isLive: Boolean
    ) {
        binding.apply {

            fullScreen = videoPlayer.findViewById(R.id.exo_fullscreen_button)
            val btnSpeed: AppCompatImageView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_playback_speed)
            val btnPause: AppCompatImageView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_pause)
            val btnPlay: AppCompatImageView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_play)
            val btnPip: AppCompatImageView =
                videoPlayer.findViewById(R.id.exo_picture_in_picture_button)
            val btnMute: AppCompatImageView = videoPlayer.findViewById(R.id.ivMute)
            val ivForward: AppCompatImageView = videoPlayer.findViewById(R.id.ivForward)
            val ivRewind: AppCompatImageView = videoPlayer.findViewById(R.id.ivRewind)
            val tvPosition: MaterialTextView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_position)
            val tvDuration: MaterialTextView = videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_duration)

            val trackSelector = DefaultTrackSelector(requireContext())
            player = ExoPlayer.Builder(requireContext())
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(10000)
                .build()

            if (isLive) {
                btnSpeed.toGone()
                llFastBackward.toGone()
                llFastForward.toGone()
                llFastBackwardClick.toGone()
                llFastForwardClick.toGone()
                ivRewind.toGone()
                ivForward.toGone()
                /*ivRewind.setColorFilter(
                     ContextCompat.getColor(
                         requireContext(),
                         R.color.gray_brown
                     ), android.graphics.PorterDuff.Mode.MULTIPLY
                 )
                 ivForward.setColorFilter(
                     ContextCompat.getColor(
                         requireContext(),
                         R.color.gray_brown
                     ), android.graphics.PorterDuff.Mode.MULTIPLY
                 )*/
                ivRewind.disable()
                ivForward.disable()
                tvPosition.toGone()
                tvDuration.toGone()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                btnPip.toShow()

            player?.initialize(
                videoPlayer = videoPlayer,
                title = title,
                url = url,
                isLive = isLive
            )

            if (!isLive) player?.seekTo(args.duration)

            if (videoPlayer.player?.isPlaying == true) {
                btnPause.toShow()
                btnPlay.toGone()
            } else {
                btnPause.toGone()
                btnPlay.toShow()
            }

            btnMute.setOnClickListener {
                if (player?.volume != 0f) {
                    player?.volume = 0f
                    btnMute.setImageResource(R.drawable.ic_sound_off_player)
                } else {
                    player?.volume = 1f
                    btnMute.setImageResource(R.drawable.ic_sound_on_player)
                }
            }

            btnPause.setOnClickListener {
                player?.pause()
            }

            btnPlay.setOnClickListener {
                if ((player?.currentPosition ?: 0L) >= (player?.duration ?: 0L)) {
                    player?.seekTo(0)
                } else {
                    player?.play()
                }
            }

            btnPip.setOnClickListener {
                if (
                    player?.videoFormat?.height != null &&
                    player?.videoFormat?.width != null
                ) {
                    player?.pause()
                    playVideo(
                        title,
                        url,
                        videoPlayer.player?.currentPosition ?: 0L,
                        isLive,
                        player?.videoFormat!!.height,
                        player?.videoFormat!!.width
                    )
                }
            }

            ivForward.setOnClickListener {
                val currentTime = player?.currentPosition ?: 0L
                player?.seekTo(currentTime + 10000)
                if ((player?.currentPosition ?: 0L) >= ((player?.duration
                        ?: 0L) - 1L)
                ) {
                    player?.seekTo(player?.duration ?: 0L)
                }
            }

            ivRewind.setOnClickListener {
                val currentTime = player?.currentPosition ?: 0L
                player?.seekTo(currentTime - 5000)
            }

            llFastBackwardClick.setOnTouchListener { _, event ->
                val currentTime = player?.currentPosition ?: 0L
                if (event.action == MotionEvent.ACTION_DOWN) {
                    jobBackward = CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        videoPlayer.performClick()
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
                                    getString(R.string.second_fast)
                                        .applyValue("0")
                                } else {
                                    ivFastBackward.startAnimation(animationBlink)
                                    getString(R.string.second_fast)
                                        .applyValue(timeLongPress / 1000)
                                }
                            }
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    videoPlayer.performClick()
                    llFastBackward.setBackgroundColor(0)
                    jobBackward?.cancel()
                    timeJob?.cancel()
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
                        videoPlayer.performClick()
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
                                        getString(R.string.second_fast).applyValue("0")
                                    } else {
                                        ivFastForward.startAnimation(animationBlink)
                                        getString(R.string.second_fast)
                                            .applyValue((timeLongPress / 1000))
                                    }
                            }
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    videoPlayer.performClick()
                    llFastForward.setBackgroundColor(0)
                    jobForward?.cancel()
                    timeJob?.cancel()
                    timeLongPress = 1000L
                    timeFast = 0L
                    tvFastForward.toHide()
                    ivFastForward.toHide()
                    ivFastForward.clearAnimation()
                }
                true
            }

            fullScreen.setOnClickListener {
                if (isFullScreen) {
                    toolbar.toShow()
                    isFullScreen = false
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    toolbar.toGone()
                    isFullScreen = true
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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
                            "STATE_READY".logE(TAG)
                        }
                        Player.STATE_BUFFERING -> {
                            progressBar.toShow()
                            videoPlayer.keepScreenOn = true
                            "STATE_BUFFERING".logE(TAG)
                        }
                        Player.STATE_IDLE -> {
                            findNavController().navigateUp()
                            "STATE_IDLE".logE(TAG)
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
        if (!backFromPipPermission) player?.play()
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

    companion object {
        const val TAG = "VideoPlayerFragment"
    }

}