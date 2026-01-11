package org.wit.musiczone.data.location

data class SavedLocation(
    val province: String,
    val city: String,
    val district: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val time: Long
)
