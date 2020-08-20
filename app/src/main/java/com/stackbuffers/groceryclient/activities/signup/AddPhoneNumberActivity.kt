package com.stackbuffers.groceryclient.activities.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import kotlinx.android.synthetic.main.activity_add_phone_number.*
import java.util.concurrent.TimeUnit

class AddPhoneNumberActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var phoneNumber: String
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_phone_number)

        back.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        sharedPreference = SharedPreference(this)
        currentUserId = sharedPreference.getUserId()!!

        ccp.registerCarrierNumberEditText(number)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$p0")

                val db = FirebaseDatabase.getInstance().getReference("/users")
                db.child("$currentUserId/Mobile_Number").setValue(phoneNumber)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@AddPhoneNumberActivity,
                                "Mobile Number Added Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddPhoneNumberActivity,
                                "Sign up Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@AddPhoneNumberActivity,
                            "Sign up Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        this@AddPhoneNumberActivity,
                        "Invalid Credentials",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(
                        this@AddPhoneNumberActivity,
                        "Too many requests from this number received, Try Again in few hours",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)

                Log.d(TAG, "Code Sent is $verificationId")
                Log.d(TAG, "Token is $token")

                val intent = Intent(this@AddPhoneNumberActivity, VerifyNumberActivity::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("verificationId", verificationId)
                intent.putExtra("token", token)
                startActivity(intent)
            }
        }

        sendBtn.setOnClickListener {
            phoneNumber = ccp.fullNumberWithPlus

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                90,
                TimeUnit.SECONDS,
                this,
                callbacks
            )
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        const val TAG = "AddPhoneNumberActivity"
    }
}