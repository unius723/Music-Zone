package org.wit.musiczone.models

data class Artist(
    val id: Int,
    val name: String,
    val imageResId: Int = 0 // Drawable resource ID for artist image
)

