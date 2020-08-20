package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stackbuffers.groceryclient.R
import kotlinx.android.synthetic.main.activity_points.*

class PointsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        back.setOnClickListener {
            finish()
        }
    }
}