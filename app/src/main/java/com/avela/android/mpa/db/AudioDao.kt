package com.avela.android.mpa.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.avela.android.mpa.data.Album
import com.avela.android.mpa.data.Audio
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Query("SELECT * FROM audio")
    fun getAll(): Flow<List<Audio>>

    @Query("SELECT * FROM audio WHERE album_name = :album ORDER BY track_number ASC")
    fun getAudiosFromAlbum(album: String): Flow<List<Audio>>

    @Query("SELECT * FROM audio WHERE artist = :artist ORDER BY track_number ASC")
    fun getAudiosFromArtist(artist: String): Flow<List<Audio>>

    @Query("SELECT * FROM album")
    fun getAllAlbum(): Flow<List<Album>>

    @Query(
        "SELECT * FROM Audio WHERE album_name = :albumName AND album_artist = :albumArtist AND artist = :artist LIMIT" +
                " 1"
    )
    suspend fun getAudioFromAlbumByArtistAndAlbumArtist(albumName: String, artist: String, albumArtist: String):
            Audio

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAudio(audios: List<Audio>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlbum(albums: List<Album>)

    @Delete
    fun delete(audio: Audio)

    @Delete
    fun delete(album: Album)
}