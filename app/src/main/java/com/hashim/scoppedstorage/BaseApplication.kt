/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        hInitTimber()
    }

    private fun hInitTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : DebugTree() {
                override fun log(
                        priority: Int,
                        tag: String?,
                        message: String,
                        t: Throwable?) {
                    super.log(
                            priority,
                            String.format(Constants.hTag, tag),
                            message,
                            t
                    )
                }
            })
        }
    }
}