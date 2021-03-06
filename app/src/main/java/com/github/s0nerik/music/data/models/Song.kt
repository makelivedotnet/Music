package com.github.s0nerik.music.data.models

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns._ID
import android.provider.MediaStore.Audio.AudioColumns.*
import android.provider.MediaStore.MediaColumns.MIME_TYPE
import android.webkit.MimeTypeMap
import com.github.s0nerik.music.App
import com.github.s0nerik.music.R
import com.github.s0nerik.music.data.helpers.db.CursorFactory
import org.parceler.Parcel
import java.io.Serializable

@Parcel
data class Song(
        val id: Long = 0,
        val artistId: Long = 0,
        val albumId: Long = 0,
        val duration: Int = 0,
        val title: String = "",
        val source: String = "",
        val artistName: String = "",
        val albumName: String = "",
        val lyrics: String = "",
        val mimeType: String = ""
) : Serializable {
    val artistNameForUi: String
        get() {
            if (artistName == "" || artistName == "<unknown>")
                return App.comp.getResources().getString(R.string.unknown_artist)
            else
                return artistName
        }

    val durationString: String
        get() {
            var seconds = duration / 1000
            val minutes = seconds / 60
            seconds -= minutes * 60
            return "$minutes:${String.format("%02d", seconds)}"
        }

    val albumArtUri = ContentUris.withAppendedId(ARTWORK_URI, albumId)!!

    companion object {
        val SUPPORTED_MIME_TYPES: List<String> = arrayOf("mp3", "m4a", "mp4", "aac")
                .map { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }

        val ARTWORK_URI = Uri.parse("content://media/external/audio/albumart")!!
    }

    class Factory : CursorFactory<Song> {
        override fun produce(cursor: Cursor, indices: Map<String, Int>): Song {
            return Song(
                    id = cursor.getLong(indices[_ID]!!),
                    artistId = cursor.getLong(indices[ARTIST_ID]!!),
                    albumId = cursor.getLong(indices[ALBUM_ID]!!),
                    title = cursor.getString(indices[TITLE]!!),
                    artistName = cursor.getString(indices[ARTIST]!!),
                    albumName = cursor.getString(indices[ALBUM]!!),
                    source = cursor.getString(indices[DATA]!!),
                    duration = cursor.getInt(indices[DURATION]!!),
                    mimeType = cursor.getString(indices[MIME_TYPE]!!)
            )
        }
    }
}