package com.believeapps.touch

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.customview.widget.ViewDragHelper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_touch.*
import java.io.File

const val TAKE_PHOTO_REQUEST = 1

class TouchActivity : AppCompatActivity() {

    private lateinit var dragHelper: ViewDragHelper
    private var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch)
        dragHelper = ViewDragHelper.create(rootView, 1.0f, createCallback())
        activityTouchBtn.setOnClickListener { requestRequiredPermissions { launchCamera() } }
    }


    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        with(intent) {
            if (resolveActivity(packageManager) != null) {
                currentPhotoPath = fileUri.toString()
                putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                addFlags(FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(intent, TAKE_PHOTO_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO_REQUEST) {
            processCapturedPhoto()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processCapturedPhoto() {
        val cursor = contentResolver.query(Uri.parse(currentPhotoPath),
                Array(1) { android.provider.MediaStore.Images.ImageColumns.DATA },
                null, null, null)
        cursor.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)
        val uri = Uri.fromFile(file)
        activityTouchCustomView.setImage(BitmapFactory.decodeFile(uri.path))
    }

    private fun requestRequiredPermissions(onSuccessfulPermission: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, CAMERA) +
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                == PERMISSION_GRANTED) {
            onSuccessfulPermission()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "androix is so bad", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(
                        this, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE), TAKE_PHOTO_REQUEST)
            } else {
                ActivityCompat.requestPermissions(
                        this, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE), TAKE_PHOTO_REQUEST)
            }
        }
    }

    private fun createCallback(): ViewDragHelper.Callback {
        return object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return true
            }
        }
    }
}
