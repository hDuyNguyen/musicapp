package com.example.musicplayer.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri

object MusicUtils {
    fun getAlbumArtUri(albumId: Long): Uri {
        val albumArtUri = "content://media/external/audio/albumart".toUri()
        return ContentUris.withAppendedId(albumArtUri, albumId)
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(time: Long): String {
        val minutes = time / 60000
        val seconds = (time % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
}
