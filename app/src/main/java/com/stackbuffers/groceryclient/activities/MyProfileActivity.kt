package com.stackbuffers.groceryclient.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.orders.ImageOrderActivity
import com.stackbuffers.groceryclient.activities.orders.ManualOrderActivity
import com.stackbuffers.groceryclient.utils.FileUtil
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_image_order.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_my_profile.back
import kotlinx.coroutines.launch
import java.io.File

class MyProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private val financeRef = FirebaseDatabase.getInstance().getReference("/Finance")
    private val storageRef = FirebaseStorage.getInstance().reference.child("ProfileImages")
    var orderCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        sharedPreference = SharedPreference(this)

        usersRef.child(sharedPreference.getUserId()!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var pts = "0"
                    val imageUrl = snapshot.child("profileImageUrl").value.toString()
                    val username = snapshot.child("Name").value.toString()
                    val email = snapshot.child("Email").value.toString()
                    val city = snapshot.child("City").value.toString()
                    val number = snapshot.child("Mobile_Number").value.toString()
                    val address = snapshot.child("Address").value.toString()

                    if (snapshot.hasChild("orderCount"))
                        orderCount = snapshot.child("orderCount").value.toString().toInt()

                    if (snapshot.hasChild("Points"))
                        pts = snapshot.child("Points").value.toString()

                    GlideApp.with(this@MyProfileActivity).load(imageUrl)
                        .placeholder(R.drawable.profile_image).into(profileImage)
                    userName.text = username
                    userEmail.text = email
                    userNumber.text = number
                    addressText.text = address
                    points.text = pts

                    financeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val silverStart = snapshot.child("SilverStart").value.toString().toInt()
                            val silverEnd = snapshot.child("SilverEnd").value.toString().toInt()
                            val goldStart = snapshot.child("GoldStart").value.toString().toInt()
                            val goldEnd = snapshot.child("GoldEnd").value.toString().toInt()
                            val platStart = snapshot.child("PlatinumStart").value.toString().toInt()
                            val platEnd = snapshot.child("PlatinumEnd").value.toString().toInt()
                            when (orderCount) {
                                in silverStart..silverEnd -> {
                                    GlideApp.with(this@MyProfileActivity)
                                        .load(R.drawable.silver_coin).into(coin)
                                    userCate.text = getString(R.string.silver)
                                }
                                in goldStart..goldEnd -> {
                                    GlideApp.with(this@MyProfileActivity)
                                        .load(R.drawable.gold_coin).into(coin)
                                    userCate.text = getString(R.string.gold)
                                }
                                in platStart..platEnd -> {
                                    GlideApp.with(this@MyProfileActivity)
                                        .load(R.drawable.plat_coin).into(coin)
                                    userCate.text = getString(R.string.plat)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.dbErToast(this@MyProfileActivity)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyProfileActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })

        back.setOnClickListener {
            finish()
        }

        profileImage.setOnClickListener {
            changeProfileImage()
        }

        pointsCard.setOnClickListener {
            startActivity(Intent(this@MyProfileActivity, PointsActivity::class.java))
        }

        couponsCard.setOnClickListener {
            startActivity(Intent(this@MyProfileActivity, CouponsActivity::class.java))
        }

        addressCard.setOnClickListener {
            val intent = Intent(this@MyProfileActivity, EditActivity::class.java)
            intent.putExtra("field", "Address")
            startActivity(intent)
        }
    }

    private fun changeProfileImage() {
        Dexter.withContext(this@MyProfileActivity)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY_REQUEST)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Utils.toast(this@MyProfileActivity, "Storage Permission Denied")
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            })
            .onSameThread()
            .check()

        usersRef.child(sharedPreference.getUserId()!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ManualOrderActivity.GALLERY_REQUEST) {
                FileUtil.from(this@MyProfileActivity, data.data!!).let {
                    Log.d(ManualOrderActivity.TAG, "Image ${it.path}")
                    GlideApp.with(this@MyProfileActivity).load(it.path).into(profileImage)
                    compressImage(it.path)
                }
            }
        }
    }

    private fun compressImage(image: String) {
        lifecycleScope.launch {
            val actualFile = File(image)
            val compressedImageFile = Compressor.compress(this@MyProfileActivity, actualFile)
            val compressedImageUri = Uri.fromFile(compressedImageFile)
            uploadImageToFirebase(compressedImageUri.toString())
        }
    }

    private fun uploadImageToFirebase(image: String) {
        val imageUri = Uri.parse(image)

        val ref = storageRef.child(sharedPreference.getUserId()!!)
        val uploadTask = ref.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                val downloadUrl = it.toString()
                continueImage(downloadUrl)
            }
        }.addOnFailureListener {
            progress.visibility = View.GONE
            Utils.toast(this@MyProfileActivity, "Error Placing Order")
        }.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            Log.d(ImageOrderActivity.TAG, "Progress : $progress")
        }
    }

    private fun continueImage(downloadUrl: String) {
        usersRef.child(sharedPreference.getUserId()!!)
            .child("profileImageUrl")
            .setValue(downloadUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Utils.toast(this@MyProfileActivity, "Profile Image Changed")
                } else {
                    Utils.toast(this@MyProfileActivity, "Error")
                }
            }.addOnFailureListener {
                Utils.toast(this@MyProfileActivity, "Error")
            }
    }

    companion object {
        const val GALLERY_REQUEST = 12
    }
}