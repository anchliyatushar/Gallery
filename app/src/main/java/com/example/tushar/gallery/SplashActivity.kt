package com.example.tushar.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    internal var SPLASH_TIME_OUT = 800

    @RequiresApi(Build.VERSION_CODES.M)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed(
            {
                if (!checkSelfPermission()) {

                    requestPermission()

                } else {

                    loadAllImages()
                }
            }, SPLASH_TIME_OUT.toLong()
        )
    }

    private fun checkSelfPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        } else
            return true
    }

    private fun loadAllImages() {

        var imageList = getAllShownImagesPath(this)
        var intent = Intent(this,MainActivity::class.java)
        intent.putParcelableArrayListExtra("image_data",imageList)
        startActivity(intent)
        finish()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 6036)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            6036 -> {
                if (grantResults.size > 0) {
                    var permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (permissionGranted) {

                        loadAllImages()

                    } else {
                        Toast.makeText(this, "Permission Denied! Cannot load images.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

private fun getAllShownImagesPath(activity: Activity): ArrayList<Albums> {

    val uri: Uri
    val cursor: Cursor
    var cursorBucket: Cursor
    val column_index_data: Int
    val column_index_folder_name: Int
    val listOfAllImages = ArrayList<String>()
    var absolutePathOfImage: String? = null
    var albumsList = ArrayList<Albums>()
    var album: Albums? = null


    val BUCKET_GROUP_BY = "1) GROUP BY 1,(2"
    val BUCKET_ORDER_BY = "MAX(datetaken) DESC"

    uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Images.ImageColumns.BUCKET_ID,
        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.DATA)

    cursor = activity.contentResolver.query(uri, projection, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY)

    if (cursor != null) {
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        column_index_folder_name = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data)
            Log.d("title_apps", "bucket name:" + cursor.getString(column_index_data))

            val selectionArgs = arrayOf("%" + cursor.getString(column_index_folder_name) + "%")
            val selection = MediaStore.Images.Media.DATA + " like ? "
            val projectionOnlyBucket = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            cursorBucket = activity.contentResolver.query(uri, projectionOnlyBucket, selection, selectionArgs, null)
            Log.d("title_apps", "bucket size:" + cursorBucket.count)

            if (absolutePathOfImage != "" && absolutePathOfImage != null) {
                listOfAllImages.add(absolutePathOfImage)
                albumsList.add(Albums(cursor.getString(column_index_folder_name), absolutePathOfImage, cursorBucket.count, false))
            }
        }
    }
    return albumsList
}
