package com.stackbuffers.groceryclient.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_help.*
import java.lang.Exception

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        back.setOnClickListener {
            finish()
        }

        feedbackMenu.setOnClickListener {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Utils.toast(this@HelpActivity, "App not Found")
            }
        }

        tcMenu.setOnClickListener {
            startActivity(Intent(this@HelpActivity, TermsConditionsActivity::class.java))
        }
    }
}