package com.avela.android.mpa.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album")
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "album_name")
    val name: String,
    @ColumnInfo(name = "artist")
    var artist: String = "UNKNOWN",
    @ColumnInfo(name = "album_artist")
    var albumArtist: String = "UNKNOWN",
    @ColumnInfo(name = "cover_uri")
    val coverUri: Uri?,
) {
    data class AlbumItemData(
        val name: String,
        val artist: String,
        val albumArtist: String,
        val coverUri: Uri?,
        val embeddedArt: ByteArray?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AlbumItemData

            if (name != other.name) return false
            if (artist != other.artist) return false
            if (albumArtist != other.albumArtist) return false
            if (coverUri != other.coverUri) return false
            if (embeddedArt != null) {
                if (other.embeddedArt == null) return false
                if (!embeddedArt.contentEquals(other.embeddedArt)) return false
            } else if (other.embeddedArt != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + artist.hashCode()
            result = 31 * result + albumArtist.hashCode()
            result = 31 * result + (coverUri?.hashCode() ?: 0)
            result = 31 * result + (embeddedArt?.contentHashCode() ?: 0)
            return result
        }

    }
}
