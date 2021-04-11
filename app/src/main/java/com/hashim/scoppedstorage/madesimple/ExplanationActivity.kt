/*
 * Copyright (c) 2021/  4/ 11.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.madesimple

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    fun hOpenFileWithFileStream(uri: Uri) {
        // Open a specific media item using InputStream.
        val hResolver = applicationContext.contentResolver
        hResolver.openInputStream(uri).use { stream ->
            // Perform operations on "stream".
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////////

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
///////////////////////////////////////////////////////////////////////////////////////////////////

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
///////////////////////////////////////////////////////////////////////////////////////////////////

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hCreateThumbNail(uri: Uri) {
        val hThumbNail: Bitmap =
            applicationContext.contentResolver.loadThumbnail(
                uri, Size(640, 480), null
            )

    }
///////////////////////////////////////////////////////////////////////////////////////////////////

    /*Say after creating an audio file, to save it afterwards
    * If app performs potentially time-consuming operations, such as writing
    *  to media files, it's useful to have exclusive access to the file as it's
    *  being processed. On devices that run Android 10 or higher, this exclusive access
    * can be  set by the value of the IS_PENDING flag to 1. Only the app
    * in working can view the file until this app changes the value of IS_PENDING back to 0.
    * */
    fun hAddNewItem() {
        val hResolver = applicationContext.contentResolver

        /*Since Volume external primary has read/write */
        val hAudioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        // Publish a new song.
        val hAudioDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "My audio.mp3")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
            /*put other details here*/
        }
        val hInsertedAudio = hResolver
            .insert(hAudioCollection, hAudioDetails)

        hResolver.openFileDescriptor(hInsertedAudio!!, "w", null)
            .use {
                // Write data into the pending audio file.
            }

//        release the "pending" status, and allow other apps to play the audio track.
        hAudioDetails.clear()
        hAudioDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
        hResolver.update(hInsertedAudio, hAudioDetails, null, null)

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    fun hUpdateItem(orignalSongsUri: Uri) {
        val hMediaId = 0     // MediaStore.Audio.Media._ID of item to update.
        val hResolver = applicationContext.contentResolver

        // When performing a single item update, prefer using the ID
        val hSelection = "${MediaStore.Audio.Media._ID} = ?"

        val hSelectionArguments = arrayOf(hMediaId.toString())

        // Update an existing song.
        val hSongDetialsToUpdate = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "test Song.mp3")
            put(MediaStore.Audio.Media.ALBUM, "abc")
        }

        // Use the individual song's URI to represent the collection that's updated.
        /*Use resolvers delete to delete the file*/
        val hUdatedSong = hResolver.update(
            orignalSongsUri!!,
            hSongDetialsToUpdate,
            hSelection,
            hSelectionArguments
        )
    }
///////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    * Update other apps' media files
    * If  app uses scoped storage, it ordinarily cannot update a media file
    * that a different app contributed to the media store. It's still possible to
    * get user consent to modify the file, however, by catching the
    * RecoverableSecurityException that the platform throws. One can then request that
    * the user grant your app write access to that specific item.
    *
    * This process should be completed each time your app needs to modify a
    * media file that it didn't create.
    * */

    fun hUpdateOtherMediaNotBelongingToMyApp(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                /*What you want to change or modify*/
            }
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val hRecoverableSecurityException = securityException as?
                        RecoverableSecurityException
                    ?: throw RuntimeException(securityException.message, securityException)

                val hIntentSender =
                    hRecoverableSecurityException.userAction.actionIntent.intentSender
                hIntentSender?.let {
                    startIntentSenderForResult(
                        hIntentSender, 12,
                        null, 0, 0, 0, null
                    )
                }
            } else {
                throw RuntimeException(securityException.message, securityException)
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /*values of placeholder variables*/
    fun hGetSelectionArgs() {
        /*Add conditions, whatever is required. below is just an example*/
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
        )
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    fun hGetSortOrder() {
        /*Add conditions, whatever is required. below is just an example*/
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////////

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
///////////////////////////////////////////////////////////////////////////////////////////////////

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
///////////////////////////////////////////////////////////////////////////////////////////////////
/*
* Manage groups of media files
* On Android 11 and higher, ask the user to select a group of media files,
* then update these media files in a single operation. These methods offer better
* consistency across devices, and the methods make it easier for users to manage
* their media collections.
*
* The methods that provide this "batch update" functionality include the following:
*
* createWriteRequest()
*       Request that the user grant your app write access to the specified group of media files.
*
*  createFavoriteRequest()
*       Request that the user marks the specified media files as some of their
*        "favorite" media on the device. Any app that has read access to this
*         file can see that the user has marked the file as a "favorite".
*
* createTrashRequest()
*       Request that the user place the specified media files in the device's trash.
*        Items in the trash are permanently deleted after a system-defined time period.
*
* createDeleteRequest()
*       Request that the user permanently delete the specified media files immediately,
*        without placing them in the trash beforehand.
*
*
* After calling any of these methods, the system builds a PendingIntent object.
*  After app invokes this intent, users see a dialog that requests their consent
* for the app to update or delete the specified media files.
* */


    @RequiresApi(Build.VERSION_CODES.R)
    fun hCreateWriteRequest(uri: List<Uri>) {
        /* A collection of content URIs to modify. */
        val editPendingIntent = MediaStore.createWriteRequest(
            contentResolver,
            uri
        )

        // Launch a system prompt requesting user permission for the operation.
        // on Activity result is called after the user inputs.
        startIntentSenderForResult(
            editPendingIntent.intentSender, 12,
            null, 0, 0, 0
        )
    }
//////////////////////////////////////////////////////////////////////////////////////////////////

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