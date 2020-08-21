package com.stackbuffers.groceryclient.activities.signin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_password_recovery.*

class PasswordRecoveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery)

        back.setOnClickListener {
            finish()
        }

        signInBtn.setOnClickListener {
            if (emailAddress.text.toString().trim().isEmpty()) {
                emailAddress.error = "Empty Email"
            } else if (!emailAddress.text.toString().trim().contains("@")) {
                emailAddress.error = "Invalid Email"
            } else {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(emailAddress.text.toString().trim())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Utils.toast(
                                this@PasswordRecoveryActivity,
                                "An Email sent to ${emailAddress.text}"
                            )
                        } else {
                            Utils.toast(this@PasswordRecoveryActivity, "Error Sending Email")
                        }
                    }.addOnFailureListener {
                        if (it is FirebaseAuthInvalidUserException) {
                            Utils.toast(this@PasswordRecoveryActivity, "Invalid User")
                        } else {
                            Utils.toast(this@PasswordRecoveryActivity, "Error Sending Email")
                        }
                    }
            }
        }
    }
}