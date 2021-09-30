package com.avela.android.mpa.data

import com.avela.android.mpa.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val scanner: Scanner
) {

    val albumList: Flow<List<Album>> = appDatabase.audioDao().getAllAlbum()

    suspend fun getAudioFromAlbum(album: Album): Audio {
        val audio = appDatabase.audioDao().getAudioFromAlbumByArtistAndAlbumArtist(
            album.name,
            album.artist,
            album.albumArtist
        )
        val image = scanner.scanEmbeddedArt(audio)
        image?.let {
            audio.embedArt = image
        }
        return audio
    }
}