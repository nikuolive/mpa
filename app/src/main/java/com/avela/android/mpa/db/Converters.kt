package com.avela.android.mpa.db

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter


class Converters {
//    @TypeConverter
//    fun uriToString(uri: Uri): String = uri.toString()
//
//    @TypeConverter
//    fun stringToUri(string: String): Uri = string.toUri()

    @TypeConverter
    fun uriToString(uri: Uri?): String? = uri?.toString()

    @TypeConverter
    fun stringToUri(string: String?): Uri? = string?.toUri()
}