package com.avela.android.mpa.ui.home

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.avela.android.mpa.data.Audio
import com.avela.android.mpa.extensions.isPrepared
import com.avela.android.mpa.services.MusicServiceConnection
import com.avela.android.mpa.workers.AudioDatabaseWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FilenameFilter
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "asdf"
    }
    val text: LiveData<String> = _text
    private val audioList = mutableListOf<Audio>()

    fun connect() {
        viewModelScope.launch {
//            mpdConnectionRepository.connect(MPDConnectionProfile(1, "test", "10.0.2.2", "", 6600))
//            repository.connect(MPDConnectionProfile(1, "test", "192.168.0.8", "", 6600))
        }
    }

    fun getArtistList() {
        viewModelScope.launch {
//            val albumArtistList = repository.getAlbumArtistList()
//            Timber.d("$albumArtistList")
        }
    }

    fun getList(context: Context) {
//        val l = "/storage/3231-CA71/Music/[STAL-1301] TOHO BOSSA NOVA 2/01 Unity-Gain - Close to your Mind.flac"
//        val pathsd = context.externalMediaDirs
//        Environment.getExternalStorageDirectory()
//        val a = ContextCompat.getExternalFilesDirs(context.applicationContext, null)
//        a.forEach {
//            Timber.d("${it.path}")
//        }
//        val file = File(l)
//        val parent = File(file.parent)
//        Timber.d("${parent.path}")
//        Timber.d("${parent.isDirectory}")
//        val pattern = Regex("Folder.+")
//        Timber.d("${parent.listFiles()}")
//        val art = parent.listFiles { dir, name -> name.matches(pattern) }
//        Timber.d("$art")

        val folder = File(context.filesDir, "cacheCover")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val filename = "myfile"
        val fileContents = "Hello world!"
        val ff = File(folder, filename)
    }

    fun openDirectory(activity: Activity, pickerInitialUri: Uri) {
//        val a = "content://com.android.externalstorage.documents/tree/3231-CA71%3AMusic"
//        val documentTree = DocumentFile.fromTreeUri(activity, a.toUri())
//        Timber.d("${documentTree?.isDirectory}")
//        documentTree?.listFiles()?.forEach {
//            Timber.d("${it.name}")
//        }
//        Timber.d("${documentTree?.isDirectory}")
//        Audio.scan(activity as Context)
    }

    fun play(context: Context) {
        val transportControls = musicServiceConnection.transportControls
        transportControls.playFromMediaId("236280", null)
    }
}

const val OPEN_DIRECTORY_CODE = 111