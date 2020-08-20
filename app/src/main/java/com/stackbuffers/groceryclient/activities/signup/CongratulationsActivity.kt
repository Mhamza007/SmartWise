package com.stackbuffers.groceryclient.activities.signup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stackbuffers.groceryclient.R
import kotlinx.android.synthetic.main.activity_congratulations.*

class CongratulationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)

        done.setOnClickListener {
            finish()
        }
    }
}