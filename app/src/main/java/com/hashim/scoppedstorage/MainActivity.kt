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
import com.hashim.scoppedstorage.imagepicker.ImagePickerActivity

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
            hLaunchImagePicker()
        }

        hActivityMainBinding.hDownloadImageB.setOnClickListener {
            hDownloadImage()
        }

        hActivityMainBinding.hAccessMediaB.setOnClickListener {
            hAccessMediaLocation()
        }

        hActivityMainBinding.hPickFileB.setOnClickListener {
            hStartFileActivity()
        }
    }


        fun hStartFileActivity() {
        startActivity(Intent(this@MainActivity, FileActivity::class.java))
    }

    fun hAccessMediaLocation() {

        startActivity(Intent(this@MainActivity, AccessMediaLocationActivity::class.java))
    }


    fun hLaunchImagePicker() {
        startActivity(Intent(this@MainActivity, ImagePickerActivity::class.java))
    }

    fun hDownloadImage() {

        startActivity(Intent(this@MainActivity, DownLoadImageActivity::class.java))
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