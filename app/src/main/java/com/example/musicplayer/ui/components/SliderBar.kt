package com.example.musicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun SliderBar(currentPos: Long, duration: Long, onValueChange: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = currentPos.toFloat(),
            onValueChange = { onValueChange(it.toLong()) },
            valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPos), style = MaterialTheme.typography.bodySmall)
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