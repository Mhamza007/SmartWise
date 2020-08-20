package com.stackbuffers.groceryclient.activities.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.signin.SignInActivity
import com.stackbuffers.groceryclient.utils.SharedPreference
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var city: String

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        sharedPreference = SharedPreference(this)

        back.setOnClickListener {
            finish()
        }

        signIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        signUpBtn.setOnClickListener {
            name = userName.text.toString().trim()
            email = emailAddress.text.toString().trim()
            city = cityName.text.toString().trim()
            val pw = password.text.toString().trim()
            if (name.isEmpty() && email.isEmpty() && city.isEmpty() && pw.isEmpty()) {
                userName.error = "Empty User Name"
                emailAddress.error = "Empty Email Address"
                cityName.error = "Empty City"
                password.error = "Empty Password"
            } else if (userName.text.toString().trim().isEmpty()) {
                userName.error = "Empty User Name"
            } else if (emailAddress.text.toString().trim().isEmpty()) {
                emailAddress.error = "Empty Email Address"
            } else if (cityName.text.toString().trim().isEmpty()) {
                cityName.error = "Empty City"
            } else if (password.text.toString().trim().isEmpty()) {
                password.error = "Empty Password"
            } else {
                signupWithEmailAndPassword(email, pw)
            }
        }
    }

    private fun signupWithEmailAndPassword(email: String, pw: String) {

        auth.createUserWithEmailAndPassword(email, pw)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    currentUserId = auth.currentUser!!.uid

                    val db = FirebaseDatabase.getInstance().getReference("/users")

                    val map = HashMap<String, Any>()
                    map["Ban"] = ""
                    map["City"] = city
                    map["Email"] = email
                    map["Mobile_Number"] = ""
                    map["Name"] = name
                    map["Reason"] = ""
                    map["User_ID"] = currentUserId
                    map["profileImageUrl"] = ""

                    db.child(currentUserId).setValue(map).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            sharedPreference.setUserId(currentUserId)
                            startActivity(Intent(this, AddPhoneNumberActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Sign up Failed", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Sign up Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Sign up Failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Sign up Failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Exception: ${it.message}")
            }.addOnCanceledListener {
                Toast.makeText(this, "Sign up Cancelled", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        const val TAG = "SignUpActivity"
    }
}