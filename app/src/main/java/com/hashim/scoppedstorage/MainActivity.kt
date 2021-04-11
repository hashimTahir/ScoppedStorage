/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.hashim.scoppedstorage.accessMedia.AccessMediaLocationActivity
import com.hashim.scoppedstorage.databinding.ActivityMainBinding
import com.hashim.scoppedstorage.downloadImage.DownLoadImageActivity
import com.hashim.scoppedstorage.file.FileActivity
import com.hashim.scoppedstorage.foldertree.OpenFolderActivity
import com.hashim.scoppedstorage.imagepicker.ImagePickerActivity
import com.hashim.scoppedstorage.madesimple.ExplanationActivity

class MainActivity : AppCompatActivity() {
    lateinit var hActivityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(hActivityMainBinding.root)

        hSetupListerns()

    }

    private fun hSetupListerns() {

        hActivityMainBinding.hImagePickerB.setOnClickListener {
            startActivity(Intent(this, ImagePickerActivity::class.java))
        }

        hActivityMainBinding.hDownloadImageB.setOnClickListener {
            startActivity(Intent(this, DownLoadImageActivity::class.java))
        }

        hActivityMainBinding.hAccessMediaB.setOnClickListener {
            startActivity(Intent(this, AccessMediaLocationActivity::class.java))
        }

        hActivityMainBinding.hPickFileB.setOnClickListener {
            startActivity(Intent(this, FileActivity::class.java))
        }
        hActivityMainBinding.hOpenDocTreeB.setOnClickListener {
            startActivity(Intent(this, OpenFolderActivity::class.java))
        }

        hActivityMainBinding.hSimplified.setOnClickListener {
            startActivity(Intent(this, ExplanationActivity::class.java))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}