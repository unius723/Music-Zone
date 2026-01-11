package org.wit.musiczone.data.location

import android.content.Context
import com.amap.api.location.AMapLocation
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import com.google.firebase.firestore.FirebaseFirestore


private const val KEY_LIT_CITIES = "lit_cities"
private const val KEY_LIT_PROVINCES = "lit_provinces"
class LocationRepository(private val context: Context) {

    private val sp = context.getSharedPreferences("musiczone_location", Context.MODE_PRIVATE)

    private val firestore = FirebaseFirestore.getInstance()

    fun saveLocation(location: AMapLocation) {
        sp.edit()
            .putString("province", location.province)
            .putString("city", location.city)
            .putString("district", location.district)
            .putString("address", location.address)
            .putFloat("latitude", location.latitude.toFloat())
            .putFloat("longitude", location.longitude.toFloat())
            .putLong("time", System.currentTimeMillis())
            .apply()
    }

    fun getLastLocation(): SavedLocation? {
        val province = sp.getString("province", null) ?: return null
        return SavedLocation(
            province = province,
            city = sp.getString("city", "") ?: "",
            district = sp.getString("district", "") ?: "",
            address = sp.getString("address", "") ?: "",
            latitude = sp.getFloat("latitude", 0f).toDouble(),
            longitude = sp.getFloat("longitude", 0f).toDouble(),
            time = sp.getLong("time", 0L)
        )
    }

    fun addLitCity(city: String?) {
        if (city.isNullOrBlank()) return
        val set = sp.getStringSet(KEY_LIT_CITIES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(city)
        sp.edit().putStringSet(KEY_LIT_CITIES, set).apply()
    }

    fun addLitProvince(province: String?) {
        if (province.isNullOrBlank()) return

        val set = sp.getStringSet(KEY_LIT_PROVINCES, mutableSetOf())
            ?.toMutableSet() ?: mutableSetOf()
        set.add(province)
        sp.edit()
            .putStringSet(KEY_LIT_PROVINCES, set)
            .apply()
    }

    fun getLitCities(): Set<String> {
        return sp.getStringSet(KEY_LIT_CITIES, emptySet()) ?: emptySet()
    }

    fun getLitProvinces(): Set<String> {
        return sp.getStringSet(KEY_LIT_PROVINCES, emptySet()) ?: emptySet()
    }

    fun saveSavedLocationToFirebase(
        userId: String,
        location: SavedLocation
    ) {
        val data = hashMapOf(
            "province" to location.province,
            "city" to location.city,
            "district" to location.district,
            "address" to location.address,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "time" to location.time
        )

        firestore.collection("users")
            .document(userId)
            .collection("locations")
            .add(data)
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}



