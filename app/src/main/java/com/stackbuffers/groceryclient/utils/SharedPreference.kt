package com.stackbuffers.groceryclient.utils

import android.content.Context

class SharedPreference(private val context: Context) {
    fun setUserId(userId: String) {
        val sharedPreferences =
            context.getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.apply()
    }

    fun getUserId(): String? {
        val sharedPreferences =
            context.getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", "")
    }

    fun setCouponDiscount(discount: Float) {
        val sharedPreferences =
            context.getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("discount", discount)
        editor.apply()
    }

    fun getCouponDiscount(): Float {
        val sharedPreferences =
            context.getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat("discount", 0F)
    }
}