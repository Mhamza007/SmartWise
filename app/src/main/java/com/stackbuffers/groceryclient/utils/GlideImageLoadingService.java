package com.stackbuffers.groceryclient.utils;

import android.content.Context;
import android.widget.ImageView;

import ss.com.bannerslider.ImageLoadingService;

public class GlideImageLoadingService implements ImageLoadingService {
    public Context context;

    public GlideImageLoadingService(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(String url, ImageView imageView) {
        GlideApp.with(context).load(url).into(imageView);
    }

    @Override
    public void loadImage(int resource, ImageView imageView) {
        GlideApp.with(context).load(resource).into(imageView);
    }

    @Override
    public void loadImage(String url, int placeHolder, int errorDrawable, ImageView imageView) {
        GlideApp.with(context).load(url).placeholder(placeHolder).error(errorDrawable).into(imageView);
    }
}
