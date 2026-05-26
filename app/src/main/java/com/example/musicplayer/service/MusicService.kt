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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicService : Service() {

    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

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
                if (playing) {
                    startProgressTracker() // Chạy tracker an toàn bằng Coroutine khi phát nhạc
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    next()
                }
            }
        })
    }

    private fun startProgressTracker() {
        stopProgressTracker() // Hủy job cũ trước khi tạo job mới, tuyệt đối không chạy song song
        progressJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    currentPosition.value = player.currentPosition
                }
                delay(1000) // Cập nhật đều đặn mỗi 1 giây
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun setPlaylist(songs: List<Song>, index: Int) {
        val targetSong = songs.getOrNull(index) ?: return

        // Nếu là bài đang phát -> Chỉ cập nhật lại luồng để giao diện đồng bộ, không nạp lại nguồn phát
        if (currentPlayingSong.value?.id == targetSong.id) {
            currentPlayingSong.value = null
            currentPlayingSong.value = targetSong
            return
        }

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
        stopProgressTracker()
        serviceScope.cancel() // Giải phóng toàn bộ coroutines chạy ngầm
        exoPlayerInstance?.release()
        exoPlayerInstance = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // 1. Dừng tracker thời gian SeekBar
        stopProgressTracker()

        // 2. Dừng phát nhạc và giải phóng ExoPlayer an toàn
        exoPlayerInstance?.apply {
            stop()
            release()
        }
        exoPlayerInstance = null

        // 3. Xóa bỏ trạng thái Foreground Notification khỏi thanh thông báo
        stopForeground(STOP_FOREGROUND_REMOVE)

        // 4. Hủy bỏ toàn bộ Coroutine Job đang chạy ngầm trong Service
        serviceScope.cancel()

        // 5. Kết thúc hoàn toàn Service hiện tại
        stopSelf()
    }
}