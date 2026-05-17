package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import com.example.musicplayer.utils.MusicUtils.formatTime
import com.example.musicplayer.utils.MusicUtils.getAlbumArtUri
import com.example.musicplayer.viewmodel.PlayerViewModel

@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = getAlbumArtUri(song.albumId),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(R.drawable.music_icon)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                MarqueeText(song.title ?: "Unknown", color = Color.Black)
                Text(
                    text = "${song.artist}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PlayerControlBar(
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: Int,
    onShuffle: () -> Unit,
    onPrev: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffle) {
            Icon(
                painterResource(if (isShuffle) R.drawable.shuffle_selected_icon else R.drawable.shuffle_icon),
                null,
                tint = Color.Unspecified
            )
        }
        IconButton(onClick = onPrev) {
            Icon(
                painterResource(R.drawable.previous_icon), null, tint = Color.Unspecified
            )
        }
        IconButton(onClick = onToggle, modifier = Modifier.size(64.dp)) {
            Icon(
                painterResource(if (isPlaying) R.drawable.pause_icon else R.drawable.play_icon),
                null,
                tint = Color.Unspecified
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                painterResource(R.drawable.next_icon), null, tint = Color.Unspecified
            )
        }
        IconButton(onClick = onRepeat) {
            Icon(
                painterResource(if (repeatMode != Player.REPEAT_MODE_OFF) R.drawable.repeat_selected_icon else R.drawable.repeat_icon),
                null,
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun BackgroundBlurImage(albumId: Long) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = albumId?.let { getAlbumArtUri(it) },
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp), // Độ mờ của hình nền
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.music_icon),
            error = painterResource(R.drawable.music_icon)
        )
        // Thêm một lớp phủ tối (Overlay) để chữ dễ đọc hơn
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun SliderBar(viewModel: PlayerViewModel) {
    val currentPos by viewModel.currentPosition.collectAsState()
    val totalDuration by viewModel.duration.collectAsState()

    var isDragging by remember { mutableStateOf(false) }
    var localPosition by remember { mutableFloatStateOf(0f) }

    // Ngăn chặn Slider nhảy loạn khi Recompose liên tục
    val sliderValue = if (isDragging) localPosition else currentPos.toFloat()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                isDragging = true
                localPosition = newValue // Chỉ đổi giao diện tạm thời
            },
            onValueChangeFinished = {
                isDragging = false
                viewModel.seekTo(localPosition) // Thả tay ra mới gọi ExoPlayer đúng 1 lần
            },
            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = formatTime(sliderValue.toLong()), color = Color.White)
            Text(text = formatTime(totalDuration), color = Color.White)
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1, // Bắt buộc 1 dòng để kích hoạt marquee
        modifier = modifier.basicMarquee(
            iterations = Int.MAX_VALUE, // Chạy vô hạn
            repeatDelayMillis = 2000,        // Đợi 2s rồi mới bắt đầu chạy
            initialDelayMillis = 1000,
            velocity = 30.dp           // Tốc độ chạy
        )
    )
}