package com.example.musicplayer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel : ViewModel() {

    private var musicService: MusicService? = null

    // UI States sử dụng MutableState theo yêu cầu bài toán
    var isFavourite by mutableStateOf(false)
    var isShuffle by mutableStateOf(false)
    var isRepeat by mutableStateOf(false)

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
        isFavourite = !isFavourite
        if (isFavourite) favouriteList.add(song) else favouriteList.remove(song)
    }

    fun toggleShuffle() {
        isShuffle = !isShuffle
        musicService?.setShuffle(isShuffle)
    }

    fun toggleRepeat() {
        isRepeat = !isRepeat
        musicService?.setRepeat(isRepeat)
    }
}