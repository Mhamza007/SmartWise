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
import kotlinx.android.synthetic.main.activity_coupons.*

class CouponsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupons)

        back.setOnClickListener {
            finish()
        }

        val categoryRef = FirebaseDatabase.getInstance().getReference("/Categories")
        categorySpinner.hint = "Select Category"
        val categories = ArrayList<String>()
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    categories.add(snap.child("Category_Name").value.toString())
                }
                categorySpinner.setItems(categories)
                categorySpinner.setOnItemSelectedListener { view, position, id, item ->
                    Toast.makeText(this@CouponsActivity, "$item", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CouponsActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        const val TAG = "CouponsActivity"
    }
}