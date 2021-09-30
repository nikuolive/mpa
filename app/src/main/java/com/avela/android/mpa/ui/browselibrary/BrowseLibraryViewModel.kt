package com.avela.android.mpa.ui.browselibrary

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.*
import com.avela.android.mpa.data.Album
import com.avela.android.mpa.data.MusicRepository
import com.avela.android.mpa.services.MY_MEDIA_ALBUM_ID
import com.avela.android.mpa.services.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseLibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    val list = MutableLiveData<List<Album.AlbumItemData>>()

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            playMedia(parentId)
//            val itemsList = children.map { child ->
//                val subtitle = child.description.subtitle ?: ""
//                MediaItemData(
//                    child.mediaId!!,
//                    child.description.title.toString(),
//                    subtitle.toString(),
//                    child.description.iconUri!!,
//                    child.isBrowsable,
//                    getResourceForMediaId(child.mediaId!!)
//                )
//            }
//            _mediaItems.postValue(itemsList)
        }
    }

    init {
        viewModelScope.launch {
            musicRepository.albumList.collect { aList ->
                val albumItemDataList = mutableListOf<Album.AlbumItemData>()
                aList.forEach {
                    albumItemDataList.add(Album.AlbumItemData(it.name, it.artist, it.albumArtist, it.coverUri, null))
                }
                list.value = albumItemDataList.sortedBy { album -> album.name }
            }
        }
    }

    fun getSongList(album: Album.AlbumItemData) {
//        viewModelScope.launch {
//            musicRepository.getAudiosFromAlbum(albumName).collectLatest {
//                it.forEach { audio ->
//                    Timber.d("${audio.title} track: ${audio.trackNumber}")
//                }
//            }
//        }
        val id = "$MY_MEDIA_ALBUM_ID-${album.name}"
        musicServiceConnection.playlistId += id
        musicServiceConnection.also {
            it.subscribe(id, subscriptionCallback)
        }
    }

    fun playMedia(mediaId: String, pauseAllowed: Boolean = true) {
        musicServiceConnection.playMediaId(mediaId)
    }
}