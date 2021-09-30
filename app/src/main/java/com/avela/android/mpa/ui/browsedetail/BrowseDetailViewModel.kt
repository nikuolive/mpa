package com.avela.android.mpa.ui.browsedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BrowseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

//    val list: MutableLiveData<MutableList<MPDListResponse>?> = MutableLiveData()

    init {
        val tagName = savedStateHandle.get<String>(TAG_NAME)!!
        val tagValue = savedStateHandle.get<String>(TAG_VALUE)!!
        Timber.d("value: $tagName")
        viewModelScope.launch {
//            list.value = repository.getAlbumFromAlbumArtistList(tagValue)
        }
    }

    companion object {
        private const val TAG_NAME = "tagName"
        private const val TAG_VALUE = "tagValue"
    }
}