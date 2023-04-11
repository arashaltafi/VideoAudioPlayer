package com.arash.altafi.instagramexplore.ext

import android.app.Activity
import android.view.Gravity
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.arash.altafi.instagramexplore.R
import com.tapadoo.alerter.Alerter

fun Fragment.popSuccess(
    @StringRes text: Int? = null,
    @StringRes title: Int? = null,
    duration: Long? = null, gravity: Int? = null
) = popSuccess(text?.let { getString(it) }, title?.let { getString(it) },
    duration, gravity
)

fun Fragment.popError(
    @StringRes text: Int? = null, @StringRes title: Int? = null,
    duration: Long? = null, gravity: Int? = null
) = popError(text?.let { getString(it) }, title?.let { getString(it) },
    duration, gravity
)

fun Fragment.popLoading(
    @StringRes text: Int? = null, @StringRes title: Int? = null,
    duration: Long? = null, gravity: Int? = null
) = popLoading(text?.let { getString(it) }, title?.let { getString(it) },
    duration, gravity
)

fun Fragment.popSuccess(
    text: String? = null,
    title: String? = null,
    duration: Long? = null, gravity: Int? = null
) = requireActivity().popSuccess(text, title, duration, gravity)

fun Fragment.popError(
    text: String? = null, title: String? = null,
    duration: Long? = null, gravity: Int? = null
) = requireActivity().popError(text, title, duration, gravity)

fun Fragment.popLoading(
    text: String? = null, title: String? = null,
    duration: Long? = null, gravity: Int? = null
) = requireActivity().popLoading(text, title, duration, gravity)


fun Activity.popSuccess(
    @StringRes text: Int? = null,
    @StringRes title: Int? = null,
    duration: Long? = null, gravity: Int? = null
) = popSuccess(
    text?.let { getString(it) }, title?.let { getString(it) },
    duration, gravity
)

fun Activity.popError(
    @StringRes text: Int? = null,
    @StringRes title: Int? = null,
    duration: Long? = null, gravity: Int? = null
) = popError(text?.let { getString(it) }, title?.let { getString(it) },
    duration, gravity
)

fun Activity.popLoading(
    @StringRes text: Int? = null,
    @StringRes title: Int? = null,
    duration: Long? = null, gravity: Int? = null
) = popLoading(text?.let { getString(it) }, title?.let { getString(it) },
    duration, gravity
)

fun Activity.popSuccess(
    text: String? = null,
    title: String? = null,
    duration: Long? = null, gravity: Int? = null
) = popMessage(
    text, title, duration, gravity
).apply { success() }.show()

fun Activity.popError(
    text: String? = null,
    title: String? = null,
    duration: Long? = null, gravity: Int? = null
) = popMessage(
    text, title, duration, gravity
).apply { error() }.show()

fun Activity.popLoading(
    text: String? = null,
    title: String? = null,
    duration: Long? = null, gravity: Int? = null
) = popMessage(
    text, title, duration, gravity
).apply { loading() }.show()

private fun Activity.popMessage(
    text: String? = null, title: String? = null,
    duration: Long? = null, gravity: Int? = null
): Alerter {
    ("$text\n$title").logE("popMessage")

    return Alerter.create(this).apply {
        default(duration ?: 3000, gravity ?: Gravity.TOP)
        title?.let { setTitle(it) }
        text?.let { setText(it) }
    }
}

fun Alerter.default(
    duration: Long = 3000,
    gravity: Int
): Alerter {
    if (gravity == Gravity.BOTTOM)
        bottom() else top()

    enableIconPulse(false)
    setTitleAppearance(R.style.txt_title)
    setTextAppearance(R.style.txt_desc)
    enableClickAnimation(false)
    setDuration(duration)
    return this
}

fun Alerter.top(): Alerter {
    setLayoutGravity(Gravity.TOP)
//    setEnterAnimation(R.anim.slide_up)// FIXME: 6/22/2021 animation has issue
//    setExitAnimation(R.anim.slide_down)// FIXME: 6/22/2021 default animations is good :)
    return this
}

fun Alerter.bottom(): Alerter {
    setLayoutGravity(Gravity.BOTTOM)
//    setEnterAnimation(R.anim.slide_fade_in_bottom)//
//    setExitAnimation(R.anim.slide_fade_out_bottom)//
    return this
}

fun Alerter.error(): Alerter {
    setBackgroundResource(R.drawable.bg_alert_error)
    setIcon(R.drawable.ic_error)

    return this
}

fun Alerter.success(): Alerter {
    setBackgroundResource(R.drawable.bg_alert_success)
//    setBackgroundColorInt(Color.BLUE)
    setIcon(R.drawable.ic_success)
    return this
}

fun Alerter.info(): Alerter {
    setBackgroundResource(R.drawable.bg_alert_info)
    setIcon(R.drawable.ic_info)

    return this
}

fun Alerter.loading(): Alerter {
    showIcon(false)
    enableInfiniteDuration(true)
    enableProgress(true)
    return this
}

