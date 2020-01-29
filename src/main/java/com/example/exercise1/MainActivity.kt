package com.example.exercise1

import android.graphics.Point
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout



class MainActivity : AppCompatActivity() {

    private var helicopters : Array<Helicopter> = emptyArray()
    private val maxHelicopterCount : Int = 5
    private val collisionChecker : Thread = Thread(Runnable{
        while (true){
            helicopters.forEachIndexed{ index, helicopter ->
                var hostRect = Rect()
                helicopter.getDrawingRect(hostRect)
                hostRect.left += helicopter.translationX.toInt()
                hostRect.right += helicopter.translationX.toInt()

                hostRect.top += helicopter.translationY.toInt()
                hostRect.bottom += helicopter.translationY.toInt()

                for(i in index+1 until maxHelicopterCount){
                    var targetRect = Rect()
                    helicopters[i].getDrawingRect(targetRect)
                    targetRect.left += helicopters[i].translationX.toInt()
                    targetRect.right += helicopters[i].translationX.toInt()

                    targetRect.top += helicopters[i].translationY.toInt()
                    targetRect.bottom += helicopters[i].translationY.toInt()

                    if(hostRect.intersect(targetRect)){
                        helicopter.collideWith(targetRect)
                        helicopters[i].collideWith(hostRect)
                    }
                }
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var displayDimension : Point = Point(0,0)
        windowManager.defaultDisplay.getSize(displayDimension)

        val linearLayout = findViewById<ConstraintLayout>(R.id.root)

        this.helicopters = Array(this.maxHelicopterCount) {
            Helicopter(this, Helicopter.Mode.FREE, findViewById(R.id.txtPosition), displayDimension)
        }

        for (i in 0 until maxHelicopterCount){
            // Add ImageView to LinearLayout
            linearLayout?.addView(helicopters[i])
        }

        //Default mode is free
        setFree()

    }

    fun onModeChange(view: View) {

        when(view.id){
            R.id.modeFree -> setFree()
            R.id.modeManual -> setControl()
            R.id.modeMultiple -> setCollision()
        }
    }

    private fun setCollision() {

        helicopters.forEach {helicopter ->
            helicopter.visibility = View.VISIBLE
            helicopter.updateMode(Helicopter.Mode.COLLISION)
        }
        //TODO Find a good way to restart the thread
       collisionChecker.start()
    }

    private fun setControl() {
        helicopters.forEachIndexed{ index, helicopter ->
            if(index != 0){
                helicopter.visibility = View.GONE
            }else{
                helicopter.updateMode(Helicopter.Mode.CONTROL)
            }
        }
        if(collisionChecker.isAlive){
            collisionChecker.interrupt()
        }
    }

    private fun setFree() {

        helicopters.forEachIndexed{ index, helicopter ->
            if(index != 0){
                helicopter.visibility = View.GONE
            }else{
                helicopter.updateMode(Helicopter.Mode.FREE)
            }
        }

        if(collisionChecker.isAlive){
            collisionChecker.interrupt()
        }
    }

}
