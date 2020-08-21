package com.stackbuffers.groceryclient.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.widget.Toast

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

        fun dbErToast(context: Context) {
            Toast.makeText(context, "Database Error", Toast.LENGTH_SHORT).show()
        }

        fun toast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}