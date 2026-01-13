package org.wit.musiczone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.wit.musiczone.models.StyleRepository

class StylePresenter(private var view: StyleContract.View?) : StyleContract.Presenter {

    private val model: StyleRepository = StyleRepository()

    override fun onViewCreated(intent: Intent) {
        val style = intent.getStringExtra("style") ?: "Rock"

        val bgRes = model.getBackgroundForStyle(style)
        val videoRes = model.getVideoForStyle(style)
        val horizontalBias = model.getHorizontalBiasForStyle(style)

        view?.setBackground(bgRes)
        view?.playVideo(videoRes)
        view?.adjustVideoPosition(horizontalBias)
    }

    override fun onBackClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, ChooseStyleActivity::class.java))
        view?.finishActivity()
    }

    override fun onSearchClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, SearchActivity::class.java))
    }

    override fun onChangeStyleClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, ChooseStyleActivity::class.java))
        view?.finishActivity()
    }

    override fun onDisplayRoomClicked() {
        // Not implemented
    }

    override fun onRecommendClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, MusicRecommendationActivity::class.java))
    }

    override fun onMapClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, AmapLocationActivity::class.java))
    }

    override fun onListeningClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, MusicPlayerActivity::class.java))
    }

    override fun onGameClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, StartGameActivity::class.java))
    }

    override fun onReportClicked() {
        view?.navigateTo(Intent(view as? AppCompatActivity, FeedbackActivity::class.java))
    }

    override fun onExitTouched(touchX: Float, viewWidth: Int) {
        if (touchX < viewWidth.toFloat() / 2) {
            view?.navigateTo(Intent(view as? AppCompatActivity, AppInformationActivity::class.java))
        } else {
            view?.navigateTo(Intent(view as? AppCompatActivity, StartActivity::class.java))
            view?.finishActivity()
        }
    }

    override fun onDestroy() {
        view = null // Avoid memory leaks
    }
}