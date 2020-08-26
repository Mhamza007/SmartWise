package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_banned.*

class BannedActivity : AppCompatActivity() {

    private var usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private var contactUsRef = FirebaseDatabase.getInstance().getReference("/ContactUs")
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banned)

        sharedPreference = SharedPreference(this@BannedActivity)

        usersRef.child(sharedPreference.getUserId()!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reason.text = snapshot.child("Reason").value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@BannedActivity)
                }
            })

        contactUsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contact.text = snapshot.child("Email").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@BannedActivity)
            }

        })
    }
}