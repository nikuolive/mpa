package com.avela.android.mpa.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.avela.android.mpa.data.Audio
import com.avela.android.mpa.data.Scanner
import com.avela.android.mpa.db.AppDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

@HiltWorker
class AudioDatabaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val scanner: Scanner
) : CoroutineWorker(context, workerParameters) {

    @Suppress("UNCHECKED_CAST")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Running")
            val list = scanner.scan(applicationContext)
            val database = AppDatabase.getInstance(applicationContext)
            list.first.removeAll { it == null }
            database.audioDao().insertAllAudio(list.first as List<Audio>)
            database.audioDao().insertAllAlbum(list.second.toList())
//            val aList = database.audioDao().getAllAlbum()
//            aList.collect {
//                it.
//            }

            Result.success()
        } catch (ex: Exception) {
            Timber.e("Error seeding database $ex")
            Result.failure()
        }
    }
}