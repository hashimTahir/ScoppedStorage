/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hashim.scoppedstorage.Constants.PERMISSION_READ_EXTERNAL_STORAGE
import com.hashim.scoppedstorage.Constants.PERMISSION_REQUEST_CODE
import timber.log.Timber

object CheckPemission {



    fun checkPermissionForAML(context: Context): Boolean {
        Timber.d("checkPermissionForAML")
        val result: Int =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        return result == PackageManager.PERMISSION_GRANTED
    }


    fun requestPermissionForAML(context: Context) {
        Timber.d("requestPermissionForAML")

        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(android.Manifest.permission.ACCESS_MEDIA_LOCATION), PERMISSION_REQUEST_CODE
        )

    }

    fun hCheckForReadWritePermissions(context: Context): Boolean {
       Timber.d( "checkPermissionForREadWriteBelowP")
        val result: Int =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )

        return result == PackageManager.PERMISSION_GRANTED
    }


    fun hRequestPermissionForReadWrite(context: Context) {
       Timber.d( "requestPermissionForREadWriteBelowP")

        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), PERMISSION_READ_EXTERNAL_STORAGE
        )

    }

    fun checkMultiplePermissions(permissionList: ArrayList<String>, context: Context): Boolean {
        val arrPermission = checkPermissions(permissionList, context)
        if (arrPermission.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrPermission,
                PERMISSION_READ_EXTERNAL_STORAGE
            )
            return true
        }
        return false
    }

    private fun checkPermissions(
        permissionList: ArrayList<String>,
        context: Context
    ): Array<String?> {
        val arrPermission = ArrayList<String>()

        for (i in permissionList.indices) {
            val permission = permissionList[i]
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                arrPermission.add(permission)
            }
        }
        return arrPermission.toTypedArray()
    }


}