package com.avela.android.mpa.services

import android.app.Service
import android.content.Intent
import android.os.*
import timber.log.Timber

class MPDService : Service(){

    private val binder = LocalBinder()
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            Timber.d("Handling message")
            try {
                Timber.d("${msg.obj}")
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun onCreate() {
        Timber.d("Creating Service")

//        mpdConnectionRepository.connect(MPDConnectionProfile(1, "test", "10.0.2.2", "", 6600))
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        Timber.d("starting")
        if (intent.action != null) {
            serviceHandler?.obtainMessage()?.also { msg ->
                msg.arg1 = startId
                msg.obj = intent.action
                serviceHandler?.sendMessage(msg)
            }
        }
        return START_STICKY
    }



    inner class LocalBinder : Binder() {
        fun getService(): MPDService = this@MPDService
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}