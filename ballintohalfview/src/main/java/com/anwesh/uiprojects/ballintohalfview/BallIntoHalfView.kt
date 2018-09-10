package com.anwesh.uiprojects.ballintohalfview

/**
 * Created by anweshmishra on 10/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5

fun Canvas.drawBIHNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val r : Float = gap / 3
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val arcR : Float = r * sc1
    paint.strokeWidth = Math.min(w, h) / 90
    paint.style = Paint.Style.FILL_AND_STROKE
    paint.color = Color.parseColor("#283593")
    save()
    translate(w/2, gap + i * gap)
    for (j in 0..1) {
        val sf : Float = 1f - 2 * (j % 2)
        save()
        translate((w/2 - r) * sf * sc2, 0f)
        rotate(180f * sc2)
        drawArc(RectF(-arcR, -arcR, arcR, arcR), 90f * sf, 180f,true, paint)
        restore()
    }
    restore()
}

class BallIntoHalfView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }
    }

    data class BIHNode(var i : Int, val state : State = State()) {

        private var prev : BIHNode? = null
        private var next : BIHNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BIHNode(i + 1)
                next?.prev = this
            }
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBIHNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BIHNode {
            var curr : BIHNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BallIntoHalf(var i : Int) {
        private var curr : BIHNode = BIHNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }
    }

    data class Renderer(var view : BallIntoHalfView) {
        private val animator : Animator = Animator(view)
        private val bih : BallIntoHalf = BallIntoHalf(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            bih.draw(canvas, paint)
            animator.animate {
                bih.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bih.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : BallIntoHalfView {
            val view : BallIntoHalfView = BallIntoHalfView(activity)
            activity.setContentView(view)
            return view
        }
    }
}