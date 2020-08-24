package com.stackbuffers.groceryclient.activities.orders

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.FileUtil
import com.stackbuffers.groceryclient.utils.Utils
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_manual_order.*
import java.io.ByteArrayOutputStream
import java.io.File


class ManualOrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_order)

        camera.setOnClickListener {
            Dexter.withContext(this@ManualOrderActivity)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                        if (p0.areAllPermissionsGranted()) {
                            val cameraIntent =
                                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(cameraIntent, CAMERA_REQUEST)
                        } else {
                            Utils.toast(this@ManualOrderActivity, "Permissions Denied")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                })
                .onSameThread()
                .check()
        }

        gallery.setOnClickListener {
            Dexter.withContext(this@ManualOrderActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                        if (p0.areAllPermissionsGranted()) {

                            val galleryIntent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(galleryIntent, GALLERY_REQUEST)
                        } else {
                            Utils.toast(this@ManualOrderActivity, "Permissions Denied")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                })
                .onSameThread()
                .check()
        }

        manually.setOnClickListener {
            startActivity(Intent(this@ManualOrderActivity, OrderManuallyActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            // Camera
            if (requestCode == CAMERA_REQUEST) {
                val bmp = data.extras?.get("data") as Bitmap
                val imageUri = getImageUri(this@ManualOrderActivity, bmp)
//                val actualImage = FileUtil.from(this@ManualOrderActivity, data.data)?.let {
                    val intent = Intent(this@ManualOrderActivity, ImageOrderActivity::class.java)
                    intent.putExtra("camera", imageUri.toString())
                    startActivity(intent)
//                }
            }
            // Gallery
            if (requestCode == GALLERY_REQUEST) {
                val actualImage = FileUtil.from(this@ManualOrderActivity, data.data!!).let {
                    Log.d(TAG, "Image ${it.path}")
                    val intent = Intent(this@ManualOrderActivity, ImageOrderActivity::class.java)
                    intent.putExtra("gallery", it.path)
                    startActivity(intent)
                }
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    companion object {
        const val TAG = "ManualOrderActivity"

        const val CAMERA_REQUEST = 11
        const val GALLERY_REQUEST = 12
    }
}