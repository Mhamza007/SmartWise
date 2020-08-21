package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.GlideApp
import kotlinx.android.synthetic.main.activity_contact_us.*

class ContactUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        back.setOnClickListener {
            finish()
        }

        val contactUsRef = FirebaseDatabase.getInstance().getReference("/ContactUs")
        contactUsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                GlideApp.with(this@ContactUsActivity).load(snapshot.child("Map_image").value)
                    .placeholder(R.drawable.banner_two).into(map)
                contactAddress.text = snapshot.child("Address").value.toString()
                contactNumber.text = snapshot.child("Contact_Number").value.toString()
                contactEmail.text = snapshot.child("Email").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ContactUsActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}