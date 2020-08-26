package com.stackbuffers.groceryclient.utils;

import com.stackbuffers.groceryclient.R;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class NewArrivalSliderAdapter extends SliderAdapter {

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder viewHolder) {
        switch (position) {
            case 0:
                viewHolder.bindImageSlide(R.drawable.new_arrivals);
                break;
            case 1:
                viewHolder.bindImageSlide(R.drawable.new_arrivals);
                break;
            case 2:
                viewHolder.bindImageSlide(R.drawable.new_arrivals);
                break;
        }
    }
}