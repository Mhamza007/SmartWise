package com.stackbuffers.groceryclient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_points.*

class PointsActivity : AppCompatActivity() {

    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private val financeRef = FirebaseDatabase.getInstance().getReference("/Finance")
    private val pointsRateRef = FirebaseDatabase.getInstance().getReference("/CoinRate")
    private lateinit var sharedPreference: SharedPreference
    var orderCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        sharedPreference = SharedPreference(this@PointsActivity)
        back.setOnClickListener {
            finish()
        }

        usersRef.child(sharedPreference.getUserId()!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("Points")) {
                        points.text = snapshot.child("Points").value.toString()
                    }
                    if (snapshot.hasChild("orderCount"))
                        orderCount = snapshot.child("orderCount").value.toString().toInt()

                    financeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {

                                val silverStart =
                                    snapshot.child("SilverStart").value.toString().toInt()
                                val silverEnd = snapshot.child("SilverEnd").value.toString().toInt()
                                val goldStart = snapshot.child("GoldStart").value.toString().toInt()
                                val goldEnd = snapshot.child("GoldEnd").value.toString().toInt()
                                val platStart =
                                    snapshot.child("PlatinumStart").value.toString().toInt()
                                val platEnd = snapshot.child("PlatinumEnd").value.toString().toInt()

                                pointsRateRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val silverRate =
                                            snapshot.child("silverRate").value.toString()
                                        val goldRate = snapshot.child("goldRate").value.toString()
                                        val platRate = snapshot.child("platRate").value.toString()

//                                        when (orderCount) {
//                                            in silverStart..silverEnd -> {
                                                GlideApp.with(this@PointsActivity)
                                                    .load(R.drawable.silver_coin).into(silverCoin)
                                                silverCoinRate.text = "$$silverRate"
//                                                each.text = getString(R.string.each) + silverRate
//                                            }
//                                            in goldStart..goldEnd -> {
                                                GlideApp.with(this@PointsActivity)
                                                    .load(R.drawable.gold_coin).into(goldCoin)
                                                goldCoinRate.text = "$$goldRate"
//                                                each.text = getString(R.string.each) + goldRate
//                                            }
//                                            in platStart..platEnd -> {
                                                GlideApp.with(this@PointsActivity)
                                                    .load(R.drawable.plat_coin).into(platCoin)
                                                platCoinRate.text = "$$platRate"
//                                                each.text = getString(R.string.each) + platRate
//                                            }
//                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Utils.dbErToast(this@PointsActivity)
                                    }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.dbErToast(this@PointsActivity)
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@PointsActivity)
                }
            })
    }
}