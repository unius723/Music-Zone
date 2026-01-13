package org.wit.musiczone.models

import org.wit.musiczone.R

class StyleRepository {

    fun getBackgroundForStyle(style: String): Int {
        return when (style) {
            "Rock" -> R.drawable.style_rock
            "Classic" -> R.drawable.style_classic
            "Electronic" -> R.drawable.style_electronic
            "Chinese" -> R.drawable.style_chinese
            else -> R.drawable.style_rock
        }
    }

    fun getVideoForStyle(style: String): Int {
        return when (style) {
            "Rock" -> R.raw.rock
            "Classic" -> R.raw.classic
            "Electronic" -> R.raw.electronic
            "Chinese" -> R.raw.chinese
            else -> R.raw.rock
        }
    }

    fun getHorizontalBiasForStyle(style: String): Float {
        return when (style) {
            "Classic", "Chinese" -> 0.35f
            "Electronic" -> 0.65f
            else -> 0.5f // Default for Rock
        }
    }
}