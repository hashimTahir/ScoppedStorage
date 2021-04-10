/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.imagepicker

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.IntentSender
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class ImagePickerViewModel(application: Application) : AndroidViewModel(application) {

    private val hImagesListMLD = MutableLiveData<List<MediaStoreModel>>()
    val hImagesListMD: LiveData<List<MediaStoreModel>> get() = hImagesListMLD

    private var hContentObserver: ContentObserver? = null

    private var hDeletePendingItem: MediaStoreModel? = null

    private val hPermissionNeededToDeleteMLD = MutableLiveData<IntentSender?>()
    val hGetPermissionToDeleteIfNeeded: LiveData<IntentSender?> = hPermissionNeededToDeleteMLD

    fun hLoadImages() {
        viewModelScope.launch {
            val hImagesList = hQueryImages()
            hImagesListMLD.postValue(hImagesList)

            if (hContentObserver == null) {
                hContentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {
                    hLoadImages()
                }
            }
        }
    }

    fun hDeleteImages(image: MediaStoreModel) {
        viewModelScope.launch {
            hPerformImageDeletion(image)
        }
    }

    fun hDeletePendingImage() {
        hDeletePendingItem?.let { image ->
            hDeletePendingItem = null
            hDeleteImages(image)
        }
    }

    private suspend fun hQueryImages(): List<MediaStoreModel> {
        val hImagesList = mutableListOf<MediaStoreModel>()

        withContext(Dispatchers.IO) {

            val hProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
            )

            val hSelection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"

            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            var hQuery = getApplication<Application>().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                sortOrder
            )
            hQuery?.use { cursor ->

                val hColumnId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val hDateColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val hDisplayColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {

                    val hId = cursor.getLong(hColumnId)
                    val hDateTaken = Date(cursor.getLong(hDateColumn))
                    val hDisplayName = cursor.getString(hDisplayColumn)

                    val hContentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        hId
                    )

                    val hImage = MediaStoreModel(hId, hDisplayName, hDateTaken, hContentUri)
                    hImagesList += hImage


                    Timber.d(hImage.toString())
                }
            }
        }

        Timber.d("Found ${hImagesList.size} images")
        return hImagesList
    }

    private suspend fun hPerformImageDeletion(image: MediaStoreModel) {
        withContext(Dispatchers.IO) {
            try {

                getApplication<Application>().contentResolver.delete(
                    image.contentUri,
                    "${MediaStore.Images.Media._ID} = ?",
                    arrayOf(image.id.toString())
                )
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException


                    hDeletePendingItem = image
                    hPermissionNeededToDeleteMLD.postValue(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    )
                } else {
                    throw securityException
                }
            }
        }
    }

    override fun onCleared() {
        hContentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }
}


private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val hContentObserver = object : ContentObserver(Handler(Looper.myLooper()!!)) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, hContentObserver)
    return hContentObserver
}
