/*
 * Copyright (c) 2021/  4/ 10.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.imagepicker

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class MediaStoreModel(
    val id: Long,
    val displayName: String,
    val dateTaken: Date,
    val contentUri: Uri
){
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<MediaStoreModel>() {
            override fun areItemsTheSame(oldItem: MediaStoreModel, newItem: MediaStoreModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MediaStoreModel, newItem: MediaStoreModel) =
                oldItem == newItem
        }
    }
}