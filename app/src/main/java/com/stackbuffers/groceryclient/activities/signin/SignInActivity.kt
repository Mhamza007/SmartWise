package com.stackbuffers.groceryclient.activities.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.signup.SignUpActivity
import com.stackbuffers.groceryclient.utils.SharedPreference
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var pw: String
    private lateinit var currentUserId: String

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        sharedPreference = SharedPreference(this)

        back.setOnClickListener {
            finish()
        }

        forget.setOnClickListener {
            startActivity(Intent(this, PasswordRecoveryActivity::class.java))
        }

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        signInBtn.setOnClickListener {
            email = emailAddress.text.toString().trim()
            pw = password.text.toString().trim()
            if (email.isEmpty() && pw.isEmpty()) {
                emailAddress.error = "Empty Email"
                password.error = "Empty Password"
            } else if (email.isEmpty()) {
                emailAddress.error = "Empty Email"
            } else if (pw.isEmpty()) {
                password.error = "Empty Password"
            } else {
                auth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            sharedPreference.setUserId(auth.currentUser!!.uid)
                            Toast.makeText(
                                this@SignInActivity,
                                "Signed In Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@SignInActivity,
                                "Signed In Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        when (it) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Invalid Email and Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "User not found or is invalid",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Signed In Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}