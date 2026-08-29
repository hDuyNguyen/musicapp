package com.example.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.FavouriteRepository
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var musicService: MusicService? = null

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite
    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle
    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat

    val currentSong: StateFlow<Song?> = MusicService.currentPlayingSong
    val isPlaying: StateFlow<Boolean> = MusicService.isPlaying
    val currentPosition: StateFlow<Long> = MusicService.currentPosition

    init {
        // Favourite state is per-song now (backed by FavouriteRepository/ContentProvider), so it
        // has to be re-checked every time the currently playing song changes.
        viewModelScope.launch {
            currentSong.collect { song ->
                _isFavourite.value = song?.let { FavouriteRepository.isFavourite(getApplication(), it.id) } ?: false
            }
        }
    }

    fun setService(service: MusicService) {
        this.musicService = service
    }

    fun togglePlayPause() = musicService?.togglePlayPause()
    fun nextSong() = musicService?.next()
    fun previousSong() = musicService?.previous()
    fun seekTo(pos: Long) = musicService?.seekTo(pos)

    fun toggleFavourite(song: Song) {
        viewModelScope.launch {
            _isFavourite.value = FavouriteRepository.toggleFavourite(getApplication(), song)
        }
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
