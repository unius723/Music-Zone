package org.wit.musiczone.views.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceView
import org.wit.musiczone.R
import org.wit.musiczone.activities.GameActivity
import kotlin.random.Random

class GameView(context: Context, private val screenX: Int, private val screenY: Int) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var isPlaying = false
    private var isPaused = false

    private var scorePaint: Paint = Paint()

    private val linePool: ArrayList<Line> = ArrayList()
    private val maxLines = 12

    private var score: Int = 0
    private val activity: GameActivity = context as GameActivity

    private var backgroundMovie: Movie? = null
    private var movieStart: Long = 0

    private var defaultTileBitmap: Bitmap
    private var touchedTileBitmap: Bitmap
    private var missedTileBitmap: Bitmap

    private val targetFPS = 60
    private var lastFrameTime: Long = 0
    private val baseSpeedPixelsPerSecond: Float = screenY * 0.4f

    init {
        scorePaint.color = Color.BLACK
        scorePaint.textSize = 64f
        scorePaint.textAlign = Paint.Align.RIGHT

        val inputStream = context.resources.openRawResource(R.drawable.musiczone_daybg)
        backgroundMovie = Movie.decodeStream(inputStream)

        val tileWidth = screenX / 4
        val tileHeight = screenY / 4
        defaultTileBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.icon_listeninglist), tileWidth, tileHeight, false)
        touchedTileBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.icon_tiles_true), tileWidth, tileHeight, false)
        missedTileBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.icon_tiles_false), tileWidth, tileHeight, false)

        for (i in 0 until maxLines) {
            linePool.add(Line(screenX, screenY))
        }
    }

    override fun run() {
        lastFrameTime = System.nanoTime()
        val targetTimeNanos = (1000000000 / targetFPS).toLong()

        while (isPlaying) {
            val now = System.nanoTime()
            val deltaTime = (now - lastFrameTime) / 1000000000.0f
            lastFrameTime = now

            if (!isPaused) {
                update(deltaTime)
            }
            draw()

            val cycleTime = (System.nanoTime() - now) / 1000000
            val sleepTime = targetTimeNanos / 1000000 - cycleTime

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {}
            }
        }
        
        activity.runOnUiThread {
            if (!activity.isFinishing) {
                activity.gameOver(score)
            }
        }
    }

    private fun update(deltaTime: Float) {
        val currentSpeed = baseSpeedPixelsPerSecond + (score * 10)
        val pixelsToMove = currentSpeed * deltaTime

        var topmostY = screenY.toFloat()
        var hasActiveLine = false

        for (line in linePool) {
            if (line.isActive) {
                hasActiveLine = true
                line.update(pixelsToMove)

                if (line.y > screenY) {
                    if (!line.isWhite) {
                        line.isMissed = true
                        isPlaying = false
                        return
                    } else {
                        line.isActive = false
                    }
                }

                if (line.isActive && line.y < topmostY) {
                    topmostY = line.y
                }
            }
        }

        if (!hasActiveLine || (topmostY > 0 && topmostY < screenY)) {
            val line = getInactiveLine()
            line?.reset(-line.height.toFloat(), Random.nextInt(4))
        }
    }

    private fun getInactiveLine(): Line? {
        return linePool.firstOrNull { !it.isActive }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas() ?: return
            
            val now = android.os.SystemClock.uptimeMillis()
            if (movieStart == 0L) {
                movieStart = now
            }
            if (backgroundMovie != null) {
                val dur = if (backgroundMovie!!.duration() == 0) 1000 else backgroundMovie!!.duration()
                val relTime = ((now - movieStart) % dur).toInt()
                backgroundMovie!!.setTime(relTime)
                canvas.save()
                val scale = Math.max(screenX.toFloat() / backgroundMovie!!.width(), screenY.toFloat() / backgroundMovie!!.height())
                canvas.scale(scale, scale)
                backgroundMovie!!.draw(canvas, 0f, 0f)
                canvas.restore()
            } else {
                canvas.drawColor(Color.WHITE)
            }

            for (line in linePool) {
                if (line.isActive) {
                    val bitmapToDraw = when {
                        line.isMissed -> missedTileBitmap
                        line.isWhite -> touchedTileBitmap
                        else -> defaultTileBitmap
                    }
                    canvas.drawBitmap(bitmapToDraw, line.x.toFloat(), line.y, null)
                }
            }
            canvas.drawText("Score: $score", (screenX - 50).toFloat(), 100f, scorePaint)

            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun resume() {
        if (!isPlaying && thread == null) {
            isPlaying = true
            isPaused = false
            thread = Thread(this).apply { start() }
        } else if (isPaused) {
            isPaused = false
            lastFrameTime = System.nanoTime()
        }
    }

    fun pause() {
        isPaused = true
    }

    fun stop() {
        isPlaying = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {}
        thread = null
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (isPaused) return true

            // Find the tile that should be tapped (lowest active, untouched tile)
            val targetTile = linePool
                .filter { it.isActive && !it.isWhite }
                .maxByOrNull { it.y }

            // If there's no target, a tap in an empty area is a game over.
            if (targetTile == null) {
                isPlaying = false
                return true
            }

            // Check if the tap hit the correct target tile
            if (targetTile.isTouched(event.x, event.y)) {
                // Correct tap
                score++
                targetTile.isWhite = true
            } else {
                // Incorrect tap (missed the target or hit the wrong area)
                isPlaying = false
                targetTile.isMissed = true // Mark the tile that should have been tapped as missed
            }
        }
        return true
    }
}