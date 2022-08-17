package com.arash.altafi.instagramexplore.utils

import android.content.Context
import android.content.res.Resources
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.ext.applyValue

object Utils {

    fun speedMedia(context: Context) =
        arrayOf(
            context.getString(R.string.speed_media).applyValue("0/25"),
            context.getString(R.string.speed_media).applyValue("0/5"),
            context.getString(R.string.speed_media).applyValue("0/75"),
            context.getString(R.string.normal),
            context.getString(R.string.speed_media).applyValue("1/25"),
            context.getString(R.string.speed_media).applyValue("1/5"),
            context.getString(R.string.speed_media).applyValue("2")
        )

    fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels
    fun getScreenHeight() = Resources.getSystem().displayMetrics.heightPixels

}