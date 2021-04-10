/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.downloadImage

import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hashim.scoppedstorage.CheckPemission.hCheckForReadWritePermissions
import com.hashim.scoppedstorage.CheckPemission.hRequestPermissionForReadWrite
import com.hashim.scoppedstorage.R
import com.hashim.scoppedstorage.databinding.ActivityDownLoadImageBinding
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class DownLoadImageActivity : AppCompatActivity() {

    var hImageUrl =
        "https://cdn.pixabay.com/photo/2021/03/27/09/44/bird-6127928_1280.jpg"
    lateinit var hActivityDownLoadImageBinding: ActivityDownLoadImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityDownLoadImageBinding = ActivityDownLoadImageBinding.inflate(layoutInflater)
        setContentView(hActivityDownLoadImageBinding.root)

        hSetupListeners()

        Glide.with(this)
            .load(hImageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .dontAnimate()
            .into(hActivityDownLoadImageBinding.hDisplayIV)

    }

    private fun hSetupListeners() {
        hActivityDownLoadImageBinding.hDownloadImageToPicturesFolderB.setOnClickListener {
            hDownloadToPublicStoragePicturesFolder()
        }
        hActivityDownLoadImageBinding.hDownloadToInternalDirectoryB.setOnClickListener {
            hSaveFileToInternalStorage()
        }

        hActivityDownLoadImageBinding.hDownloadWithMediaStoreB.setOnClickListener {
            hDownloadToMediaStore()
        }

        hActivityDownLoadImageBinding.hDownloadToExternalStorage.setOnClickListener {
            hDownloadImageWithMediaStoreToSdCard()
        }

        hActivityDownLoadImageBinding.hPickImageAsBitmapB.setOnClickListener {
            hSelectNewFile()
        }
    }

    fun hSelectNewFile() {
        val hIntent = Intent()
        hIntent.type = "image/*"
        hIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(hIntent, "Select Picture"), 1223)
    }


    fun hDownloadToPublicStoragePicturesFolder() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            if (hCheckForReadWritePermissions(this)) {
                hDownloadFile(hImageUrl)
            } else {
                hRequestPermissionForReadWrite(this)
            }
        } else {
            hDownloadFile(hImageUrl)
        }

    }


    private fun hDownloadFile(hUrl: String) {

        val hDownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val hDownloadUri = Uri.parse(hUrl)
        val hDownloadRequest = DownloadManager.Request(
            hDownloadUri
        )

        hDownloadRequest.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
        )
            .setAllowedOverRoaming(false).setTitle("Test")
            .setDescription("testing scopped storage")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "testimage.jpg")

        Toast.makeText(
            this,
            "Download successfully to ${hDownloadUri.path}",
            Toast.LENGTH_LONG
        ).show()

        hDownloadManager.enqueue(hDownloadRequest)

    }


    fun hSaveFileToInternalStorage() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (hCheckForReadWritePermissions(this)) {
                hDownloadToInternalStoreage()
            } else {
                hRequestPermissionForReadWrite(this)
            }

        } else {
            hDownloadToInternalStoreage()

        }

    }

    private fun hDownloadToInternalStoreage() {
        try {
            val hBitmap = (hActivityDownLoadImageBinding.hDisplayIV.drawable as BitmapDrawable)
                .bitmap

            val hFilePath = saveToInternalStorage(hBitmap)

            Toast.makeText(
                applicationContext,
                "Download successfully to $hFilePath",
                Toast.LENGTH_LONG
            ).show()


        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                "$e=== Access (Permission denied)",
                Toast.LENGTH_LONG
            ).show()
            Timber.d(e.toString())
            e.printStackTrace()

        }
    }


    private fun saveToInternalStorage(bitmapImage: Bitmap?): String {

        var filepath = ""

        try {

            val testFile = File(
                this.getExternalFilesDir(
                    null
                ), "TestFile.png"
            )
            filepath = testFile.absolutePath
            if (!testFile.exists())
                testFile.createNewFile()

            var fos: FileOutputStream? = null

            fos = FileOutputStream(testFile)

            bitmapImage?.compress(Bitmap.CompressFormat.PNG, 100, fos)

        } catch (e: IOException) {
            filepath = "not done"
            Timber.d("Failed")
        }

        return filepath

    }

    fun hDownloadToMediaStore() {
        try {
            val hBitmap = (hActivityDownLoadImageBinding.hDisplayIV.drawable as BitmapDrawable)
                .bitmap

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val hFile = File(getExternalFilesDir(null), "testimage.jpg")

                val hFileOutputStream: FileOutputStream?

                hFileOutputStream = FileOutputStream(hFile)

                hBitmap?.compress(Bitmap.CompressFormat.PNG, 100, hFileOutputStream)

                Toast.makeText(
                    applicationContext,
                    "Download successfully to  ${hFile.path}",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                val hContentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "imgMS.jpeg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val hResolver = contentResolver

                val hCollection = MediaStore.Images.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                val hItem = hResolver.insert(hCollection, hContentValues)

                if (hItem != null) {
                    hResolver.openOutputStream(hItem).use { out ->
                        hBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    hContentValues.clear()
                    hContentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    hResolver.update(hItem, hContentValues, null, null)

                    Toast.makeText(
                        applicationContext,
                        "Download successfully to ${hItem.path}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Timber.d("$e")
            Toast.makeText(
                applicationContext,
                "$e",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    fun hDownloadImageWithMediaStoreToSdCard() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                val hBitmap = (hActivityDownLoadImageBinding.hDisplayIV.drawable as BitmapDrawable)
                    .bitmap

                val hPath = Environment.getExternalStorageDirectory().toString()

                val hFileName = File("$hPath/image.jpg")

                val hFileOutputStream = FileOutputStream(hFileName)

                hBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, hFileOutputStream)
                hFileOutputStream.flush()
                hFileOutputStream.close()

                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    hFileName.absolutePath,
                    hFileName.name,
                    hFileName.name
                )

                Toast.makeText(applicationContext, "Saved in  $hFileName", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val hBitmap = (hActivityDownLoadImageBinding.hDisplayIV.drawable as BitmapDrawable)
                    .bitmap
                val hValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "imgMS.jpeg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val hContentResolver = contentResolver

                val hVolumesNames = MediaStore.getExternalVolumeNames(this)

                val hCollecation = MediaStore.Images.Media.getContentUri(hVolumesNames.elementAt(1))

                val hItem = hContentResolver.insert(hCollecation, hValues)


                if (hItem != null) {
                    hContentResolver.openOutputStream(hItem).use { out ->
                        hBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    hValues.clear()
                    hValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    hContentResolver.update(hItem, hValues, null, null)

                    Toast.makeText(
                        applicationContext,
                        "Download successfully to ${hItem.path}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    applicationContext,
                    "$e",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 1223) {

            Timber.d("Data=== ${data?.data}")

            val hUri = data?.data
            val hFile = File(hUri?.path)

            Timber.d("File=== $hFile")

            val hBitmap = BitmapFactory.decodeStream(data?.data?.let {
                contentResolver.openInputStream(
                    it
                )
            })
            hActivityDownLoadImageBinding.hDisplayIV.setImageBitmap(hBitmap)
        }
    }
}
