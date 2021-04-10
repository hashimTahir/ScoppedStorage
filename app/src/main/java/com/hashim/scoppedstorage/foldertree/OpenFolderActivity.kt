/*
 * Copyright (c) 2021/  4/ 11.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.foldertree

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hashim.scoppedstorage.Constants.OPEN_DIRECTORY_REQUEST_CODE
import com.hashim.scoppedstorage.databinding.ActivityOpenFolderBinding
import timber.log.Timber

class OpenFolderActivity : AppCompatActivity() {
    lateinit var hActivityOpenFolderBinding: ActivityOpenFolderBinding
    var hFileList = ArrayList<DocumentFile>()
    val adpater = FileViewAdpater(this, hFileList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityOpenFolderBinding = ActivityOpenFolderBinding.inflate(layoutInflater)
        setContentView(hActivityOpenFolderBinding.root)


        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        hActivityOpenFolderBinding.hRv.layoutManager = layoutManager



        hActivityOpenFolderBinding.hOpenDocTree.setOnClickListener {
            hOpenDocTree()
        }
    }

    fun hOpenDocTree() {
        val hIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(hIntent, OPEN_DIRECTORY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            hFileList.clear()
            val hDirUri = data?.data ?: return

            contentResolver.takePersistableUriPermission(
                hDirUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            hLoadDirectory(hDirUri)


        }
    }

    fun hLoadDirectory(directoryUri: Uri) {
        val hDocumentsTre = DocumentFile.fromTreeUri(application, directoryUri) ?: return
        val hChildDocuments = hDocumentsTre.listFiles().asList()


        for (i in 0 until hChildDocuments.size) {
            Timber.d("${hChildDocuments[i].type}")
            hFileList.add(hChildDocuments[i])

        }
        hActivityOpenFolderBinding.hRv.adapter = adpater

    }

}