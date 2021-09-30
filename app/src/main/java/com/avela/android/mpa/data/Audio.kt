package com.avela.android.mpa.data

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.room.*
import com.avela.android.mpa.extensions.*
import java.util.concurrent.TimeUnit

@Entity(tableName = "audio")
data class Audio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long,
    @ColumnInfo(name = "uri")
    var uri: Uri,
    @ColumnInfo(name = "title")
    var title: String = "UNKNOWN",
    @ColumnInfo(name = "album_name")
    var album: String = "UNKNOWN",
    @ColumnInfo(name = "has_embedded_art")
    var hasEmbeddedArt: Boolean = false,
    @Ignore var embedArt: ByteArray? = null,
    @ColumnInfo(name = "cover_uri")
    var coverUri: Uri = Uri.EMPTY,
    @ColumnInfo(name = "genre")
    var genre: String = "UNKNOWN",
    @ColumnInfo(name = "track_number")
    var trackNumber: Long,
    @ColumnInfo(name = "artist")
    var artist: String = "UNKNOWN",
    @ColumnInfo(name = "album_artist")
    var albumArtist: String = "UNKNOWN",
    @ColumnInfo(name = "duration")
    var duration: Long,
) {
    constructor() : this(0, Uri.EMPTY, "", "", false, null, Uri.EMPTY, "", 0, "", "", 0)

    companion object {
        fun buildMediaMetaData(context: Context, list: List<Audio>): List<MediaMetadataCompat> {
            return list.map {
                val item = MediaMetadataCompat.Builder()
                    .from(it)
                    .build()
                item
            }.toList()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Audio

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (title != other.title) return false
        if (album != other.album) return false
        if (embedArt != null) {
            if (other.embedArt == null) return false
            if (!embedArt.contentEquals(other.embedArt)) return false
        } else if (other.embedArt != null) return false
        if (genre != other.genre) return false
        if (trackNumber != other.trackNumber) return false
        if (artist != other.artist) return false
        if (albumArtist != other.albumArtist) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + (embedArt?.contentHashCode() ?: 0)
        result = 31 * result + genre.hashCode()
        result = 31 * result + trackNumber.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}

/**
 * Extension method for [MediaMetadataCompat.Builder] to set the fields from
 * our JSON constructed object (to make the code a bit easier to see).
 */
fun MediaMetadataCompat.Builder.from(audio: Audio): MediaMetadataCompat.Builder {
    // The duration from the JSON is given in seconds, but the rest of the code works in
    // milliseconds. Here's where we convert to the proper units.
    val durationMs = TimeUnit.SECONDS.toMillis(audio.duration.toLong())

    id = audio.id.toString()
    title = audio.title
    artist = audio.artist
    album = audio.album
    duration = durationMs
    mediaUri = audio.uri.toString()

    albumArtUri = audio.coverUri.toString()
    genre = audio.genre
    trackNumber = audio.trackNumber
    flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

    // To make things easier for *displaying* these, set the display properties as well.
    displayTitle = audio.title
    displaySubtitle = audio.artist
    displayDescription = audio.album

    // Add downloadStatus to force the creation of an "extras" bundle in the resulting
    // MediaMetadataCompat object. This is needed to send accurate metadata to the
    // media session during updates.
    downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED

    // Allow it to be used in the typical builder style.
    return this
}

