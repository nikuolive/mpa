package com.avela.android.mpa.services;

import android.app.Notification
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.avela.android.mpa.R
import com.avela.android.mpa.data.Audio
import com.avela.android.mpa.db.AppDatabase
import com.avela.android.mpa.extensions.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber


private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

const val MY_MEDIA_ALBUM_ID = "album_id"

class MusicService : MediaBrowserServiceCompat() {

    private lateinit var notificationManager: MpaNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var _currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    val currentPlaylistItems: List<MediaMetadataCompat>
        get() = _currentPlaylistItems
    private lateinit var appDatabase: AppDatabase

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSource: List<MediaMetadataCompat>

    private var isForegroundService = false

    private val mpaAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val playerListener = PlayerEventListener()

    private val exoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(mpaAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
            playWhenReady = true
        }
    }

    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(
            /* context= */ this,
            Util.getUserAgent(/* context= */ this, USER_AGENT), /* listener= */
            null
        )
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, "MusicService")
        sessionToken = mediaSession.sessionToken

        notificationManager = MpaNotificationManager(
            this,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        )

        appDatabase = AppDatabase.getInstance(applicationContext)

        serviceScope.launch {
            appDatabase.audioDao().getAll().collect {
                mediaSource = Audio.buildMediaMetaData(applicationContext, it)
            }
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(MpaPlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(MpaQueueNavigator(mediaSession))
        mediaSessionConnector.setPlayer(exoPlayer)
        notificationManager.showNotificationForPlayer(exoPlayer)


    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        if (clientUid == Process.myUid())
            return BrowserRoot(MY_MEDIA_ROOT_ID, null)
        return BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        if (parentId == MY_EMPTY_MEDIA_ROOT_ID) {
            result.sendResult(null)
        } else {
            Timber.d(parentId)
            val split = parentId.split("-", limit = 2)
            if (split[0] == MY_MEDIA_ALBUM_ID) {
                val songList = mediaSource.filter {
                    it.album == split[1]
                }.sortedBy {
                    it.trackNumber
                }
                val items = songList.map {
                    MediaItem(it.description, it.flag)
                }.toMutableList()
                result.sendResult(items)
            } else {
                result.sendResult(null)
            }
        }
    }

    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        // Since the playlist was probably based on some ordering (such as tracks
        // on an album), find which window index to play first so that the song the
        // user actually wants to hear plays first.
//        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
        val initialWindowIndex = 0
        _currentPlaylistItems = metadataList

        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        val mediaSource = metadataList.toMediaSource(dataSourceFactory)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.seekTo(initialWindowIndex, playbackStartPositionMs)
    }

    private inner class UampQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
            _currentPlaylistItems[windowIndex].description
    }

    private inner class MpaPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        /**
         * UAMP supports preparing (and playing) from search, as well as media ID, so those
         * capabilities are declared here.
         *
         * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
         */
        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            return
//            onPrepareFromMediaId(
//                recentSong.mediaId!!,
//                playWhenReady,
//                recentSong.description.extras
//            )
        }

        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            val split = mediaId.split("-", limit = 2)
            Timber.d(mediaId)
            if (split[0] == MY_MEDIA_ALBUM_ID) {
                val itemToPlay = mediaSource.filter {
                    it.album == split[1]
                }.sortedBy {
                    it.trackNumber
                }

                if (itemToPlay.isEmpty()) {
                    Timber.tag(TAG).w( "Content not found: MediaID=$mediaId")
                    // TODO: Notify caller of the error.
                } else {

                    val playbackStartPositionMs =
                        extras?.getLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, C.TIME_UNSET)
                            ?: C.TIME_UNSET

                    preparePlaylist(
                        itemToPlay,
                        playWhenReady,
                        playbackStartPositionMs
                    )
                }
            }
//            val itemToPlay: MediaMetadataCompat? = mediaSource.find { item ->
//                item.id == mediaId
//            }
        }

        /**
         * This method is used by the Google Assistant to respond to requests such as:
         * - Play Geisha from Wake Up on UAMP
         * - Play electronic music on UAMP
         * - Play music on UAMP
         *
         * For details on how search is handled, see [AbstractMusicSource.search].
         */
        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
//            mediaSource.whenReady {
//                val metadataList = mediaSource.search(query, extras ?: Bundle.EMPTY)
//                if (metadataList.isNotEmpty()) {
//                    preparePlaylist(
//                        metadataList,
//                        metadataList[0],
//                        playWhenReady,
//                        playbackStartPositionMs = C.TIME_UNSET
//                    )
//                }
//            }
        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onCommand(
            player: Player,
            controlDispatcher: ControlDispatcher,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ) = false

        /**
         * Builds a playlist based on a [MediaMetadataCompat].
         *
         * TODO: Support building a playlist by artist, genre, etc...
         *
         * @param item Item to base the playlist on.
         * @return a [List] of [MediaMetadataCompat] objects representing a playlist.
         */
        private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
            mediaSource.filter { it.album == item.album }.sortedBy { it.trackNumber }
    }

    private inner class PlayerEventListener : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(exoPlayer)
                    if (playbackState == Player.STATE_READY) {

                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
                        // Search for "media resumption" for more information.
//                        saveRecentSongToStorage()

                        if (!playWhenReady) {
                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed. An alternative would be to provide a
                            // "close" button in the notification which stops playback and clears
                            // the notification.
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }
                }
                else -> {
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = R.string.generic_error
            when (error) {

            }
            Toast.makeText(
                applicationContext,
                error.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    private inner class MpaQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
            _currentPlaylistItems[windowIndex].description
    }
}
const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"

/** Content styling constants */
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2

private const val USER_AGENT = "mpa"

val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

private const val TAG = "MusicService"
