package com.stackbuffers.groceryclient

import android.app.Application
import com.stackbuffers.groceryclient.utils.GlideImageLoadingService
import ss.com.bannerslider.Slider

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Slider.init(GlideImageLoadingService(this))
    }
}