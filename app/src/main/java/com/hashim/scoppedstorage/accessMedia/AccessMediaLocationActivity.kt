/*
 * Copyright (c) 2021/  4/ 11.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.accessMedia

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.hashim.scoppedstorage.CheckPemission.hCheckAMLpermissions
import com.hashim.scoppedstorage.CheckPemission.hRequestAMLPermissions
import com.hashim.scoppedstorage.Constants.CHOOSE_FILE
import com.hashim.scoppedstorage.R
import com.hashim.scoppedstorage.databinding.ActivityAccessMediaLocationBinding
import timber.log.Timber
import java.io.IOException
import java.io.InputStream


class AccessMediaLocationActivity : AppCompatActivity() {

    lateinit var hActivityAccessMediaLocationBinding: ActivityAccessMediaLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityAccessMediaLocationBinding = ActivityAccessMediaLocationBinding
            .inflate(layoutInflater)
        setContentView(hActivityAccessMediaLocationBinding.root)

        hActivityAccessMediaLocationBinding.hChooseMediaFileB.setOnClickListener {
            hChooseFile()
        }
    }

    fun hChooseFile() {
        Timber.d("chooseFile")
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            hOpenIntentChooser()
        } else {
            if (hCheckAMLpermissions(this)) {

                hOpenIntentChooser()
            } else {
                Timber.d("else chooseFile")

                hRequestAMLPermissions(this)
            }
        }
    }

    fun hOpenIntentChooser() {
        val hIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        hIntent.type = "image/*"
        hIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        hIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(hIntent, "Select Picture"), CHOOSE_FILE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_FILE) {
                if (data != null) {

                    var hInputStream: InputStream? = null
                    try {
                        hInputStream = contentResolver.openInputStream(data.data!!)
                        val exifInterface = ExifInterface(hInputStream!!)

                        Timber.d("exif ${exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)}")
                        Glide.with(hActivityAccessMediaLocationBinding.hImageV)
                            .load(data.dataString)
                            .thumbnail(0.33f)
                            .into(hActivityAccessMediaLocationBinding.hImageV)

                        hActivityAccessMediaLocationBinding.hImagePathTv.text =
                            String.format(getString(R.string.path_for_image), data.data)

                        hActivityAccessMediaLocationBinding.hImageLocationTv.text = String.format(
                            getString(R.string.lat_for_image),
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                        )
                        hActivityAccessMediaLocationBinding.hImageLocationTagTv.text =
                            String.format(
                                getString(R.string.lat_for_image),
                                exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                            )
                    } catch (e: IOException) {
                        Timber.d("Exception ${e.message}")
                    } finally {
                        if (hInputStream != null) {
                            try {
                                hInputStream.close()
                            } catch (ignored: IOException) {
                                Timber.d("Exception ${ignored.message}")
                            }
                        }
                    }
                }
            }
        }

    }
}