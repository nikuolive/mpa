package com.avela.android.mpa.extensions

import android.content.ContentResolver
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * This file contains extension methods for the java.io.File class.
 */

/**
 * Returns a Content Uri for the AlbumArtContentProvider
 */
fun File.asAlbumArtContentUri(): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .appendPath(this.path)
        .build()
}

fun DocumentFile.isAudio(): Boolean {
    val audioMime = arrayOf("audio/mpeg", "audio/ogg", "audio/flac")
    return !this.isDirectory && audioMime.any { it == this.type }
}

fun DocumentFile.isAlbumArtImage(): Boolean {
    val imageMime = arrayOf("image/jpeg", "image/jpg", "image/png")
    val knownFilename = arrayOf("cover", "folder")
    return !this.isDirectory && imageMime.any {
        it == this.type
    } && knownFilename.any {
        this.name!!.lowercase()
            .contains(it)
    }
}

private const val AUTHORITY = "com.avela.mpa"
