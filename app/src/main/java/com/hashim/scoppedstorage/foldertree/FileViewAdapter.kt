/*
 * Copyright (c) 2021/  4/ 11.  Created by Hashim Tahir
 */

package com.hashim.scoppedstorage.foldertree

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.hashim.scoppedstorage.databinding.ItemFileBinding

class FileViewAdpater(private val context: Context, private val files: ArrayList<DocumentFile>) :
    RecyclerView.Adapter<FileViewAdpater.FileVh>() {


    override fun getItemCount(): Int {
        return files.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileVh {
        return FileVh(
            ItemFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FileVh, position: Int) {
        val hFiles = files[position]

        holder.hItemFileBinding.hFileNameTv.text = hFiles.name
        holder.hItemFileBinding.hFileTypeTv.text = hFiles.type
    }

    class FileVh(val hItemFileBinding: ItemFileBinding) :
        RecyclerView.ViewHolder(hItemFileBinding.root)
}