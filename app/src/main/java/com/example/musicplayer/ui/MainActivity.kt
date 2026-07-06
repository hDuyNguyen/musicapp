package com.example.musicplayer.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import com.example.musicplayer.ui.components.MiniPlayerBar
import com.example.musicplayer.ui.components.SongItem
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

        // Check permission Read Audio Storage
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        }

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            LaunchedEffect(Unit) { mainViewModel.loadSongs(this@MainActivity) }

            val songList by mainViewModel.songs.collectAsState()
            val currentSong by playerViewModel.currentSong.collectAsState()
            val isPlaying by playerViewModel.isPlaying.collectAsState()

            MainScreen(
                songList = songList,
                onSongClick = { list, position ->
                    val intent = Intent(this, PlayerActivity::class.java).apply {
                        putParcelableArrayListExtra("listSong", ArrayList(list))
                        putExtra("position", position)
                    }
                    startActivity(intent)
                },
                currentSong = currentSong,
                isPlaying = isPlaying,
                onMiniBarClick = { startActivity(Intent(this, PlayerActivity::class.java)) },
                onFavClick = { currentSong?.let { playerViewModel.toggleFavourite(it) } },
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
    songList: List<Song>,
    onSongClick: (List<Song>, Int) -> Unit,
    currentSong: Song?,
    isPlaying: Boolean,
    onMiniBarClick: () -> Unit,
    onFavClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Playlist") }) },
        bottomBar = {
            currentSong?.let { song ->
                MiniPlayerBar(
                    song = song,
                    isPlaying = isPlaying,
                    onBarClick = onMiniBarClick,
                    onPrevClick = onFavClick,
                    onPlayPauseClick = onPlayPauseClick,
                    onNextClick = onNextClick
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
        ) {
            itemsIndexed(songList) { index, song ->
                SongItem(song = song, onClick = { onSongClick(songList, index) })
            }
        }
    }
}
