package com.stackbuffers.groceryclient.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.beverages.ItemDetailsActivity
import com.stackbuffers.groceryclient.model.Banner
import ss.com.bannerslider.adapters.SliderAdapter
import ss.com.bannerslider.viewholder.ImageSlideViewHolder
import kotlin.properties.Delegates

class MainSliderAdapter(private val context: Context) : SliderAdapter() {
    private val bannersRef = FirebaseDatabase.getInstance().getReference("/Banners")
    private var bannerList: ArrayList<Banner> = ArrayList()

    init {
        bannersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.hasChild("Banner_Type") && it.child("Banner_Type").value.toString()
                            .equals("Home", ignoreCase = true)
                    ) {
                        bannerList.add(
                            Banner(
                                it.child("Banner_ID").value.toString(),
                                it.child("Banner_Image").value.toString(),
                                it.child("Banner_Type").value.toString(),
                                it.child("Category_ID").value.toString(),
                                it.child("Product_ID").value.toString(),
                                it.child("Sub_Categories_ID").value.toString()
                            )
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(context)
            }
        })
    }

    override fun getItemCount() = bannerList.size

    override fun onBindImageSlide(position: Int, viewHolder: ImageSlideViewHolder) {
        for (i in 0 until bannerList.size) {
            when (position) {
                i -> viewHolder.bindImageSlide(bannerList[position].Banner_Image)
            }
        }
    }
}