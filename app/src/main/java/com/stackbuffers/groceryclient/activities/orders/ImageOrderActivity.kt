package com.stackbuffers.groceryclient.activities.orders

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_image_order.*
import kotlinx.coroutines.launch
import java.io.File

class ImageOrderActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    private val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private val storageRef = FirebaseStorage.getInstance().reference.child("ImageOrders")

    private lateinit var userAddress: String
    private lateinit var userNumber: String
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_order)

        sharedPreference = SharedPreference(this@ImageOrderActivity)

        back.setOnClickListener {
            finish()
        }

        usersRef.child(sharedPreference.getUserId()!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.child("Name").value.toString()
                        userAddress = snapshot.child("Address").value.toString()
                        userNumber = snapshot.child("Mobile_Number").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@ImageOrderActivity)
                }
            })

        try {
            val camera = intent.getStringExtra("camera")
            val gallery = intent.getStringExtra("gallery")

            if (camera != null) {
                GlideApp.with(this@ImageOrderActivity).load(camera).into(orderImage)
                placeOrder.setOnClickListener {
                    progress.visibility = View.VISIBLE
                    placeImageOrder(camera)
                }
            } else if (gallery != null) {
                GlideApp.with(this@ImageOrderActivity).load(gallery).into(orderImage)
                placeOrder.setOnClickListener {
                    progress.visibility = View.VISIBLE
                    compressImage(gallery)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private fun compressImage(image: String) {
        lifecycleScope.launch {
            val actualFile = File(image)
            val compressedImageFile = Compressor.compress(this@ImageOrderActivity, actualFile)
            val compressedImageUri = Uri.fromFile(compressedImageFile)
            placeImageOrder(compressedImageUri.toString())
        }
    }

    private fun placeImageOrder(image: String?) {
        val imageUri = Uri.parse(image)
        val date = System.currentTimeMillis().toString()

        val ref = storageRef.child(date)
        val uploadTask = ref.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                val downloadUrl = it.toString()
                continueImageOrder(downloadUrl, date)
            }
        }.addOnFailureListener {
            progress.visibility = View.GONE
            Utils.toast(this@ImageOrderActivity, "Error Placing Order")
        }.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            Log.d(TAG, "Progress : $progress")
        }
    }

    private fun continueImageOrder(downloadUri: String, date: String) {
        val orderId = ordersRef.push().key.toString()

        val orderMap = HashMap<String, Any>()
        orderMap["orderId"] = orderId
        orderMap["date"] = date
        orderMap["status"] = "Pending"
        orderMap["imageUrl"] = downloadUri

        orderMap["userId"] = sharedPreference.getUserId()!!
        orderMap["userName"] = userName
        orderMap["mobileNumber"] = userNumber
        orderMap["address"] = userAddress

        orderMap["type"] = "image"

        ordersRef.child(orderId).setValue(orderMap).addOnCompleteListener {
            if (it.isSuccessful) {
                Utils.toast(this@ImageOrderActivity, "Order Placed")
                progress.visibility = View.GONE
                finish()
            } else {
                progress.visibility = View.GONE
                Utils.toast(this@ImageOrderActivity, "Error Placing Order")
            }
        }.addOnFailureListener {
            progress.visibility = View.GONE
            Utils.toast(this@ImageOrderActivity, "Error Placing Order")
        }
    }


    companion object {
        const val TAG = "ImageOrderActivity"
    }
}