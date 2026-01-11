package org.wit.musiczone.activities
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.wit.musiczone.R
import org.wit.musiczone.data.ProvincePoint
import org.wit.musiczone.data.provincePointMap
import org.wit.musiczone.data.location.LocationRepository
import timber.log.Timber

class MusicMapActivity : AppCompatActivity() {
    private lateinit var gifBg: ImageView
    private lateinit var mapImage: ImageView
    private lateinit var locationText: TextView
    private lateinit var litCountText: TextView
    private lateinit var characterView: ImageView
    private val provinceViews = mutableMapOf<String, ImageView>()
    private lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_map)

        gifBg = findViewById(R.id.gifBg)
        mapImage = findViewById(R.id.musiczone_map)
        locationText = findViewById(R.id.locationText)
        litCountText = findViewById(R.id.litCountText)
        characterView = findViewById(R.id.characterView)

        val backButton = findViewById<ImageButton>(R.id.icon_back)

        Glide.with(this)
            .asGif()
            .load(R.drawable.musiczone_daybg)
            .into(gifBg)

        locationRepository = LocationRepository(this)

        initProvinceViews()

        mapImage.post {
            updateAllProvincePositions()
            showAllLitProvinces()
        }

        val savedLocation = locationRepository.getLastLocation()
        if (savedLocation != null) {
            Timber.i("Location：${savedLocation.province} ${savedLocation.city}")

            locationText.text = "YOU ARE IN\n  ${savedLocation.province}  ${savedLocation.city}"

            locationRepository.addLitCity(savedLocation.city)

            locationRepository.addLitProvince(savedLocation.province)

            moveCharacterToProvince(savedLocation.province)

            showProvinceMarker(savedLocation.province)

            updateLitCount()
        }
        else {
            locationText.text = "LOCATION UNKNOWN"
            Toast.makeText(this, "No Location Data.", Toast.LENGTH_SHORT).show()
            Timber.e("MusicMapActivity：Can't read SavedLocation")
        }

        backButton.setOnClickListener {
            finish() //
        }
    }

    private fun updateLitCount() {
        val count = locationRepository.getLitCities().size

        if (count == 0) {
            litCountText.text = getString(R.string.Location_cities)
            return
        }

        val cityText = if (count == 1) "City" else "Cities"
        val text = "Your Ears Have Been Traveled $count $cityText!"

        val spannable = android.text.SpannableString(text)
        val start = text.indexOf(count.toString())
        val end = start + count.toString().length
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(getColor(R.color.light_blue)),
            start,
            end,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        litCountText.text = spannable
    }



    private fun showProvinceMarker(provinceName: String) {
        provinceViews[provinceName]?.visibility = View.VISIBLE
    }


    private fun initProvinceViews() {
        provincePointMap.keys.forEach { province ->
            val resId = resources.getIdentifier(province, "id", packageName)
            val view = findViewById<ImageView?>(resId)
            view?.let { provinceViews[province] = it
                it.visibility = View.INVISIBLE}
        }
    }

    private fun updateAllProvincePositions() {
        val mapLeft = mapImage.x
        val mapTop = mapImage.y
        val mapWidth = mapImage.width.toFloat()
        val mapHeight = mapImage.height.toFloat()

        provincePointMap.forEach { (province, point: ProvincePoint) ->
            provinceViews[province]?.let { view ->
                val x = (mapWidth * point.xPercent).coerceIn(0f, mapWidth - view.width)
                val y = (mapHeight * point.yPercent).coerceIn(0f, mapHeight - view.height)
                view.x = mapLeft + x
                view.y = mapTop + y
            }
        }
    }
//    private fun showAllLitProvinces() {
//        val litProvinces = locationRepository.getLitProvinces()
//
//        litProvinces.forEach { province ->
//            provinceViews[province]?.visibility = View.VISIBLE
//        }
//    }

    private fun showAllLitProvinces() {
        val litProvinces = locationRepository.getLitProvinces()

        Timber.d("Province List：$litProvinces")

        litProvinces.forEach { province ->
            if (provinceViews.containsKey(province)) {
                Timber.i("Show Provinces：$province")
                provinceViews[province]?.visibility = View.VISIBLE
            } else {
                Timber.e("UI doesn't show the Province but in the list：$province")
            }
        }
    }

    private val Int.dp: Float
        get() = this * resources.displayMetrics.density

    private fun moveCharacterToProvince(province: String) {
        val provinceView = provinceViews[province] ?: return

        provinceView.post {

            val offsetX = characterView.width - 6.dp
            val offsetY = provinceView.height / 2 - characterView.height / 2

            characterView.x = provinceView.x - offsetX
            characterView.y = provinceView.y + offsetY

            characterView.visibility = View.VISIBLE
        }
    }
}
