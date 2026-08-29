package com.example.musicplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String?,
    val artist: String?,
    val albumArtUrl: String?,
    val data: String?, // Local file path or remote stream URL
    val duration: Long
): Parcelable
