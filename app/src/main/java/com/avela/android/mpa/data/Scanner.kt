package com.avela.android.mpa.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.avela.android.mpa.db.AppDatabase
import com.avela.android.mpa.extensions.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase
) {
    fun scan(context: Context): Pair<MutableList<Audio?>, MutableList<Album>> {
        val a = "content://com.android.externalstorage.documents/tree/primary%3AMusic"
        val list = mutableListOf<Audio?>()
        val alist = mutableListOf<Album>()

        val documentTree = DocumentFile.fromTreeUri(context.applicationContext, a.toUri())
        Timber.d("${documentTree?.isDirectory}")
        if (documentTree != null) {
            scanDirectory(context, documentTree, list, alist)
        }
        return Pair(list, alist)
    }

    fun scanEmbeddedArt(audio: Audio): ByteArray? {
        Timber.d(audio.title)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, audio.uri)
        val byte = mediaMetadataRetriever.embeddedPicture
        Timber.d("${audio.title} ${byte.toString()}")
        return byte
    }

    private fun scanDirectory(
        context: Context, file: DocumentFile, list: MutableList<Audio?>, aList:
        MutableList<Album>
    ) {
        val li = file.listFiles()

        li.map {
            if (it.isDirectory) {
                scanDirectory(context, it, list, aList)
            } else {
                if (it.isAudio()) {
//                    Timber.d("${it.name}")

                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, it?.uri)
                    val imageFile = scanAlbumArt(it)
                    audioMetadata(list, aList, mediaMetadataRetriever, it.uri, imageFile)
                }
            }
        }
    }

    private fun getAlbumAndImage(
        aList: MutableList<Album>,
        mediaMetadataRetriever: MediaMetadataRetriever,
        imageFile: DocumentFile?
    ) {
        val album = mediaMetadataRetriever.album
        val albumArtist = mediaMetadataRetriever.albumArtist
        val artist = mediaMetadataRetriever.artist

        val albumExist = aList.any {
            it.name == album && if (it.albumArtist != UNKNOWN) {
                it.albumArtist == albumArtist
            } else {
                it.artist == artist
            }
        }
        if (!albumExist) aList += Album(0, album, artist, albumArtist, imageFile?.uri)
    }

    private fun scanAlbumArt(file: DocumentFile): DocumentFile? {
        val list = file.parentFile?.listFiles()

        return list?.find { it.isAlbumArtImage() }
    }

    private fun audioMetadata(
        list: MutableList<Audio?>, aList: MutableList<Album>, mediaMetadataRetriever: MediaMetadataRetriever, uri:
        Uri,
        imageFile: DocumentFile?
    ) {
        val title = mediaMetadataRetriever.title
        val album = mediaMetadataRetriever.album
        val track = mediaMetadataRetriever.track
        val artist = mediaMetadataRetriever.artist
        val albumArtist = mediaMetadataRetriever.albumArtist
        val duration = mediaMetadataRetriever.duration
        val genre = mediaMetadataRetriever.genre
        val disc = mediaMetadataRetriever.discNumber
        val image = mediaMetadataRetriever.embeddedPicture
        var coverFile = imageFile

        if (coverFile == null) {
            val embeddedExist = image != null
            val folder = File(context.filesDir, "cover")
            if (!folder.exists()) {
                folder.mkdir()
            }
            val cover = File(folder, "${album}.jpg")
            if (embeddedExist && !cover.exists()) {
                cover.writeBytes(image!!)
                coverFile = DocumentFile.fromFile(cover)
            } else if (cover.exists()) {
                coverFile = DocumentFile.fromFile(cover)
            }
        }

        val albumExist = aList.any {
            it.name == album && if (it.albumArtist != UNKNOWN) {
                it.albumArtist == albumArtist
            } else {
                it.artist == artist
            }
        }
        if (!albumExist){
            aList += Album(0, album, artist, albumArtist, coverFile?.uri)
        }

        Timber.d("$title, $album, $track, $artist, $albumArtist, $duration, $genre")
        list += Audio(
            0,
            uri,
            title,
            album,
            image != null,
            image,
            coverFile?.uri ?: Uri.EMPTY,
            genre,
            track,
            artist,
            albumArtist,
            duration
        )
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? =
            context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

//    private fun getAudioMetadata(context: Context, file: DocumentFile): Audio? {
//        context.contentResolver.query(
//            file.uri, null, null, null, null,
//        )!!.use { cursor ->
//            try {
//                // Cache column indices.
//                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//                val nameColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
//                val albumColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
//                val albumIdColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
//                val trackNumberColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
//                val artistColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//                val albumArtistColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
//                } else {
//                    cursor.getColumnIndexOrThrow("album_artist")
//                }
//                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
//
//                if (cursor.moveToFirst()) {
//                    // Get values of columns for a given video.
//                    val id = cursor.getLong(idColumn)
//                    val data = cursor.getString(dataColumn)
//                    val name = cursor.getString(nameColumn) ?: "unknown"
//                    val album = cursor.getString(albumColumn) ?: "unknown"
//                    val albumId = cursor.getLong(albumIdColumn)
//                    val track = cursor.getLong(trackNumberColumn)
//                    var genre = "unknown"
//                    val audioUri = MediaStore.Audio.Genres.getContentUriForAudioId(file.uri.toString(), id.toInt())
//                    val genreProj = arrayOf(
//                        MediaStore.Audio.Genres.NAME,
//                        MediaStore.Audio.Genres._ID,
//                    )
//                    context.contentResolver.query(
//                        audioUri, genreProj, null, null
//                    )?.use {
//                        while (it.moveToNext()) {
//                            val genreColumn =
//                                it.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
//                            genre = it.getString(genreColumn) ?: "unknown"
//                        }
//                    }
//
//                    val artist = cursor.getString(artistColumn) ?: "unknown"
//                    val albumArtist = cursor.getString(albumArtistColumn) ?: "unknown"
//                    val duration = cursor.getInt(durationColumn)
//
//                    val contentUri: Uri = file.uri
//
//                    // Stores column values and the contentUri in a local object
//                    // that represents the media file.
//                    return Audio(
//                        contentUri,
//                        id.toString(),
//                        data,
//                        name,
//                        album,
//                        albumId,
//                        genre,
//                        track,
//                        artist,
//                        albumArtist,
//                        duration
//                    )
//                }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
//        return null
//    }

//    fun scanAudio(context: Context): List<Audio> {
//        val audioList = mutableListOf<Audio>()
//        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            arrayOf(
//                MediaStore.Audio.Media._ID,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media.TITLE,
//                MediaStore.Audio.Media.ALBUM,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.TRACK,
//                MediaStore.Audio.Media.ALBUM_ARTIST,
//                MediaStore.Audio.Media.DURATION,
//            )
//        } else {
//            arrayOf(
//                MediaStore.Audio.Media._ID,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media.TITLE,
//                MediaStore.Audio.Media.ALBUM,
//                MediaStore.Audio.Genres.NAME,
//                MediaStore.Audio.Media.TRACK,
//                MediaStore.Audio.Media.ARTIST,
//                "album_artist",
//                MediaStore.Audio.Media.DURATION,
//            )
//        }
//
//        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
//
//        val selectionArgs = arrayOf(
//            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
//        )
//
//        val sortOrder = "${MediaStore.Audio.Media.TRACK} ASC"
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val volumeNames: Set<String> =
//                MediaStore.getExternalVolumeNames(context)
//            Timber.d(volumeNames.toString())
//            var count = 0
//            val iterator = volumeNames.iterator()
//            while (iterator.hasNext()) {
//                count = count.inc()
//                Timber.tag("loop").d("$count")
//                val uri = iterator.next()
//
//                val collection =
//                    MediaStore.Audio.Media.getContentUri(
//                        uri
//                    )
//                audioList += buildQuery(context, uri, collection, projection, selection, selectionArgs, sortOrder)
//            }
//            return audioList
//        } else {
//            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//            audioList += buildQuery(context, uri = "", collection, projection, selection, selectionArgs, sortOrder)
//        }
//        return audioList
//    }
//
//    private fun buildQuery(
//        context: Context,
//        uri: String = "",
//        collection: Uri,
//        projection: Array<String>? = null,
//        selection: String? = null,
//        selectionArgs: Array<String>? = null,
//        sortOrder: String? = null
//    ): MutableList<Audio> {
//        val audioList = mutableListOf<Audio>()
//        val query = context.contentResolver.query(
//            collection,
//            null,
//            selection,
//            null,
//            sortOrder
//        )
//
//        query?.let { cursor ->
//            try {
//                // Cache column indices.
//                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//                val nameColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
//                val albumColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
//                val trackNumberColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
//                val artistColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//                val albumArtistColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
//                } else {
//                    cursor.getColumnIndexOrThrow("album_artist")
//                }
//                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
//
//                while (cursor.moveToNext()) {
//                    // Get values of columns for a given video.
//                    val id = cursor.getLong(idColumn)
//                    val data = cursor.getString(dataColumn)
//                    val name = cursor.getString(nameColumn) ?: "unknown"
//                    val album = cursor.getString(albumColumn) ?: "unknown"
//                    val track = cursor.getLong(trackNumberColumn)
//                    var genre = "unknown"
//                    val audioUri = MediaStore.Audio.Genres.getContentUriForAudioId(uri, id.toInt())
//                    val genreProj = arrayOf(
//                        MediaStore.Audio.Genres.NAME,
//                        MediaStore.Audio.Genres._ID,
//                    )
//                    context.contentResolver.query(
//                        audioUri, genreProj, null, null
//                    )?.use {
//                        while (it.moveToNext()) {
//                            val genreColumn =
//                                it.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
//                            genre = it.getString(genreColumn) ?: "unknown"
//                        }
//                    }
//
//                    val artist = cursor.getString(artistColumn) ?: "unknown"
//                    val albumArtist = cursor.getString(albumArtistColumn) ?: "unknown"
//                    val duration = cursor.getInt(durationColumn)
//
//                    val contentUri: Uri = ContentUris.withAppendedId(
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                            collection
//                        } else {
//                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                        },
//                        id
//                    )
//
//                    // Stores column values and the contentUri in a local object
//                    // that represents the media file.
//                    audioList += Audio(
//                        contentUri, id.toString(), data, name, album, genre, track, artist,
//                        albumArtist,
//                        duration
//                    )
//                }
//                cursor.close()
//                return audioList
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
//        return audioList
//    }


}