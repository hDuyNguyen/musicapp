package com.example.musicplayer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.musicplayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(title: String, artist: String, onBackClick: () -> Unit, onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = artist, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(painterResource(R.drawable.back_icon), contentDescription = "back_icon")
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(painterResource(R.drawable.menu_icon   ), contentDescription = "menu_icon")
            }
        }
    )
}