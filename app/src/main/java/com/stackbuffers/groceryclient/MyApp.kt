package com.stackbuffers.groceryclient

import android.app.Application
import com.google.firebase.iid.FirebaseInstanceId
import com.stackbuffers.groceryclient.utils.GlideImageLoadingService
import ss.com.bannerslider.Slider

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Slider.init(GlideImageLoadingService(this))
    }

    companion object{
        const val FCM_API = "https://fcm.googleapis.com/fcm/send"
        const val SERVER_KEY = "key=" + "AAAAYTFmUCc:APA91bH7dVsQF3eP4oF1sGwVir9KjygHJf5IGV0GxLoOS13JfyfxqXDJ4BLnl6BdxDUWXaMsSeQ1fq4Ro2TJ05JydESheTuT2jsFbczCJJa3vLNQAZ0IFJhb35WHo4vFccPhQZIEq5SE"
        const val CONTENT_TYPE = "application/json"
    }
}