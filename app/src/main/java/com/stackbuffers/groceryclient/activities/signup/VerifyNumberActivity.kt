package com.stackbuffers.groceryclient.activities.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import kotlinx.android.synthetic.main.activity_verify_number.*
import java.lang.Exception

class VerifyNumberActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var sharedPreference: SharedPreference

    private lateinit var phoneNumber: String
    private lateinit var verificationId: String
    private lateinit var token: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_number)

        auth = FirebaseAuth.getInstance()
        sharedPreference = SharedPreference(this)
        currentUserId = sharedPreference.getUserId()!!

        back.setOnClickListener {
            finish()
        }

        try {
            phoneNumber = intent.getStringExtra("phoneNumber")!!
            verificationId = intent.getStringExtra("verificationId")!!
            token = intent.extras?.get("token")!!
        } catch (e: Exception) {
            Log.e(TAG, "Number not found : $e")
        }

        verNumber.text = phoneNumber
        pinInput.setAnimationEnable(true)
        pinInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                verifyBtn.isEnabled = p0?.length == 6
                if (p0?.length == 6) {
                    verifyBtn.alpha = 1F
                } else {
                    verifyBtn.alpha = 0.5F
                }
            }
        })
        verifyBtn.setOnClickListener {
            val code = pinInput.text.toString().trim()
            val credentials = PhoneAuthProvider.getCredential(verificationId, code)

            auth.signInWithCredential(credentials)
                .addOnCompleteListener {
                    if (it.isSuccessful) {

                        val db = FirebaseDatabase.getInstance().getReference("/users")
                        db.child("$currentUserId/Mobile_Number")
                            .setValue(phoneNumber)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@VerifyNumberActivity,
                                        "Mobile Number Added Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, CongratulationsActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@VerifyNumberActivity,
                                        "Sign up Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this@VerifyNumberActivity,
                                    "Sign up Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@VerifyNumberActivity,
                            "Code Verification failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    if (it is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this@VerifyNumberActivity,
                            "The verification code entered was invalid",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@VerifyNumberActivity,
                            "Code Verification failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        private const val TAG = "VerifyNumberActivity"
    }
}