package com.stackbuffers.groceryclient.utils

import android.app.Activity
import android.util.DisplayMetrics

class Utils {
    companion object {
        fun getScreenWidth(activity: Activity): Int {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

            return displayMetrics.widthPixels
        }

        fun getScreenHeight(activity: Activity): Int {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

            return displayMetrics.heightPixels
        }
    }
}