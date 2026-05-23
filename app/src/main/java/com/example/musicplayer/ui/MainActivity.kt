package com.example.musicplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.musicplayer.ui.components.SongItem
import com.example.musicplayer.viewmodel.MainViewModel
import java.util.ArrayList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            MainScreen(
                songList = songList,
                onSongClick = { list, position ->
                    val intent = Intent(this, PlayerActivity::class.java).apply {
                        putParcelableArrayListExtra("listSong", ArrayList(list))
                        putExtra("position", position)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(songList: List<Song>, onSongClick: (List<Song>, Int) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Playlist") }) }
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