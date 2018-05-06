package com.believeapps.touch

import android.content.Context
import android.graphics.Rect
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.view.ViewGroup


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
    }

    private fun getDragHelperCallback(): ViewDragHelper.Callback {
        return object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return true
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                Log.d("clampY", "${left} + ${dx} + ${width} + ${height}")
                return when {
                    left < 0 -> 0
                    left + child.width > width -> width - child.width
                    checkIfCollidesHorizonal(this@CustomView, child, dx < 0) -> child.x.toInt() - 1
                    else -> left
                }
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return when {
                    top < 0 -> 0
                    top + child.height > height -> height - child.height
                    checkIfCollidesVertivaly(this@CustomView, child, dy > 0) -> child.y.toInt() - 1
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

    private fun checkIfCollidesHorizonal(viewGroup: ViewGroup, view: View, leftCollision: Boolean): Boolean {
        val x: Int = if (leftCollision) view.left else view.right
        val topY: Int = view.top
        val lowY: Int = view.bottom
        return executeOnEveryHitRect(viewGroup, view) { it.contains(x, topY) || it.contains(x, lowY) }
    }

    private fun checkIfCollidesVertivaly(viewGroup: ViewGroup, view: View, topCollision: Boolean): Boolean {
        val y = if (topCollision) view.bottom else view.top
        val leftX = view.left
        val rightX = view.right
        return executeOnEveryHitRect(viewGroup, view) { it.contains(leftX, y) || it.contains(rightX, y) }
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

}
