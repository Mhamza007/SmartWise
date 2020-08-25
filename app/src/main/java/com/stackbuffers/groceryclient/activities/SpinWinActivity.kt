package com.stackbuffers.groceryclient.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_spin_win.*
import rubikstudio.library.LuckyWheelView
import rubikstudio.library.model.LuckyItem

class SpinWinActivity : AppCompatActivity() {

    private lateinit var data: ArrayList<LuckyItem>
    private var points = 0
    private lateinit var sharedPreference: SharedPreference
    private val spinWinRef = FirebaseDatabase.getInstance().getReference("/DailySpin/SpinToWin")
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")

    private val oneDay: Long = 86400000
    private val currentTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spin_win)

        sharedPreference = SharedPreference(this@SpinWinActivity)
        data = ArrayList()

        val luckyWheelView = findViewById<LuckyWheelView>(R.id.wheel)

        luckyWheelView.isEnabled = false
        luckyWheelView.isTouchEnabled = false

        val luckyItem1 = LuckyItem()
        luckyItem1.topText = "1"
        luckyItem1.color = -0xc20
        data.add(luckyItem1)

        val luckyItem2 = LuckyItem()
        luckyItem2.topText = "2"
        luckyItem2.color = -0x1f4e
        data.add(luckyItem2)

        val luckyItem3 = LuckyItem()
        luckyItem3.topText = "3"
        luckyItem3.color = -0x3380
        data.add(luckyItem3)

        val luckyItem4 = LuckyItem()
        luckyItem4.topText = "4"
        luckyItem4.color = -0xc20
        data.add(luckyItem4)

        val luckyItem5 = LuckyItem()
        luckyItem5.topText = "5"
        luckyItem5.color = -0x1f4e
        data.add(luckyItem5)

        val luckyItem6 = LuckyItem()
        luckyItem6.topText = "6"
        luckyItem6.color = -0x3380
        data.add(luckyItem6)

        val luckyItem7 = LuckyItem()
        luckyItem7.topText = "7"
        luckyItem7.color = -0xc20
        data.add(luckyItem7)

        val luckyItem8 = LuckyItem()
        luckyItem8.topText = "8"
        luckyItem8.color = -0x1f4e
        data.add(luckyItem8)

        luckyWheelView.setData(data)
        luckyWheelView.setRound(5)

        spin.setOnClickListener {
            val index: Int = getRandomIndex()
            luckyWheelView.startLuckyWheelWithTargetIndex(index)
        }

        usersRef.child(sharedPreference.getUserId()!!).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.hasChild("SpinTime")) {
                            val spinTime = snapshot.child("SpinTime").value.toString().toLong()
                            spin.isEnabled = currentTime - oneDay >= spinTime
                        } else {
                            spin.isEnabled = true
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@SpinWinActivity)
                }

            }
        )

        luckyWheelView.setLuckyRoundItemSelectedListener {
            spinWinRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (data[it].topText == "1") {
                        points = snapshot.child("First_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "2") {
                        points = snapshot.child("Second_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "3") {
                        points = snapshot.child("Third_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "4") {
                        points = snapshot.child("Fourth_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "5") {
                        points = snapshot.child("Fifth_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "6") {
                        points = snapshot.child("Sixth_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "7") {
                        points = snapshot.child("Seventh_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                    if (data[it].topText == "8") {
                        points = snapshot.child("Eight_Coupon").value.toString().toInt()
                        addPoints(points)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@SpinWinActivity)
                }

            })
        }
    }

    private fun addPoints(points: Int) {
        val map = HashMap<String, Any>()
        val time = System.currentTimeMillis()
        usersRef.child(sharedPreference.getUserId()!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.hasChild("Points")) {
                            val pt = snapshot.child("Points").value.toString().toInt()
                            val finalPoints = points + pt
                            map["Points"] = finalPoints
                            map["SpinTime"] = time
                            usersRef.child(sharedPreference.getUserId()!!)
                                .updateChildren(map)
                                .addOnCompleteListener {
                                    Utils.toast(
                                        this@SpinWinActivity,
                                        "$points points added"
                                    )
                                    spin.isEnabled = false
                                }.addOnFailureListener {
                                    Utils.toast(
                                        this@SpinWinActivity,
                                        "Failed"
                                    )
                                }
                        } else {
                            map["Points"] = points
                            map["SpinTime"] = time
                            usersRef.child(sharedPreference.getUserId()!!)
                                .updateChildren(map)
                                .addOnCompleteListener {
                                    Utils.toast(
                                        this@SpinWinActivity,
                                        "$points points added"
                                    )
                                    spin.isEnabled = false
                                }.addOnFailureListener {
                                    Utils.toast(
                                        this@SpinWinActivity,
                                        "Failed"
                                    )
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@SpinWinActivity)
                }

            })
    }

    private fun getRandomIndex(): Int {
        val rand = java.util.Random()
        return rand.nextInt(data.size - 1) + 0
    }
}