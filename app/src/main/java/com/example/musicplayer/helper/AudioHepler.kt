package com.example.musicplayer.helper

import android.content.Context
import android.provider.MediaStore
import com.example.musicplayer.model.Song

object AudioHelper {
    fun getAllSongs(context: Context): List<Song> {
        val songList = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor = context.contentResolver.query(uri, projection, selection, null, null)

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                songList.add(
                    Song(
                        id = it.getLong(idCol),
                        title = it.getString(titleCol),
                        artist = it.getString(artistCol),
                        albumId = it.getLong(albumCol),
                        data = it.getString(dataCol),
                        duration = it.getLong(durationCol)
                    )
                )
            }
        }
        return songList
    }
}