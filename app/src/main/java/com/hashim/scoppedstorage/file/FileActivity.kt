/*
 * Copyright (c) 2021/  4/ 11.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.file

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.hashim.scoppedstorage.Constants.CREATE_REQUEST_CODE
import com.hashim.scoppedstorage.Constants.OPEN_DOCUMENT_REQUEST_CODE
import com.hashim.scoppedstorage.databinding.ActivityFileBinding
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class FileActivity : AppCompatActivity() {
    lateinit var hActivityFileBinding: ActivityFileBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityFileBinding = ActivityFileBinding.inflate(layoutInflater)
        setContentView(hActivityFileBinding.root)

        hSetupListeners()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hSetupListeners() {
        hActivityFileBinding.hDownloadPdfB.setOnClickListener {
            hDownalodPDf()
        }
        hActivityFileBinding.hOpenPdfB.setOnClickListener {
            hOpenPdfs()
        }
        hActivityFileBinding.hSaveB.setOnClickListener {
            hSaveFile()
        }
    }


    fun hSaveFile() {
        val hIntent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        hIntent.addCategory(Intent.CATEGORY_OPENABLE)
        hIntent.type = "text/plain"
        hIntent.putExtra(Intent.EXTRA_TITLE, "${hActivityFileBinding.hFileNameEt.text.trim()}.txt")
        startActivityForResult(hIntent, CREATE_REQUEST_CODE)


    }


    fun hOpenPdfs() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)


    }


    private fun hWriteContentofFile(uri: Uri?) {
        try {
            val hPdf = uri?.let { this.contentResolver.openFileDescriptor(it, "w") }

            val hFileOutputStream = FileOutputStream(
                hPdf!!.fileDescriptor
            )
            val hText = hActivityFileBinding.hContentEt.text.toString()

            hFileOutputStream.write(hText.toByteArray())

            hFileOutputStream.close()
            hPdf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hDownalodPDf() {
        val hPdfDocument = PdfDocument()
        val hPageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val hPage = hPdfDocument.startPage(hPageInfo)
        val hCanvas = hPage.canvas
        val hPaint = Paint()
        hPaint.color = Color.RED
        hCanvas.drawCircle(50F, 50F, 30F, hPaint)
        hPaint.color = Color.BLACK
        hCanvas.drawText("aaabbbccc", 80F, 50F, hPaint)
        hPdfDocument.finishPage(hPage)

        val hContentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "demopdf.pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val hContentResolver = contentResolver

        val hVolumeNames = MediaStore.getExternalVolumeNames(this)
        val hCollection = MediaStore.Downloads.getContentUri(hVolumeNames.elementAt(1))

        val hItems = hContentResolver.insert(hCollection, hContentValues)

        if (hItems != null) {
            hContentResolver.openOutputStream(hItems).use { out ->
                hPdfDocument.writeTo(out)
            }
        }
        hContentValues.clear()
        hContentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        hItems?.let { hContentResolver.update(it, hContentValues, null, null) }

        Toast.makeText(
            applicationContext,
            "Download successfully to ${hItems?.path}",
            Toast.LENGTH_LONG
        ).show()


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val hUri: Uri?
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_REQUEST_CODE) {
                if (data != null) {
                    hUri = data.data
                    hWriteContentofFile(hUri)
                }

            }
        }
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                hActivityFileBinding.hPdfNameTv.text = documentUri.path.toString()
            }
        }
    }
}