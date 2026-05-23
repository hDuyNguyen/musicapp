package com.example.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.musicplayer.R

@Composable
fun ControlBar(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffleClick) {
            Icon(
                painterResource(R.drawable.shuffle_icon),
                contentDescription = "shuffle_icon",
                tint = if (isShuffle) Color.Green else Color.Gray
            )
        }

        IconButton(onClick = onPreviousClick) {
            Icon(painterResource(R.drawable.previous_icon), contentDescription = "previous_icon", modifier = Modifier.size(36.dp))
        }

        IconButton(onClick = onPlayPauseClick) {
            Icon(
                if (isPlaying) {
                    painterResource(R.drawable.pause_icon)
                } else {
                    painterResource(R.drawable.play_icon)
                },
                contentDescription = if (isPlaying) "pause_icon" else "play_icon",
                modifier = Modifier.size(48.dp)
            )
        }

        IconButton(onClick = onNextClick) {
            Icon(painterResource(R.drawable.next_icon), contentDescription = "next_icon", modifier = Modifier.size(36.dp))
        }

        IconButton(onClick = onRepeatClick) {
            Icon(
                painterResource(R.drawable.repeat_icon),
                contentDescription = "repeat_icon",
                tint = if (isRepeat) Color.Green else Color.Gray
            )
        }
    }
}