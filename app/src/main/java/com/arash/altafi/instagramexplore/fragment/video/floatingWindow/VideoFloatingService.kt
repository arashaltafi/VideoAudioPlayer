package com.arash.altafi.instagramexplore.fragment.video.floatingWindow

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.PointF
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.LayoutFloatingplayerBinding
import com.arash.altafi.instagramexplore.ext.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.arash.altafi.instagramexplore.fragment.video.floatingWindow.model.VideoItem
import com.arash.altafi.instagramexplore.fragment.video.floatingWindow.view.FloatingRefreshView
import com.arash.altafi.instagramexplore.fragment.video.floatingWindow.view.XConstraintLayout

class VideoFloatingService : Service(), Player.Listener {
    private val viewBinding: LayoutFloatingplayerBinding by lazy {
        val inflater = LayoutInflater.from(this)
        LayoutFloatingplayerBinding.inflate(inflater)
    }

    private lateinit var videoURl: String
    private lateinit var videoTitle: String
    private var isLive = false
    private lateinit var ivForward: AppCompatImageView
    private lateinit var ivRewind: AppCompatImageView
    private lateinit var btnPlay: AppCompatImageView
    private lateinit var btnPause: AppCompatImageView
    private var floatingRefreshView: FloatingRefreshView? = null
    private val windowManager: WindowManager
        get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setPlayer(viewBinding)
        initListeners(viewBinding)
        btnPlay.toGone()
        btnPause.isClickable = false
        this.registerReceiver(
            broadcastReceiver,
            IntentFilter("PAUSE_PIP")
        )

    }

    val player: ExoPlayer by lazy {
        val maxWidth = getScreenWidth()
        val maxHeight = getScreenHeight() / 3

        val trackSelector = DefaultTrackSelector(
            this,
            DefaultTrackSelector
                .Parameters
                .Builder(this)
                .setMaxVideoSize(maxWidth, maxHeight)
                .build()
        )

        ExoPlayer
            .Builder(this)
            .setTrackSelector(trackSelector)
            .build()
    }

    private fun initListeners(binding: LayoutFloatingplayerBinding) = binding.apply {
        btnStop.setOnClickListener { stopService() }
        btnOpenInFull.setOnClickListener {
            this@VideoFloatingService.sendBroadcast(
                Intent(
                    "BACK_FROM_VIDEO_PLAYER_FRAGMENT"
                ).apply {
                    putExtra("VIDEO_PLAYER_TITLE", videoTitle)
                    putExtra("PIP_VIDEO_URL", videoURl)
                    putExtra("PIP_CURRENT_POSITION", player.currentPosition)
                    putExtra("PIP_IS_LIVE", isLive)
                })
            stopService()
        }

        btnMute.setOnClickListener {
            if (player.volume != 0f) {
                player.volume = 0f
                binding.btnMute.setImageResource(R.drawable.ic_sound_off_player)
            } else {
                player.volume = 1f
                binding.btnMute.setImageResource(R.drawable.ic_sound_on_player)
            }
        }

        btnPlay = binding.videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_play)
        btnPause = binding.videoPlayer.findViewById(com.google.android.exoplayer2.R.id.exo_pause)
        ivForward = binding.videoPlayer.findViewById(R.id.ivForward)
        ivRewind = binding.videoPlayer.findViewById(R.id.ivRewind)

        btnPlay.setOnClickListener {
            if (player.currentPosition >= player.duration) {
                player.seekTo(0)
            } else {
                player.play()
            }
        }

        btnPause.setOnClickListener {
            player.pause()
        }

        ivForward.setOnClickListener {
            val currentTime = player.currentPosition
            player.seekTo(currentTime + 10000)
            if (player.currentPosition >= (player.duration - 1L)
            ) {
                player.seekTo(player.duration)
            }
        }

        ivRewind.setOnClickListener {
            val currentTime = player.currentPosition
            player.seekTo(currentTime - 5000)
        }

        binding.videoPlayer.setControllerVisibilityListener(
            StyledPlayerView.ControllerVisibilityListener { visibility ->
                binding.btnStop.visibility = visibility
                binding.btnOpenInFull.visibility = visibility
                binding.btnMute.visibility = visibility
            }
        )

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                btnPause.isClickable = true
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    btnPause.toShow()
                    btnPlay.toGone()
                } else {
                    btnPause.toGone()
                    btnPlay.toShow()
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPlayer(view: LayoutFloatingplayerBinding) {
        view.videoPlayer.player = player
        player.addListener(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable(EXTRA_ITEM, VideoItem::class.java)
        } else {
            intent?.extras?.getParcelable(EXTRA_ITEM) as VideoItem?
        } ?: return START_STICKY


        if (floatingRefreshView == null) floatingRefreshView = showFloatingWindow(
            context = this,
            windowManager = windowManager,
            container = viewBinding.root,
            item.videoHeight,
            item.videoWidth
        )

        prePlay(
            videoURl = item.videoUrl,
            duration = item.duration,
            title = item.title,
            isLive = item.isLive
        )
        if (item.isLive) {
            ivRewind.toGone()
            ivForward.toGone()
        }

        isLive = item.isLive
        videoURl = item.videoUrl
        videoTitle = item.title
        return START_STICKY

    }

    override fun onDestroy() {
        player.release()
        windowManager.removeView(viewBinding.root)
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


    private fun stopService() {
        stopSelf()
    }

    private fun prePlay(
        videoURl: String,
        duration: Long,
        title: String,
        isLive: Boolean
    ) {
        player.initialize(
            videoPlayer = viewBinding.videoPlayer,
            title = title,
            url = videoURl,
            isLive = isLive
        )

        player.seekTo(duration)
    }

    private fun showFloatingWindow(
        context: Context,
        windowManager: WindowManager,
        container: XConstraintLayout,
        videoHeight: Int,
        videoWidth: Int
    ): FloatingRefreshView {
        val layoutFlag = getWindowLayoutFlag()

        val params = createFloatingWindowLayoutParams(
            context = context,
            layoutFlag = layoutFlag,
            videoHeight,
            videoWidth
        )

        makeResizableAndDraggable(
            windowManager = windowManager,
            containerView = container,
            params = params,
            getScreenWidth() / 3,
            getScreenHeight() / 3,
            getScreenWidth(),
            getScreenHeight()
        )

        windowManager.addView(container, params)

        return FloatingRefreshView(
            windowManager = windowManager,
            container = container,
            params = params
        )
    }

    private fun getWindowLayoutFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }

    private fun createFloatingWindowLayoutParams(
        context: Context,
        layoutFlag: Int,
        videoHeight: Int,
        videoWidth: Int
    ): WindowManager.LayoutParams {
        val height: Int
        val width: Int
        if (videoHeight > getScreenHeight() / 2) {
            height = getScreenHeight() / 2
            width = getScreenWidth() / 2
        } else if (videoWidth > getScreenWidth()) {
            height = videoHeight
            width = getScreenWidth()
        } else if (videoWidth < getScreenWidth() / 2 && videoHeight < getScreenHeight() / 3) {
            width = videoWidth * 2
            height = videoHeight * 2
        } else {
            height = videoHeight
            width = videoWidth
        }
        return WindowManager.LayoutParams(
            width,
            height,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun makeResizableAndDraggable(
        windowManager: WindowManager,
        containerView: XConstraintLayout,
        params: WindowManager.LayoutParams,
        minWidth: Int,
        minHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ) {
        var initialTouchX = 0f
        var initialTouchY = 0f
        var initialTouchX1 = 0f
        var initialTouchY1 = 0f
        var initialTouchX2 = 0f
        var initialTouchY2 = 0f
        var initialWidth = params.width
        var initialHeight = params.height

        containerView.dispatchTouchListener = { event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialTouchX1 = event.getX(0)
                    initialTouchY1 = event.getY(0)
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    initialTouchX2 = event.getX(1)
                    initialTouchY2 = event.getY(1)
                    initialWidth = params.width
                    initialHeight = params.height
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount >= 2) {
                        val newTouchX1 = event.getX(0)
                        val newTouchY1 = event.getY(0)
                        val newTouchX2 = event.getX(1)
                        val newTouchY2 = event.getY(1)

                        val newDistance = PointF(
                            newTouchX1, newTouchX2
                        ).length()

                        val initialDistance = PointF(
                            initialTouchX1, initialTouchX2
                        ).length()

                        val scale = newDistance / initialDistance
                        val newWidth = (initialWidth * scale).toInt()
                        val newHeight = (initialHeight * scale).toInt()

                        if (newWidth in (minWidth..maxWidth)) {
                            params.width = newWidth
                            params.height = newHeight
                        }
                    } else {
                        "ACTION_MOVE.".logE("makeResizableAndDraggable")
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }

                    windowManager.updateViewLayout(containerView, params)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> Unit
            }
        }
    }

    companion object {

        private const val EXTRA_ITEM = "extra_item"

        fun play(
            context: Context,
            videoItem: VideoItem
        ) {
            val intent = Intent(context, VideoFloatingService::class.java).also {
                it.putExtra(EXTRA_ITEM, videoItem)
            }
            context.startService(intent)
        }
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, intent: Intent) {
            if (intent.action == "PAUSE_PIP") {
                player.pause()
            }
        }
    }
}
