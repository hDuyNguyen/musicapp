package com.example.musicplayer.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicplayer.helper.AudioHelper
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import com.example.musicplayer.ui.components.ControlBar
import com.example.musicplayer.ui.components.InteractionBar
import com.example.musicplayer.ui.components.PlayerTopBar
import com.example.musicplayer.ui.components.SliderBar
import com.example.musicplayer.viewmodel.PlayerViewModel
import com.example.musicplayer.R

class PlayerActivity : ComponentActivity() {

    private var isBound = false
    private val playerViewModel: PlayerViewModel by viewModels()
    private var serviceManager by mutableStateOf<MusicService?>(null)

    private val serviceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            val manager = binder.getService()

            playerViewModel.setService(manager)
            isBound = true

            // Đọc dữ liệu từ Intent an toàn theo Android SDK
            val songs =
                intent.getParcelableArrayListExtra("listSong", Song::class.java)?.toList()
                    ?: emptyList()

            val position = intent.getIntExtra("position", 0)

            // Đẩy xử lý danh sách trực tiếp vào Service tập trung
            if (songs.isNotEmpty()) {
                manager.setPlaylist(songs, position)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start and Bind Foreground Service
        val intentService = Intent(this, MusicService::class.java)
        bindService(intentService, serviceConnection, BIND_AUTO_CREATE)

        setContent {
            val currentSong by playerViewModel.currentSong.collectAsState()
            val isPlaying by playerViewModel.isPlaying.collectAsState()
            val currentPos by playerViewModel.currentPosition.collectAsState()

            MaterialTheme {
                PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPos = currentPos,
                    viewModel = playerViewModel,
                    onBack = { finish() }
                )
            }
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

@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPos: Long,
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {

    val isFavourite by viewModel.isFavourite.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred album art background, dimmed so foreground text/controls stay readable
        AsyncImage(
            model = song?.let { AudioHelper.getAlbumArtUri(it.albumId) },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.music_icon),
            fallback = painterResource(R.drawable.music_icon),
            modifier = Modifier
                .fillMaxSize()
                .blur(32.dp)
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PlayerTopBar(
                    title = song?.title ?: "Unknown Title",
                    artist = song?.artist ?: "Unknown Artist",
                    onBackClick = onBack,
                    onMenuClick = {}
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Album Art
                AsyncImage(
                    model = song?.let { AudioHelper.getAlbumArtUri(it.albumId) },
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.music_icon),
                    fallback = painterResource(R.drawable.music_icon),
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                )

                // Interactions Bar
                InteractionBar(
                    isFavourite = isFavourite,
                    onFavClick = { song?.let { viewModel.toggleFavourite(it) } },
                    onAddClick = {},
                    onDownloadClick = {},
                    onShareClick = {},
                    onMoreClick = {}
                )

                // Slider progress bar
                SliderBar(
                    currentPos = currentPos,
                    duration = song?.duration ?: 0L,
                    onValueChange = { viewModel.seekTo(it) }
                )

                // Control Buttons
                ControlBar(
                    isPlaying = isPlaying,
                    isShuffle = isShuffle,
                    isRepeat = isRepeat,
                    onShuffleClick = { viewModel.toggleShuffle() },
                    onPreviousClick = { viewModel.previousSong() },
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.nextSong() },
                    onRepeatClick = { viewModel.toggleRepeat() }
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    MaterialTheme {
        PlayerScreen(
            song = Song(1, "Bài hát demo", "Ca sĩ nghệ sĩ", 1L, "", 240000L),
            isPlaying = true,
            currentPos = 60000L,
            viewModel = PlayerViewModel(),
            onBack = {}
        )
    }
}