package com.example.musicplayer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicplayer.data.HomeSection
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import com.example.musicplayer.ui.components.BottomNavBar
import com.example.musicplayer.ui.components.MainTab
import com.example.musicplayer.ui.components.MiniPlayerBar
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.musicplayer.viewmodel.PlayerViewModel
import java.util.ArrayList

class MainActivity : ComponentActivity() {

    private var isBound = false
    private val playerViewModel: PlayerViewModel by viewModels()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            playerViewModel.setService(binder.getService())
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi động Foreground Service ngay từ đây để giữ instance duy nhất toàn app
        val serviceIntent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            LaunchedEffect(Unit) { mainViewModel.load() }

            val homeSections by mainViewModel.homeSections.collectAsState()
            val allSongs by mainViewModel.allSongs.collectAsState()
            val searchQuery by mainViewModel.searchQuery.collectAsState()
            val searchResults by mainViewModel.searchResults.collectAsState()
            val currentSong by playerViewModel.currentSong.collectAsState()
            val isPlaying by playerViewModel.isPlaying.collectAsState()

            val onSongClick: (List<Song>, Int) -> Unit = { list, position ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putParcelableArrayListExtra("listSong", ArrayList(list))
                    putExtra("position", position)
                }
                startActivity(intent)
            }

            MainScreen(
                homeSections = homeSections,
                allSongs = allSongs,
                searchQuery = searchQuery,
                searchResults = searchResults,
                onSearchQueryChange = { mainViewModel.onSearchQueryChange(it) },
                onSongClick = onSongClick,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onMiniBarClick = { startActivity(Intent(this, PlayerActivity::class.java)) },
                onPrevClick = { playerViewModel.previousSong() },
                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                onNextClick = { playerViewModel.nextSong() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    homeSections: List<HomeSection>,
    allSongs: List<Song>,
    searchQuery: String,
    searchResults: List<Song>,
    onSearchQueryChange: (String) -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    currentSong: Song?,
    isPlaying: Boolean,
    onMiniBarClick: () -> Unit,
    onPrevClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Zing MP3 Clone") }) },
        bottomBar = {
            Column {
                currentSong?.let { song ->
                    MiniPlayerBar(
                        song = song,
                        isPlaying = isPlaying,
                        onBarClick = onMiniBarClick,
                        onPrevClick = onPrevClick,
                        onPlayPauseClick = onPlayPauseClick,
                        onNextClick = onNextClick
                    )
                }
                BottomNavBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            MainTab.HOME -> HomeScreen(
                sections = homeSections,
                onSongClick = onSongClick,
                modifier = Modifier.padding(paddingValues)
            )
            MainTab.SEARCH -> SearchScreen(
                query = searchQuery,
                results = searchResults,
                onQueryChange = onSearchQueryChange,
                onSongClick = onSongClick,
                modifier = Modifier.padding(paddingValues)
            )
            MainTab.LIBRARY -> LibraryScreen(
                songs = allSongs,
                onSongClick = onSongClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
