package org.wit.musiczone.models

data class Playlist(
    val id: Int,
    val name: String,
    val songCount: Int = 0,
    val imageResId: Int = 0 // Drawable resource ID for playlist image
)

