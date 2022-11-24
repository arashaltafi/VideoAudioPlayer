package com.arash.altafi.instagramexplore.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.utils.Utils.speedMedia
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext
import kotlin.math.hypot
import kotlin.math.roundToInt

@SuppressLint("LogNotTimber")
fun Any.logE(tag: String = "", throwable: Throwable? = null) {
    Log.e(tag, "$this\n", throwable)
}

@SuppressLint("LogNotTimber")
fun Any.logI(tag: String = "", throwable: Throwable? = null) {
    Log.i("nExt -> $tag", "$this\n", throwable)
}

@SuppressLint("LogNotTimber")
fun Any.logD(tag: String = "", throwable: Throwable? = null) {
    Log.d(tag, "$this\n", throwable)
}

inline fun <reified NEW> Any.isCastable(): Boolean {
    return this is NEW
}

inline fun <reified NEW> Any.cast(): NEW? {
    return if (this.isCastable<NEW>())
        this as NEW
    else null
}

fun View.toShow() {
    this.visibility = View.VISIBLE
}

fun View.isShow(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.toHide() {
    this.visibility = View.INVISIBLE
}

fun View.isHide(): Boolean {
    return this.visibility == View.INVISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}

fun String.applyValue(vararg args: Any?): String {
    return String.format(Locale.US, this, *args)
}

fun View.showKeyboard() {
    this.requestFocus()
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    } catch (e: java.lang.Exception) {
        "showKeyboard failed, error: $e".logE("showKeyboard")
    }
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun TextView.clear() {
    this.text = ""
}

fun Int.toPx(): Int {
    val displayMetrics = Resources.getSystem().displayMetrics
    return (this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Int.toDp(): Int {
    val displayMetrics = Resources.getSystem().displayMetrics
    return (this / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Float.toPx(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun EditText.textString() =
    this.text.toString()

enum class RevealModel {
    START,
    CENTER,
    END
}

fun View.reveal(
    duration: Long, model: RevealModel,
    endListener: (() -> Unit)? = null
) {
    val cxF = when (model) {
        RevealModel.START -> width
        RevealModel.CENTER -> width / 2
        else -> 0
    }

    val cyF = height / 2

    val radius = hypot(width.toDouble(), height.toDouble()).toFloat()

    ViewAnimationUtils.createCircularReveal(
        this, cxF, cyF, 0f, radius
    ).apply {
        setDuration(duration)
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                toShow()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                enable()
                endListener?.invoke()
            }
        })
    }.start()
}

fun View.unReveal(
    duration: Long, model: RevealModel,
    endListener: (() -> Unit)? = null
) {

    val cxF = when (model) {
        RevealModel.START -> right
        RevealModel.CENTER -> width / 2
        else -> left
    }

    val cyF = height / 2

    val radius = hypot(width.toDouble(), height.toDouble()).toFloat()

    ViewAnimationUtils.createCircularReveal(
        this, cxF, cyF, radius, 0f
    ).apply {
        setDuration(duration)
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                disable()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                toHide()
                endListener?.invoke()
            }
        })
    }.start()
}

fun EditText?.afterTextChange(afterTextChanged: (String) -> Unit): TextWatcher {
    var beforeText = ""
    val watcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            if (beforeText == editable.toString())
                return

            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            beforeText = s.toString()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    this?.addTextChangedListener(watcher)

    return watcher
}

fun <T> debounce(
    waitMs: Long = 300L,
    scope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

fun <T> debounceCancelable(
    waitMs: Long = 300L,
    scope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T?) -> Unit {
    var debounceJob: Job? = null
    return { param: T? ->
        debounceJob?.cancel()
        if (param != null)
            debounceJob = scope.launch {
                delay(waitMs)
                destinationFunction(param)
            }
    }
}

fun EditText.onChange(
    waitMs: Long = 800L,
    scope: CoroutineScope,
    destinationFunction: (String) -> Unit,
): TextWatcher = afterTextChange(debounce(waitMs, scope, destinationFunction))


fun SearchView.onChange(
    waitMs: Long = 800L,
    scope: CoroutineScope,
    destinationFunction: (String) -> Unit,
) {
    val f = debounceCancelable(waitMs, scope, destinationFunction)

    this.setOnQueryTextListener(object :
        SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            f.invoke(null)
            destinationFunction.invoke(query ?: "")

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            f.invoke(newText ?: "")

            return true
        }
    })
}

fun ExoPlayer.speedDialog(context: Context) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder.setTitle(context.getString(R.string.video_speed))
    builder.setItems(speedMedia(context)) { _, which ->
        if (which == 0) {
            val param = PlaybackParameters(0.25f)
            this.playbackParameters = param
        }
        if (which == 1) {
            val param = PlaybackParameters(0.5f)
            this.playbackParameters = param
        }
        if (which == 2) {
            val param = PlaybackParameters(0.75f)
            this.playbackParameters = param
        }
        if (which == 3) {
            val param = PlaybackParameters(1f)
            this.playbackParameters = param
        }
        if (which == 4) {
            val param = PlaybackParameters(1.25f)
            this.playbackParameters = param
        }
        if (which == 5) {
            val param = PlaybackParameters(1.5f)
            this.playbackParameters = param
        }
        if (which == 6) {
            val param = PlaybackParameters(2f)
            this.playbackParameters = param
        }
    }
    builder.show()
}

fun ExoPlayer.initialize(
    videoPlayer: com.google.android.exoplayer2.ui.StyledPlayerView? = null,
    musicPlayer: com.google.android.exoplayer2.ui.PlayerControlView? = null,
    title: String,
    url: String,
) {
    videoPlayer?.player = this
    musicPlayer?.player = this

    val mediaItem: MediaItem = MediaItem.Builder()
        .setUri(url)
//        .setMimeType(MimeTypes.APPLICATION_MP4) //For Videos and Mp3
//        .setMimeType(MimeTypes.APPLICATION_M3U8) //For Stream Live Videos
        .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
        .setLiveConfiguration(
            MediaItem.LiveConfiguration.Builder()
                .setMaxPlaybackSpeed(1.02f)
                .build()
        )
        .build()
    this.setMediaItem(mediaItem)
    this.prepare()
    this.playWhenReady = true
    videoPlayer?.requestFocus()
    musicPlayer?.requestFocus()
    videoPlayer?.setShowFastForwardButton(true)
    musicPlayer?.setShowFastForwardButton(true)
    videoPlayer?.setShowNextButton(false)
    musicPlayer?.setShowNextButton(false)
    videoPlayer?.setShowPreviousButton(false)
    musicPlayer?.setShowPreviousButton(false)
}

fun Long.convertDurationToTime(): String {
    val convertHours = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toHours(this)
    )
    val convertMinutes = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(this))
    )
    val convertSeconds = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(this))
    )
    return if (this > 3600000) "$convertHours:$convertMinutes:$convertSeconds" else "$convertMinutes:$convertSeconds"
}

fun String.captureImage(): Bitmap? {
    val mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource(this, HashMap())
    return mediaMetadataRetriever.getFrameAtTime(1000)
}

fun View.doubleClick(context: Context, onDoubleTap: (() -> Unit)) {
    val gestureDetector =
        GestureDetectorCompat(context, object :
            GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap.invoke()
                return true
            }
        })
    this.setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_BUTTON_RELEASE)
            v.performClick()
        return@setOnTouchListener gestureDetector.onTouchEvent(event)
    }
}

fun ImageView.setBlurImage(url: Int) {
    Picasso.get()
        .load(url)
        .transform(BlurTransformation(context, 25, 1))
        .into(this)
}

fun ImageView.setBlurImage(url: String) {
    Picasso.get()
        .load(url)
        .transform(BlurTransformation(context, 25, 1))
        .into(this)
}

fun ImageView.setImage(url: String, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
    Picasso.get()
        .load(url)
        .into(this, object: Callback {
            override fun onSuccess() {
                onSuccess?.invoke()
            }
            override fun onError(e: Exception?) {
                onError?.invoke()
            }
        })
}
fun Context.share(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.shareTextWithImage(
    applicationId: String,
    bitmap: Bitmap,
    body: String,
    title: String,
    subject: String
) {
    val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    out.close()
    val bmpUri = if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(file)
    } else {
        FileProvider.getUriForFile(
            this, "$applicationId.fileprovider", file
        )
    }

    val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_TEXT, title + "\n\n" + body)
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, bmpUri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share News")
    startActivity(shareIntent)
}

fun ImageView.setImage(drawable: Int) {
    Picasso.get()
        .load(drawable)
        .into(this)
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.openURL(url: String) {
    try {
        val fullUrl = if (url.startsWith("http")) url else "http://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun Context.openDownloadURL(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    when {
        this.isInstalled("com.android.chrome") -> intent.setPackage("com.android.chrome")
        this.isInstalled("org.mozilla.firefox") -> intent.setPackage("org.mozilla.firefox")
        this.isInstalled("com.opera.mini.android") -> intent.setPackage("com.opera.mini.android")
        this.isInstalled("com.opera.mini.android.Browser") -> intent.setPackage("com.opera.mini.android.Browser")
        else -> this.openURL(url)
    }
    startActivity(intent)
}

private fun Context.isInstalled(packageName: String): Boolean {
    return try {
        this.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun ViewModel.viewModel(
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launch { block(this) }


fun ViewModel.viewModelIO(
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launch(Dispatchers.IO) { block(this) }


val coroutineIO get() = CoroutineScope(Dispatchers.IO + Job())

fun ViewModel.viewModelCompute(
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launch(Dispatchers.Default) { block(this) }

val normalIO get() = CoroutineScope(Dispatchers.IO + Job())

fun normalIO(
    scope: CoroutineScope = normalIO,
    block: suspend CoroutineScope.() -> Unit,
) = scope.launch { coroutineScope(block) }

val supervisorIO get() = CoroutineScope(Dispatchers.IO + SupervisorJob())

fun supervisorIO(
    scope: CoroutineScope = supervisorIO,
    block: suspend CoroutineScope.() -> Unit,
) = scope.launch { supervisorScope(block) }

fun CoroutineScope.superLaunch(
    context: CoroutineContext? = null,
    block: suspend CoroutineScope.() -> Unit,
) = if (context != null)
    launch(context) { supervisorScope(block) }
else launch { supervisorScope(block) }

fun <T> CoroutineScope.asyncCompute(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = async(Dispatchers.Default, start, block)

fun <T> CoroutineScope.asyncIO(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = async(Dispatchers.IO, start, block)

fun <T> CoroutineScope.asyncMain(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = async(Dispatchers.Main.immediate, start, block)

suspend fun <T> withIO(
    block: suspend CoroutineScope.() -> T,
) = withContext(Dispatchers.IO) { block(this) }

suspend fun <T> withCompute(
    block: suspend CoroutineScope.() -> T,
) = withContext(Dispatchers.Default) { block(this) }

suspend fun <T> withMain(
    block: suspend CoroutineScope.() -> T,
) = withContext(Dispatchers.Main.immediate) { block(this) }