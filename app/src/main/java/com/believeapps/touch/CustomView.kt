package com.believeapps.touch

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.view.ViewGroup
import android.util.DisplayMetrics
import java.lang.Math.abs


class CustomView : FrameLayout {

    private lateinit var mDragHelper: ViewDragHelper

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        mDragHelper = ViewDragHelper.create(this, 1.0F, getDragHelperCallback())
        val count = 2
        val size = calculateWidth() / count

        addViews(size, count)
    }

    private fun addViews(size: Int, count: Int) {
        for (i in 0 until count) {
            for (j in 0 until count) {
                if (i == count - 1 && j == count - 1) return
                val view = View(context).apply {
                    layoutParams = LayoutParams(size, size)
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    (layoutParams as MarginLayoutParams).leftMargin = (j * size)
                    (layoutParams as MarginLayoutParams).topMargin = (i * size)
                }

                addView(view)
            }
        }
    }

    private fun calculateWidth(): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getDragHelperCallback(): ViewDragHelper.Callback {
        return object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return true
            }

            override fun getViewHorizontalDragRange(child: View): Int {
                return child.width
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                val newPosition = when {
                    dx < -1 -> left + (abs(dx) -1)
                    dx > 1 -> left - (dx - 1)
                    else -> left
                }
                Log.d("clampX", "${left}  ${dx}  ${newPosition}")
                return when {
                    newPosition < 0 -> 0
                    newPosition + child.width > width -> width - child.width
                    checkIfCollidesHorizonal(this@CustomView, child, dx < 0, newPosition) -> {
                        if(dx < 0) newPosition + 1 else newPosition - 1
                    }
                    else -> left
                }
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                val newPosition = when {
                    dy < -1 -> top + (abs(dy) -1)
                    dy > 1 -> top - (dy - 1)
                    else -> top
                }
                Log.d("clampY", "${top} + ${dy} + ${newPosition}")
                return when {
                    newPosition < 0 -> 0
                    newPosition + child.height > height -> height - child.height
                    checkIfCollidesVertivaly(this@CustomView, child, dy > 0, newPosition) -> child.y.toInt()
                    else -> top
                }
            }
        }
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mDragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDragHelper.processTouchEvent(event)
        return true
    }

    private fun checkIfCollidesHorizonal(viewGroup: ViewGroup, view: View, leftCollision: Boolean, left: Int): Boolean {
        val x: Int = if (leftCollision) left else view.right
        val topY: Int = view.top
        val lowY: Int = view.bottom
        val centerY: Int = (topY + lowY) / 2
        return executeOnEveryHitRect(viewGroup, view) {
            contains(it, x, topY) || contains(it, x, lowY) || contains(it, x, centerY)
        }
    }

    private fun checkIfCollidesVertivaly(viewGroup: ViewGroup, view: View, topCollision: Boolean, top: Int): Boolean {
        val y: Int = if (topCollision) view.bottom else top
        val leftX: Int = view.left
        val rightX: Int = view.right
        val centerX: Int = (leftX + rightX) / 2
        return executeOnEveryHitRect(viewGroup, view) {
            contains(it, leftX, y) || contains(it, rightX, y) || contains(it, centerX, y)
        }
    }

    private fun executeOnEveryHitRect(viewGroup: ViewGroup,
                                      view: View,
                                      checkIfContains: (Rect) -> Boolean): Boolean {
        (0 until viewGroup.childCount)
                .map { viewGroup.getChildAt(it) }
                .filter { it != view }
                .forEach {
                    val bounds = Rect()

                    it.getHitRect(bounds)
                    if (checkIfContains(bounds)) return true
                }
        return false
    }

    private fun contains(rect: Rect, x: Int, y: Int): Boolean {
        with(rect) {
            return (left < right && top < bottom  // check for empty first

                    && x > left && x < right && y > top && y < bottom)
        }
    }

}
