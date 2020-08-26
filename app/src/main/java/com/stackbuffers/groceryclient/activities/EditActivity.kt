package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.item_user.*

class EditActivity : AppCompatActivity() {

    private var field: String? = ""
    private lateinit var sharedPreference: SharedPreference
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        sharedPreference = SharedPreference(this@EditActivity)
        back.setOnClickListener {
            finish()
        }

        try {
            field = intent.getStringExtra("field")
            toolbarText.text = "Edit $field"
            fieldEt.hint = "Input $field"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        done.setOnClickListener {
            when {
                fieldEt.text.toString().trim().isEmpty() -> {
                    finish()
                }
                field == "Address" -> {
                    usersRef.child(sharedPreference.getUserId()!!).child("Address")
                        .setValue(fieldEt.text.toString()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Utils.toast(this@EditActivity, "Address updated")
                                finish()
                            } else {
                                Utils.toast(this@EditActivity, "Failed to update Address")
                            }
                        }.addOnFailureListener {
                            Utils.toast(this@EditActivity, "Failed to update Address")
                        }
                }
                field == "Name" -> {
                    usersRef.child(sharedPreference.getUserId()!!).child("Name")
                        .setValue(fieldEt.text.toString()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Utils.toast(this@EditActivity, "Address updated")
                                finish()
                            } else {
                                Utils.toast(this@EditActivity, "Failed to update Name")
                            }
                        }.addOnFailureListener {
                            Utils.toast(this@EditActivity, "Failed to update Name")
                        }
                }
                else -> finish()
            }
        }
    }
}