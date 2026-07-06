package com.example.musicplayer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel : ViewModel() {

    private var musicService: MusicService? = null

    // UI States sử dụng MutableState theo yêu cầu bài toán
    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite
    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle
    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat

    val currentSong: StateFlow<Song?> = MusicService.currentPlayingSong
    val isPlaying: StateFlow<Boolean> = MusicService.isPlaying
    val currentPosition: StateFlow<Long> = MusicService.currentPosition

    private val favouriteList = mutableListOf<Song>()

    fun setService(service: MusicService) {
        this.musicService = service
    }

    fun togglePlayPause() = musicService?.togglePlayPause()
    fun nextSong() = musicService?.next()
    fun previousSong() = musicService?.previous()
    fun seekTo(pos: Long) = musicService?.seekTo(pos)

    fun toggleFavourite(song: Song) {
        _isFavourite.value = !_isFavourite.value
        if (_isFavourite.value) favouriteList.add(song) else favouriteList.remove(song)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        musicService?.setShuffle(_isShuffle.value)
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
        musicService?.setRepeat(_isRepeat.value)
    }
}