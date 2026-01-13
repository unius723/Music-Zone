package org.wit.musiczone.activities

import android.content.Intent

interface StyleContract {

    interface View {
        fun setBackground(drawableResId: Int)
        fun playVideo(videoResId: Int)
        fun adjustVideoPosition(horizontalBias: Float)
        fun navigateTo(intent: Intent)
        fun finishActivity()
    }

    interface Presenter {
        fun onViewCreated(intent: Intent)
        fun onBackClicked()
        fun onSearchClicked()
        fun onChangeStyleClicked()
        fun onDisplayRoomClicked()
        fun onRecommendClicked()
        fun onMapClicked()
        fun onListeningClicked()
        fun onGameClicked()
        fun onReportClicked()
        fun onExitTouched(touchX: Float, viewWidth: Int)
        fun onDestroy() // Only onDestroy is needed for cleanup
    }
}
