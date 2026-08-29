package com.example.musicplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.HomeSection
import com.example.musicplayer.data.MockMusicRepository
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository: MusicRepository = MockMusicRepository()

    private val _homeSections = MutableStateFlow<List<HomeSection>>(emptyList())
    val homeSections: StateFlow<List<HomeSection>> = _homeSections

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<Song>> = combine(_searchQuery, _allSongs) { query, songs ->
        if (query.isBlank()) {
            emptyList()
        } else {
            songs.filter {
                it.title?.contains(query, ignoreCase = true) == true ||
                    it.artist?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load() {
        viewModelScope.launch {
            _homeSections.value = repository.getHomeSections()
            _allSongs.value = repository.getAllSongs()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
