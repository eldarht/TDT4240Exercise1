package com.example.exercise1

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.text.format.Time
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.random.Random

class Helicopter constructor(ctx: Context, var mode: Mode, private val txtPosition : TextView, val displayDimension : Point) : ImageView(ctx)  {
    var speed : Float = 250.0f // pixels/second
    var direction : Pair<Float, Float> = Pair(1.0f, 1.0f)

    var dt : Float = 0.0f       //delta time
    var lastFrame : Long = 0

    init{
        this.setImageResource(R.drawable.attackhelicopter)
        this.layoutParams = LinearLayout.LayoutParams(130, 52)

        this.translationX = Random.nextInt(0, this.displayDimension.x).toFloat()
        this.translationY = Random.nextInt(0, this.displayDimension.y).toFloat()
        this.setSpriteDirection()

        this.lastFrame = System.nanoTime()

        this.startSpriteAnimation()
    }

    private fun startSpriteAnimation() {
        ValueAnimator.ofInt(0, 4).apply{
            duration = 400
            repeatMode = ValueAnimator.RESTART

            addUpdateListener {
                when(it.animatedValue){
                    1 -> this@Helicopter.setImageResource(R.drawable.heli1)
                    2 -> this@Helicopter.setImageResource(R.drawable.heli2)
                    3 -> this@Helicopter.setImageResource(R.drawable.heli3)
                    4 -> this@Helicopter.setImageResource(R.drawable.heli4)
                }
                Log.println(Log.DEBUG, "ANIMATION", "New imageResource ${this@Helicopter.drawable.constantState}")
            }

            start()
        }
    }

    private fun setSpriteDirection() {
        if(this.direction.first <= 0.0f){
            this.scaleX = 1.0f
        }else{
            this.scaleX = -1.0f
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val currentFrame : Long = System.nanoTime()
        dt = (currentFrame - this.lastFrame) / 1000000000.0f
        this.lastFrame = currentFrame

        when(this.mode){
            Mode.FREE -> this.updateFree()
            Mode.CONTROL -> this.updateControl(canvas)
            Mode.COLLISION -> this.updateCollision(canvas)
        }

    }

    private fun updateCollision(canvas: Canvas?) {

        this.updatePosition()
        this.checkWallCollision()
        invalidate()
        //Collision is handled by the main activity as it needs access to the other helicopters
    }

    private fun updatePosition() {
        this.translationX += this.speed * this.dt * this.direction.first
        this.translationY += this.speed * this.dt * this.direction.second
    }

    private fun updateControl(canvas: Canvas?) {
        invalidate()
        this.checkWallCollision()
    }

    private fun updateFree() {

        this.updatePosition()
        this.checkWallCollision()
        invalidate()
    }

    private fun forceDraw() {
        if(this.left == 0){ // Dirty hack to force new draw
            this.left = 1
        }else{
            this.left = 0
        }
    }

    private fun checkWallCollision() {
        // Check bottom (as it is drawn from the top)
        if (this.displayDimension.y < this.translationY) {
            this.direction = Pair(this.direction.first, 0 - this.direction.second.absoluteValue)
        }

        // Check top
        if (this.height + this.translationY < 0) {
            this.direction = Pair(this.direction.first, this.direction.second.absoluteValue)
        }

        // Check right
        if (this.displayDimension.x < this.width + this.translationX) {
            this.direction = Pair(0 - this.direction.first.absoluteValue, this.direction.second)
            this.setSpriteDirection()
        }

        // Check left
        if (this.translationX < 0) {
            this.direction = Pair(this.direction.first.absoluteValue, this.direction.second)
            this.setSpriteDirection()
        }

        this.updatePositionText()
    }

    fun updateMode(mode: Mode){
        this.mode = mode

        if (mode == Mode.CONTROL){
            Log.println(Log.DEBUG, "CONTROL", "Setting drag handler")

            this.setOnTouchListener { v, event ->
                Log.println(Log.DEBUG, "CONTROL", "drag position is: " + event.x + ", " + event.y)
                v.translationX += event.x
                v.translationY += event.y

                if(v.translationX > displayDimension.x){
                    v.translationX = displayDimension.x.toFloat()
                }
                if(v.translationX < 0){
                    v.translationX = 0f
                }

                if(v.translationY > displayDimension.y){
                    v.translationY = displayDimension.y.toFloat()
                }
                if(v.translationY < 0){
                    v.translationY = 0f
                }
                true
            }
        }else{
            this.setOnDragListener(null)
        }
    }
    private fun updatePositionText() {
        txtPosition.text = "(" + this.translationX + ", " + this.translationY + ")"

    }

    fun collideWith(other: Rect) {
        // TODO Fix error where bounce in the wrong direction due to scale= -1 for flipping image
        // Check bottom (as it is drawn from the top)
        var hostRect = Rect()

        this.getDrawingRect(hostRect)
        hostRect.left += this.translationX.toInt()
        hostRect.right += this.translationX.toInt()

        hostRect.top += this.translationY.toInt()
        hostRect.bottom += this.translationY.toInt()

        if (hostRect.left < other.left) {
            if (hostRect.right > other.left){
                this.direction = Pair(this.direction.first.absoluteValue, this.direction.second)
            }
        }else{
            if(hostRect.right > other.right)
                this.direction = Pair(0 - this.direction.first.absoluteValue, this.direction.second)
        }

        if (hostRect.top < other.top) {
            if (hostRect.bottom > other.top){
                this.direction = Pair(this.direction.first, this.direction.second.absoluteValue)
            }
        }else{
            if(hostRect.bottom > other.bottom)
                this.direction = Pair(this.direction.first, 0 - this.direction.second.absoluteValue)
        }

    }

    enum class Mode{
        FREE, CONTROL, COLLISION
    }
}