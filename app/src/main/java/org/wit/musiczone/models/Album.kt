package org.wit.musiczone.models

data class Album(
    val id: Int,
    val title: String,
    val artist: String,
    val albumArtResId: Int = 0 // Drawable resource ID for album art
)

