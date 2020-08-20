package com.stackbuffers.groceryclient.utils;

import com.stackbuffers.groceryclient.R;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class MainSliderAdapter extends SliderAdapter {

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder viewHolder) {
        switch (position) {
            case 0:
                viewHolder.bindImageSlide(R.drawable.banner_one);
                break;
            case 1:
                viewHolder.bindImageSlide(R.drawable.banner_two);
                break;
            case 2:
                viewHolder.bindImageSlide(R.drawable.banner_three);
                break;
        }
    }
}