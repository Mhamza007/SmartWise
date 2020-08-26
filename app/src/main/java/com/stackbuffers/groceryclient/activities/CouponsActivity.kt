package com.stackbuffers.groceryclient.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.Coupon
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_coupons.*

class CouponsActivity : AppCompatActivity() {

    private val couponsRef = FirebaseDatabase.getInstance().getReference("/Coupon")
    private lateinit var couponsList: ArrayList<Coupon>
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupons)

        sharedPreference = SharedPreference(this@CouponsActivity)
        couponsList = ArrayList()

        back.setOnClickListener {
            finish()
        }

        couponsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    couponsList.add(
                        Coupon(
                            snap.child("Coupon").value.toString(),
                            snap.child("Coupon_ID").value.toString(),
                            snap.child("Discount_in_Perc").value.toString(),
                            snap.child("ExpiryDate").value.toString()
                        )
                    )

                    enter_code_btn.setOnClickListener {
                        val input = enter_code.text.toString().trim()
                        for (index in 0 until couponsList.size) {
                            when {
                                input.isEmpty() -> {
                                    enter_code.error = "Empty Code"
                                }
                                input == couponsList[index].Coupon -> {
                                    Utils.toast(
                                        this@CouponsActivity,
                                        "Coupon matched with discount percent ${couponsList[index].Discount_in_Perc}"
                                    )
                                    percentage.text = couponsList[index].Discount_in_Perc
                                    sharedPreference.setCouponDiscount(couponsList[index].Discount_in_Perc.toFloat() / 10)
                                }
//                                else -> {
//                                    enter_code.error = "Invalid Coupon Code"
//                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@CouponsActivity)
            }
        })
    }

    companion object {
        const val TAG = "CouponsActivity"
    }
}