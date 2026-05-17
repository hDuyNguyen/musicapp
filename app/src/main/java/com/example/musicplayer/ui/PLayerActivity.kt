package com.example.musicplayer.ui


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import com.example.musicplayer.ui.components.BackgroundBlurImage
import com.example.musicplayer.ui.components.MarqueeText
import com.example.musicplayer.ui.components.PlayerControlBar
import com.example.musicplayer.ui.components.SliderBar
import com.example.musicplayer.utils.MusicUtils.formatTime
import com.example.musicplayer.utils.MusicUtils.getAlbumArtUri
import com.example.musicplayer.viewmodel.PlayerViewModel

class PLayerActivity : ComponentActivity() {
    private var currentPosition: Int = 0

    private val playerViewModel: PlayerViewModel by viewModels()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val songs =
            (intent.getParcelableExtra("songList", ArrayList::class.java) as? ArrayList<Song>)
                ?: emptyList()
        currentPosition = intent.getIntExtra("position", 0)

        if (songs.isEmpty() || currentPosition < 0 || currentPosition >= songs.size) {
            Toast.makeText(this, "No song to play", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if there's an issue
            return
        }
        playerViewModel.setupPlayer(songs, currentPosition)

        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    BackgroundBlurImage(songs[currentPosition].albumId)
                }
                PlayerScreen(
                    song = songs[currentPosition],
                    viewModel = playerViewModel,
                    onBack = { finish() }
                )
            }
        }
    }

    @Composable
    fun PlayerScreen(song: Song, viewModel: PlayerViewModel, onBack: () -> Unit) {
        val isPlaying by viewModel.isPlaying.collectAsState()
        val currentPos by viewModel.currentPosition.collectAsState()
        val isFavourite by viewModel.isFavourite.collectAsState()
        val isShuffle by viewModel.isShuffle.collectAsState()
        val repeatMode by viewModel.repeatMode.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TopBar Custom
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.back_icon),
                        null,
                        tint = Color.Unspecified
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    MarqueeText(song.title ?: "", color = Color.White)
                    Text(
                        song.artist ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        painterResource(R.drawable.menu_icon),
                        null,
                        tint = Color.Unspecified
                    )
                }
            }

            // Content
            AsyncImage(
                model = getAlbumArtUri(song.albumId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(200.dp)),
                placeholder = painterResource(R.drawable.music_icon)
            )

            // InteractionBar
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { viewModel.toggleFavourite() }) {
                    Icon(
                        painterResource(if (isFavourite) R.drawable.favourite_selected_icon else R.drawable.favourite_icon),
                        null, tint = Color.Unspecified
                    )
                }
                IconButton(onClick = { viewModel }) {
                    Icon(
                        painterResource(R.drawable.list_add_icon),
                        null,
                        tint = Color.Unspecified
                    )
                }
                IconButton(onClick = { viewModel }) {
                    Icon(
                        painterResource(R.drawable.download_icon),
                        null,
                        tint = Color.Unspecified
                    )
                }
                IconButton(onClick = { viewModel }) {
                    Icon(
                        painterResource(R.drawable.menu_icon),
                        null,
                        tint = Color.Unspecified
                    )
                }
            }

            // SliderBar
            SliderBar(viewModel)

            // ControlBar
            PlayerControlBar(
                isPlaying = isPlaying,
                isShuffle = isShuffle, // Connect to state
                repeatMode = repeatMode,    // Connect to state
                onShuffle = { viewModel.toggleShuffle() },
                onPrev = { viewModel.previous() },
                onToggle = { viewModel.togglePlayPause() },
                onNext = { viewModel.next() },
                onRepeat = { viewModel.toggleRepeat() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("duy", "onDestroy PlayerActivity")
        playerViewModel.clearPlayer()
    }
}