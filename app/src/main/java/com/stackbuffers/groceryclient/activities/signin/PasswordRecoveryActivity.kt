package com.stackbuffers.groceryclient.activities.signin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stackbuffers.groceryclient.R
import kotlinx.android.synthetic.main.activity_password_recovery.*

class PasswordRecoveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery)

        back.setOnClickListener {
            finish()
        }
    }
}