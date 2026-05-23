package com.example.musicplayer.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.model.Song
import kotlinx.coroutines.flow.MutableStateFlow

class MusicService : Service() {

    private val binder = MusicBinder()

    companion object {
        private var exoPlayerInstance: ExoPlayer? = null
        val currentPlayingSong = MutableStateFlow<Song?>(null)
        val isPlaying = MutableStateFlow(false)
        val currentPosition = MutableStateFlow(0L)
    }

    val player: ExoPlayer
        get() = exoPlayerInstance ?: throw IllegalStateException("Player format errors")

    private var originalList: List<Song> = emptyList()
    private var currentList: List<Song> = emptyList()
    private var currentIndex: Int = 0

    override fun onCreate() {
        super.onCreate()
        if (exoPlayerInstance == null) {
            exoPlayerInstance = ExoPlayer.Builder(this).build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
            }
        }
        startForegroundService()
        setupPlayerListener()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun startForegroundService() {
        val channelId = "music_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Music Playback", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Player")
            .setContentText("Playing music in background...")
            .setSmallIcon(R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying.value = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    if (player.repeatMode == Player.REPEAT_MODE_ONE) {
                        // Tự động lặp lại theo logic ExoPlayer
                    } else {
                        next()
                    }
                }
            }
        })

        // Cập nhật SeekBar Position liên tục
        Handler(mainLooper).post(object : Runnable {
            override fun run() {
                if (isPlaying.value) {
                    currentPosition.value = player.currentPosition
                }
                Handler(mainLooper).postDelayed(this, 1000)
            }
        })
    }

    fun setPlaylist(songs: List<Song>, index: Int) {
        originalList = songs
        currentList = songs
        currentIndex = index
        playCurrentSong()
    }

    private fun playCurrentSong() {
        if (currentList.isEmpty()) return
        val song = currentList[currentIndex]
        currentPlayingSong.value = song

        player.stop()
        player.clearMediaItems()
        song.data?.let { path ->
            player.setMediaItem(MediaItem.fromUri(path))
            player.prepare()
            player.play()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        if (currentIndex < currentList.size - 1) {
            currentIndex++
            playCurrentSong()
        } else if (player.repeatMode == Player.REPEAT_MODE_ALL || player.repeatMode == Player.REPEAT_MODE_ONE) {
            currentIndex = 0 // Repeat list từ đầu
            playCurrentSong()
        }
    }

    fun previous() {
        if (currentIndex > 0) {
            currentIndex--
            playCurrentSong()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun setShuffle(enable: Boolean) {
        if (enable) {
            val currentSong = currentPlayingSong.value
            currentList = originalList.shuffled()
            currentIndex = currentSong?.let { currentList.indexOf(it) } ?: 0
        } else {
            val currentSong = currentPlayingSong.value
            currentList = originalList
            currentIndex = currentSong?.let { currentList.indexOf(it) } ?: 0
        }
    }

    fun setRepeat(enable: Boolean) {
        player.repeatMode = if (enable) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    override fun onDestroy() {
        super.onDestroy()
        // Giải phóng tài nguyên khi toàn bộ ứng dụng bị kill hoàn toàn
        exoPlayerInstance?.release()
        exoPlayerInstance = null
    }
}