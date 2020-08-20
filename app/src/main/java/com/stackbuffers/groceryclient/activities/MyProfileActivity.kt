package com.stackbuffers.groceryclient.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.cart_item.*

class MyProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        sharedPreference = SharedPreference(this)

        val profileRef = FirebaseDatabase.getInstance().getReference("/users")
        profileRef.child(sharedPreference.getUserId()!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val imageUrl = snapshot.child("profileImageUrl").value.toString()
                    val username = snapshot.child("Name").value.toString()
                    val email = snapshot.child("Email").value.toString()
                    val city = snapshot.child("City").value.toString()
                    val number = snapshot.child("Mobile_Number").value.toString()
                    val address = snapshot.child("Address").value.toString()

                    GlideApp.with(this@MyProfileActivity).load(imageUrl)
                        .placeholder(R.drawable.profile_image).into(profileImage)
                    userName.text = username
                    userEmail.text = email
                    userCity.text = city
                    userNumber.text = number
                    userAddress.text = address
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyProfileActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })

        back.setOnClickListener {
            finish()
        }

        coin_card.setOnClickListener {
            startActivity(Intent(this@MyProfileActivity, PointsActivity::class.java))
        }
    }
}