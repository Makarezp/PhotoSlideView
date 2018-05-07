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
        val count = 4
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
                Log.d("clampX", "${left}  ${dx}")
                return when {
                    left < 0 -> 0
                    left + child.width > width -> width - child.width
                    checkIfCollidesHorizonal(this@CustomView, child, dx < 0, left) -> child.x.toInt()
                    else -> left
                }
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                Log.d("clampY", "${top} + ${dy}")
                return when {
                    top < 0 -> 0
                    top + child.height > height -> height - child.height
                    checkIfCollidesVertivaly(this@CustomView, child, dy > 0, top) -> child.y.toInt()
                    else -> top
                }
            }

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                Log.d("clampChanged", "${left} ${top} ${dx} ${dy}")
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
        Log.d("clampHotizontalCheck", "${leftCollision}")
        val x: Int = if (leftCollision) left else left + view.width
        val topY: Int = view.top
        val lowY: Int = view.bottom
        val centerY: Int = (topY + lowY) / 2
        return executeOnEveryHitRect(viewGroup, view) {
            contains(it, x, topY) || contains(it, x, lowY) || contains(it, x, centerY)
        }
    }

    private fun checkIfCollidesVertivaly(viewGroup: ViewGroup, view: View, topCollision: Boolean, top: Int): Boolean {
        Log.d("clampVertical                                                                                   Check", "${topCollision}")
        val y: Int = if (topCollision) top + view.height else top
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

                    && x > left && x < right && y > top && y < bottom )
        }
    }

}
