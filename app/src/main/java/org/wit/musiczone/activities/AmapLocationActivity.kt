package org.wit.musiczone.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.MarkerOptions
import org.wit.musiczone.R
import org.wit.musiczone.data.location.LocationRepository
import timber.log.Timber
import android.os.Handler
import android.os.Looper


class AmapLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private var locationClient: AMapLocationClient? = null
    private lateinit var locationInfoText: TextView
    private lateinit var locationRepository: LocationRepository
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_amap_location)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map

        locationInfoText = findViewById(R.id.locationInfoText)

        locationRepository = LocationRepository(this)

        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        if (checkLocationPermission()) {
            initLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initLocation()
        }
    }

    private fun initLocation() {
        try {
            locationClient = AMapLocationClient(this)

            val option = AMapLocationClientOption().apply {
                isOnceLocation = true
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isNeedAddress = true
            }

            locationClient?.setLocationOption(option)

            locationClient?.setLocationListener { location ->

                if (location != null && location.errorCode == 0) {

                    locationRepository.saveLocation(location)

//                    locationRepository.exportProvinceCityToJson()

                    val latLng = LatLng(location.latitude, location.longitude)

                    runOnUiThread {

                        aMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 18f)
                        )

                        aMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(location.city ?: "Current Location")
                        )

                        locationInfoText.text =
                                    "Province: ${location.province}\n" +
                                    "City: ${location.city}\n" +
                                    "District: ${location.district}\n" +
                                    "Address: ${location.address}\n" +
                                    "Coordinates: ${location.latitude}, ${location.longitude}"

                        Timber.i("Location saved: ${location.province}, ${location.city}")


                        Toast.makeText(
                            this,
                            "Location detected. Opening Music Map…",
                            Toast.LENGTH_SHORT
                        ).show()


                        Handler(Looper.getMainLooper()).postDelayed({

                            val intent = Intent(this, MusicMapActivity::class.java)
                            startActivity(intent)
                            finish()

                        }, 3000)
                    }

                } else {
                    runOnUiThread {
                        locationInfoText.text =
                            "Locating Failed：${location?.errorInfo}"

                        Toast.makeText(
                            this,
                            "Locating failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            locationClient?.startLocation()

        } catch (e: Exception) {
            Timber.e(e, "Location Initialization Failed")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationClient?.stopLocation()
        locationClient?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
