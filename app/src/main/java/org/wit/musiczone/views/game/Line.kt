package org.wit.musiczone.views.game

class Line(screenX: Int, screenY: Int) {

    var x: Int = 0
    var y: Float = 0f // Use Float for smooth, sub-pixel movement
    val width: Int
    val height: Int
    var isWhite = false     // Tracks if the tile has been tapped.
    var isMissed = false    // Tracks if the tile was missed.
    var isActive = false    // Flag for object pooling, true if the tile is currently on screen.

    init {
        width = screenX / 4
        height = screenY / 4
    }

    // Update position based on a float value for smoothness
    fun update(pixelsToMove: Float) {
        if (isActive) {
            y += pixelsToMove
        }
    }

    // Resets the tile to a new state to be reused
    fun reset(yPos: Float, blackLineIndex: Int) { // yPos is now a Float
        this.y = yPos
        this.x = blackLineIndex * width
        this.isWhite = false
        this.isMissed = false
        this.isActive = true
    }

    fun isTouched(touchX: Float, touchY: Float): Boolean {
        // Only interact with active tiles. y is now a float, comparison is fine.
        return isActive && touchX > x && touchX < x + width && touchY > y && touchY < y + height
    }
}
