package com.stackbuffers.groceryclient.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.stackbuffers.groceryclient.R
import kotlinx.android.synthetic.main.activity_coupons.*

class CouponsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupons)

        back.setOnClickListener {
            finish()
        }

        val categories = ArrayList<String>()
        categories.add("Tea & Beverages")
        categories.add("Tea & Beverages")
        categories.add("Tea & Beverages")
        categories.add("Tea & Beverages")
        categories.add("Tea & Beverages")
        categories.add("Tea & Beverages")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }
}