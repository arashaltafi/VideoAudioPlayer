package com.arash.altafi.instagramexplore.fragment.video.floatingWindow.view

import android.view.WindowManager
import com.arash.altafi.instagramexplore.fragment.video.floatingWindow.view.XConstraintLayout

class FloatingRefreshView(
    private val windowManager: WindowManager,
    private val container: XConstraintLayout,
    val params: WindowManager.LayoutParams
) {
    fun updateView(): Unit = windowManager.updateViewLayout(container, params)
}