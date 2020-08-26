package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_terms_conditions.*

class TermsConditionsActivity : AppCompatActivity() {

    val tcRef = FirebaseDatabase.getInstance().getReference("/Term&Condition/Term&Conditions")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_conditions)

        back.setOnClickListener {
            finish()
        }

        tcRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tcText.text = snapshot.child("TermConditions").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@TermsConditionsActivity)
            }
        })
    }
}