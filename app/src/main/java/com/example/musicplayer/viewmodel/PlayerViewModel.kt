package com.example.musicplayer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val exoPlayer = ExoPlayer.Builder(application).build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite = _isFavourite.asStateFlow()

    init {
        // Sử dụng Listener để đồng bộ trạng thái CHUẨN XÁC từ ExoPlayer ra ngoài UI
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        })
    }

    fun setupPlayer(songs: List<Song>, startIndex: Int) {

        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        if (exoPlayer.mediaItemCount == 0) {
            val mediaItems = songs.map { MediaItem.fromUri(Uri.parse(it.data)) }
            exoPlayer.setMediaItems(mediaItems)
            exoPlayer.prepare()
        }

        exoPlayer.seekTo(startIndex, 0L)
        exoPlayer.play()

        // Update position loop
        viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _currentPosition.value = exoPlayer.currentPosition
                }
                delay(1000)
            }
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        exoPlayer.shuffleModeEnabled = _isShuffle.value
    }

    fun toggleRepeat() {
        val nextMode =
            if (_repeatMode.value == Player.REPEAT_MODE_OFF) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = nextMode
    }

    fun toggleFavourite() {
        _isFavourite.value = !_isFavourite.value
    }

    fun next() {
        exoPlayer.seekToNext()
    }

    fun previous() {
        exoPlayer.seekToPrevious()
    }

    fun seekTo(pos: Float) {
        exoPlayer.seekTo(pos.toLong())
        _currentPosition.value = pos.toLong()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    fun clearPlayer() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }
}