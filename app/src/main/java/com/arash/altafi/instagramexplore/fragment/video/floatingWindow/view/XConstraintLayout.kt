package com.arash.altafi.instagramexplore.fragment.video.floatingWindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout


class XConstraintLayout : ConstraintLayout {
    var dispatchTouchListener: ((ev: MotionEvent?) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            dispatchTouchListener?.invoke(ev)
        }
        return super.dispatchTouchEvent(ev)
    }
}