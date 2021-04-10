/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.imagepicker

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hashim.scoppedstorage.CheckPemission.hCheckForReadWritePermissions
import com.hashim.scoppedstorage.CheckPemission.hRequestPermissionForReadWrite
import com.hashim.scoppedstorage.Constants.DELETE_PERMISSION_REQUEST
import com.hashim.scoppedstorage.R
import com.hashim.scoppedstorage.databinding.ActivityImagePickerBinding


/*Demonstrates How to access public storage and modify it*/

class ImagePickerActivity : AppCompatActivity() {


    private val hImagePickerViewModel: ImagePickerViewModel by viewModels()
    private lateinit var hActivityImagePickerBinding: ActivityImagePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityImagePickerBinding = ActivityImagePickerBinding.inflate(layoutInflater)
        setContentView(hActivityImagePickerBinding.root)

        val hImageAdapter = ImageViewAdapter { image ->
            hDeleteImage(image)
        }

        hActivityImagePickerBinding.hImageRv.also { view ->
            view.layoutManager = GridLayoutManager(this, 3)
            view.adapter = hImageAdapter
        }

        hImagePickerViewModel.hImagesListMD.observe(this, { images ->
            hImageAdapter.submitList(images)
        })

        hImagePickerViewModel.hGetPermissionToDeleteIfNeeded.observe(this, { intentSender ->
            intentSender?.let {

                startIntentSenderForResult(
                    intentSender,
                    DELETE_PERMISSION_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
        })

        hActivityImagePickerBinding.hOpenAlbumIv.setOnClickListener { hOpenMediaStore() }
        hActivityImagePickerBinding.hGrantPermissionsButton.setOnClickListener { hOpenMediaStore() }

        if (!hCheckForReadWritePermissions(this)) {
            hActivityImagePickerBinding.hStartUpView.visibility = View.VISIBLE
        } else {
            hLoadImages()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            hImagePickerViewModel.hDeletePendingImage()
        }
    }

    private fun hLoadImages() {
        hImagePickerViewModel.hLoadImages()
        hActivityImagePickerBinding.hStartUpView.visibility = View.GONE
        hActivityImagePickerBinding.hPermissionRationaleView.visibility = View.GONE
    }

    private fun hOpenMediaStore() {
        if (hCheckForReadWritePermissions(this)) {
            hLoadImages()
        } else {
            hRequestPermissionForReadWrite(this)
        }
    }


    private fun hDeleteImage(image: MediaStoreModel) {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(getString(R.string.delete_dialog_message, image.displayName))
            .setPositiveButton(R.string.delete_dialog_positive) { _: DialogInterface, _: Int ->
                hImagePickerViewModel.hDeleteImages(image)
            }
            .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }


}
