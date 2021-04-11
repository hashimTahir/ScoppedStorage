/*
 * Copyright (c) 2021/  4/ 11.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.madesimple

import android.content.ContentUris
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.hashim.scoppedstorage.databinding.ActivityExplanationBinding
import timber.log.Timber
import java.util.concurrent.TimeUnit


/*Simplifies the process how to qurey data
 * 1=>Create query by providing
 *   a->Colletion i.e. where to look for files,
 *   b->Projection i.e. what to look for,
 *   c->Selection-> where clause
 *   d->Selection arguments
 *   e->Sort order
 *
 * 2->Map the retrieved data to a model.
 *
 * 3-> Perform the operation required. i.e. modify, save, delete.
 *
 * */
class ExplanationActivity : AppCompatActivity() {


    lateinit var hActivityExplanationBinding: ActivityExplanationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityExplanationBinding = ActivityExplanationBinding.inflate(layoutInflater)
        setContentView(hActivityExplanationBinding.root)
        hQueryAllMedia()
    }

    private fun hQueryAllMedia() {

        /*Not passing anything for seletion, selection arguments and sort order*/
        try {
            val a = hQueryAudios()
            val v = hQueryVideos()
            val d = hQueryDocs()
            val i = hQueryImages()

            Timber.d("Audio $a , Video $v , Docs  $d,  Images $i")

        } catch (e: Exception) {
            Timber.d("Exception ${e.message}")

        }

    }


    /*Where to look*/
    fun hGetCollectionUri(
        mediaTypes: MediaTypes
    ): Uri {
        return when (mediaTypes) {
            MediaTypes.H_AUDIO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            MediaTypes.H_FILES -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Files.getContentUri("external")
                }
            }
            MediaTypes.H_VIDEO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            }
            MediaTypes.H_IMAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }
        }

    }

    fun hOpenFileWithDescriptor(uri: Uri) {
        // Open a specific media item using ParcelFileDescriptor.
        val hContentResolver = applicationContext.contentResolver

        // "rw" for read-and-write;
        // "rwt" for truncating or overwriting existing file contents.
        val readOnlyMode = "r"
        hContentResolver.openFileDescriptor(uri, readOnlyMode).use { file ->
            // Perform operations on file
        }
    }

    fun hOpenFileWithFileStream(uri: Uri) {
        // Open a specific media item using InputStream.
        val hResolver = applicationContext.contentResolver
        hResolver.openInputStream(uri).use { stream ->
            // Perform operations on "stream".
        }

    }




    /*
    * Apps that target Android 10 or higher can access the unique name that the system assigns
    * to each external storage volume. This naming system helps to efficiently organize and
    * index content, and it gives control over where new media files are stored.
    *
    * The following volumes are particularly useful to keep in mind:
    *
    *   1-> The VOLUME_EXTERNAL volume provides a view of all shared storage volumes on the device.
    *       one can read the contents of this synthetic volume, but cannot modify the contents.
    *   2-> The VOLUME_EXTERNAL_PRIMARY volume represents the primary shared storage volume on
    *       the device. One can read and modify the contents of this volume.
    *
    * One can discover other volumes by calling MediaStore.getExternalVolumeNames():
    *
    * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun hGetVolumes() {
        val hVolumesNames: Set<String> = MediaStore.getExternalVolumeNames(this)
        val hFirstVolumeName = hVolumesNames.iterator().next()

    }


    /*
    * Location where media was captured
    * Some photographs and videos contain location information in their metadata,
    * which shows the place where a photograph was taken or where a video was recorded.
    * For Photographs If  app uses scoped storage, the system hides location information by default.
    * To access this information, following steps are required.
    *
    * Request the ACCESS_MEDIA_LOCATION permission in app's manifest.
    *
    * From  MediaStore object, get the exact bytes of the photograph by
    * calling setRequireOriginal() and pass in the URI of the photograph.
    *
    * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun hGetPhotoLocationInfo(hIdColumn: Int, cursor: Cursor) {
        var hPhotoUri: Uri = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            cursor.getString(hIdColumn)
        )

        // Get location data using the Exifinterface library.
        // Exception occurs if ACCESS_MEDIA_LOCATION permission isn't granted.
        hPhotoUri = MediaStore.setRequireOriginal(hPhotoUri)
        contentResolver.openInputStream(hPhotoUri)?.use { stream ->
            ExifInterface(stream).run {
                // If lat/long is null, fall back to the coordinates (0, 0).
                val latLong = latLong ?: doubleArrayOf(0.0, 0.0)
            }
        }

    }

    /*
    * To access location information within a video's metadata, use the
    * MediaMetadataRetriever class, app doesn't need to request any additional
    *  permissions to use this class.
    * */

    private fun hExtractVideoLocationInfo(hIdColumn: Long) {

        val hVideoUri: Uri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            hIdColumn
        )

        val hMediaMetaDataRetriever = MediaMetadataRetriever()
        val hContext = applicationContext
        try {
            hMediaMetaDataRetriever.setDataSource(hContext, hVideoUri)
        } catch (e: RuntimeException) {
            Timber.d("Cannot retrieve video file $e")
        }
        // Metadata should use a standardized format.
        val hLocationMetaData: String? =
            hMediaMetaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hCreateThumbNail(uri: Uri) {
        val hThumbNail: Bitmap =
            applicationContext.contentResolver.loadThumbnail(
                uri, Size(640, 480), null
            )

    }

    /*What to retireve*/
    fun hGetProjection(mediaTypes: MediaTypes): Array<String> {
        return when (mediaTypes) {
            MediaTypes.H_VIDEO -> {
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE
                )
            }
            MediaTypes.H_FILES -> {
                arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
                )
            }
            MediaTypes.H_IMAGE -> {
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                )
            }
            MediaTypes.H_AUDIO -> {
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                )
            }
        }
    }

    /*where clause with place holders*/
    fun hGetSelection(mediaTypes: MediaTypes): String? {
        return when (mediaTypes) {
            MediaTypes.H_VIDEO -> {
                "${MediaStore.Video.Media.DURATION} >= ?"
            }
            MediaTypes.H_FILES -> {
                null
            }
            MediaTypes.H_IMAGE -> {
                null
            }
            MediaTypes.H_AUDIO -> {
                null
            }
        }


    }

    /*values of placeholder variables*/
    fun hGetSelectionArgs() {
        /*Add conditions, whatever is required. below is just an example*/
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
        )
    }

    fun hGetSortOrder() {
        /*Add conditions, whatever is required. below is just an example*/
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
    }

    fun hCreateQuery(
        collection: Uri,
        projection: Array<String>,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return application.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    fun hQueryImages(): MutableList<Image> {
        val hImageList = mutableListOf<Image>()
        hCreateQuery(
            collection = hGetCollectionUri(MediaTypes.H_IMAGE),
            hGetProjection(MediaTypes.H_IMAGE),
            null,
            null,
            null
        ).use { cursor ->
            val hIdCol = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val hNameCol = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val hSizeCol = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor?.moveToNext() == true) {
                val hId = cursor.getLong(hIdCol!!)
                val hName = cursor.getString(hNameCol!!)
                val hSize = cursor.getInt(hSizeCol!!)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    hId
                )
                hImageList.add(Image(contentUri, hName, hSize))

            }
        }
        return hImageList
    }

    fun hQueryVideos(): MutableList<Video> {
        val hVideoList = mutableListOf<Video>()
        hCreateQuery(
            collection = hGetCollectionUri(MediaTypes.H_VIDEO),
            hGetProjection(MediaTypes.H_VIDEO),
            null,
            null,
            null
        ).use { cursor ->
            val hIdCol = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val hNameCol = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val hDurationCol = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val hSizeCol = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor?.moveToNext() == true) {
                val hId = cursor.getLong(hIdCol!!)
                val hName = cursor.getString(hNameCol!!)
                val hDuration = cursor.getInt(hDurationCol!!)
                val hSize = cursor.getInt(hSizeCol!!)

                val hContentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    hId
                )
                hVideoList.add(Video(hContentUri, hName, hDuration, hSize))
            }
        }
        return hVideoList
    }

    fun hQueryAudios(): MutableList<Audio> {
        val hAudioList = mutableListOf<Audio>()
        hCreateQuery(
            collection = hGetCollectionUri(MediaTypes.H_AUDIO),
            hGetProjection(MediaTypes.H_AUDIO),
            null,
            null,
            null
        ).use { cursor ->
            val hIdCol = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val hNameCol = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val hDurationCol = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val hSizeCol = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor?.moveToNext() == true) {
                val hId = cursor.getLong(hIdCol!!)
                val hName = cursor.getString(hNameCol!!)
                val hDuration = cursor.getInt(hDurationCol!!)
                val hSize = cursor.getInt(hSizeCol!!)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    hId
                )
                hAudioList.add(Audio(contentUri, hName, hDuration, hSize))

            }
        }
        return hAudioList
    }

    fun hQueryDocs(): MutableList<Docs> {
        val hDosList = mutableListOf<Docs>()
        hCreateQuery(
            collection = hGetCollectionUri(MediaTypes.H_FILES),
            hGetProjection(MediaTypes.H_FILES),
            null,
            null,
            null
        ).use { cursor ->

            val hIdCol = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val hMimeTypeCol = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val hDisplayNameCol =
                cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val hSizeCol = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (cursor?.moveToNext() == true) {
                val hId = cursor.getLong(hIdCol!!)
                val hMime = cursor.getString(hMimeTypeCol!!)
                val hDisplayName = cursor.getInt(hDisplayNameCol!!)
                val hSize = cursor.getInt(hSizeCol!!)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    hId
                )
                hDosList.add(
                    Docs(
                        uri = contentUri,
                        mime = hMime,
                        name = hDisplayName,
                        size = hSize
                    )
                )

            }
        }
        return hDosList
    }


    data class Video(
        val uri: Uri,
        val name: String,
        val duration: Int,
        val size: Int
    )

    data class Image(
        val uri: Uri,
        val name: String,
        val size: Int
    )

    data class Docs(
        val uri: Uri,
        val name: Int,
        val size: Int,
        val mime: String
    )

    data class Audio(
        val uri: Uri,
        val name: String,
        val duration: Int,
        val size: Int
    )

}