package com.avela.android.mpa.ui.playlist

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.avela.android.mpa.R
import com.avela.android.mpa.extensions.*
import com.avela.android.mpa.services.EMPTY_PLAYBACK_STATE
import com.avela.android.mpa.services.MusicServiceConnection
import com.avela.android.mpa.services.NOTHING_PLAYING
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.floor

class PlaylistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val musicServiceConnection: MusicServiceConnection
) : AndroidViewModel(context as Application) {

    //    val list: MutableLiveData<MutableList<MPDListResponse>?> = MutableLiveData()
    data class NowPlayingMetadata(
        val id: String,
        val albumArtUri: Uri?,
        val albumArt: Bitmap?,
        val title: String?,
        val subtitle: String?,
        val duration: String
    ) {

        companion object {
            /**
             * Utility method to convert milliseconds to a display of minutes and seconds
             */
            fun timestampToMSS(context: Context, position: Long): String {
                val totalSeconds = floor(position / 1E3).toInt()
                val minutes = totalSeconds / 60
                val remainingSeconds = totalSeconds - (minutes * 60)
                return if (position < 0) context.getString(R.string.duration_unknown)
                else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
            }
        }
    }

    val mediaButtonRes = MutableLiveData<Int>().apply {
        postValue(R.drawable.play_to_stop)
    }

    val mediaMetadata = MutableLiveData<NowPlayingMetadata>()
    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    val mediaPosition = MutableLiveData<Pair<Long, Float>>().apply {
        postValue(Pair(0L, 0F))
    }
    private var currentSongDuration = 0L

    private var updatePosition = true

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(playbackState, it)
    }

    init {
        musicServiceConnection.also {
            it.playbackState.observeForever(playbackStateObserver)
            it.nowPlaying.observeForever(mediaMetadataObserver)
        }

//        val tagName = savedStateHandle.get<String>(TAG_NAME)!!
//        val tagValue = savedStateHandle.get<String>(TAG_VALUE)!!
//        Timber.d("value: $tagName")
//        viewModelScope.launch {
//            list.value = repository.getAlbumFromAlbumArtistList(tagValue)
//        }
    }

    private fun calculateProgress(currPosition: Long): Float {
        val progress = (currPosition.toFloat() / currentSongDuration.toFloat()) * 100F
        return when {
            progress > 100F -> 100F
            progress < 0F -> 0F
            else -> progress
        }

    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ) {

        // Only update media item once we have duration available
        if (mediaMetadata.duration != 0L && mediaMetadata.id != null) {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, mediaMetadata.mediaUri)
            val embedded = mediaMetadataRetriever.embeddedPicture
            var art: Bitmap? = null
            if (embedded != null) {
                art = BitmapFactory.decodeByteArray(embedded, 0, embedded.size)
            }
            val nowPlayingMetadata = NowPlayingMetadata(
                mediaMetadata.id!!,
                mediaMetadata.displayIconUri,
                art,
                mediaMetadata.title?.trim(),
                mediaMetadata.displaySubtitle?.trim(),
                NowPlayingMetadata.timestampToMSS(context, mediaMetadata.duration)
            )
            currentSongDuration = mediaMetadata.duration
            this.mediaMetadata.postValue(nowPlayingMetadata)
        }

        // Update the media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.stop_to_play
                else -> R.drawable.play_to_stop
            }
        )
    }

    fun playMediaId(id: String) {
        musicServiceConnection.playMediaId(id)
    }

    fun setProgress(value: Float) {
        val pos = (currentSongDuration.toFloat() * (value / 100F)).toLong()
        Timber.d("$pos")
        mediaPosition.postValue(Pair(pos, value))
        musicServiceConnection.setMediaProgress(pos)
    }

    fun progressToMSS(value: Float): String {
        val pos = currentSongDuration.toFloat() * (value / 100F)
        val totalSeconds = floor(pos / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)
        return if (pos < 0) context.getString(R.string.duration_unknown)
        else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
    }

    fun playNextSong() {
        musicServiceConnection.playNext()
    }

    fun playPreviousSong() {
        musicServiceConnection.playPrevious()
    }

    companion object {
        private const val TAG_NAME = "tagName"
        private const val TAG_VALUE = "tagValue"
    }
}