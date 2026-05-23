package com.example.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.musicplayer.R

@Composable
fun InteractionBar(
    isFavourite: Boolean,
    onFavClick: () -> Unit,
    onAddClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onFavClick) {
            if (isFavourite) {
                Icon(painterResource(R.drawable.favourite_selected_icon), contentDescription = "favourite_selected_icon")
            } else {
                Icon(painterResource(R.drawable.favourite_icon), contentDescription = "favourite_icon")
            }
        }
        IconButton(onClick = onAddClick) { Icon(painterResource(R.drawable.list_add_icon), contentDescription = "list_add_icon") }
        IconButton(onClick = onDownloadClick) { Icon(painterResource(R.drawable.download_icon), contentDescription = "download_icon") }
        IconButton(onClick = onShareClick) { Icon(painterResource(R.drawable.share_icon), contentDescription = "share_icon") }
        IconButton(onClick = onMoreClick) { Icon(painterResource(R.drawable.menu_icon_horizon), contentDescription = "menu_icon_horizon") }
    }
}