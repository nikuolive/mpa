package com.avela.android.mpa.extensions

import android.media.MediaMetadataRetriever
import timber.log.Timber

const val UNKNOWN = "<UNKNOWN>"

inline val MediaMetadataRetriever.title: String
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: UNKNOWN


inline val MediaMetadataRetriever.album: String
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: UNKNOWN


inline val MediaMetadataRetriever.track: Long
    get() {
        val track = extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) ?: UNKNOWN
        return if (track == UNKNOWN) {
            0
        } else {
            if (track.any { it == '/' }) {
                track.split("/")[0].toLong()
            } else {
                track.toLong()
            }
        }
    }


inline val MediaMetadataRetriever.discNumber: String
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) ?: "1"

inline val MediaMetadataRetriever.genre: String
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: UNKNOWN

inline val MediaMetadataRetriever.artist: String
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: UNKNOWN

inline val MediaMetadataRetriever.albumArtist: String
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: UNKNOWN

inline val MediaMetadataRetriever.duration: Long
    get() {
        val duration = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: UNKNOWN
        return if (duration == UNKNOWN) {
            0
        } else {
            duration.toLong()
        }
    }
