package com.example.musicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun SliderBar(currentPos: Long, duration: Long, onValueChange: (Long) -> Unit) {
    var draggingPos by remember { mutableStateOf<Float?>(null) }
    val displayedPos = draggingPos ?: currentPos.toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = displayedPos,
            onValueChange = { draggingPos = it },
            onValueChangeFinished = {
                draggingPos?.let { onValueChange(it.toLong()) }
                draggingPos = null
            },
            valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(displayedPos.toLong()), style = MaterialTheme.typography.bodySmall)
            Text(text = formatTime(duration), style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}