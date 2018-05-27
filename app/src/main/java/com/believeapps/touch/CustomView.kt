package com.believeapps.touch

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import androidx.customview.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.view.ViewGroup
import android.util.DisplayMetrics
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.scale
import androidx.core.view.children


class CustomView : FrameLayout {

    private lateinit var dragHelper: ViewDragHelper

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
        dragHelper = ViewDragHelper.create(this, 1.0F, getDragHelperCallback())
    }

    fun setImage(bitmap: Bitmap) {
        removeAllViews()
        val count = 3
        val size = calculateWidth() / count
        setBitmap(size, count, bitmap)
    }

    private fun setBitmap(size: Int, count: Int, bitmap: Bitmap) {
        val viewList = createListOfPieces(size, count, bitmap)
        viewList.shuffle()
        var indx = 0
        for (i in 0 until count) {
            for (j in 0 until count) {
                if (i == count - 1 && j == count - 1) break
                addView(viewList[indx].apply {
                    (layoutParams as MarginLayoutParams).leftMargin = (j * size)
                    (layoutParams as MarginLayoutParams).topMargin = (i * size)
                })
                indx++
            }
        }
    }

    private fun createListOfPieces(size: Int, count: Int, bitmap: Bitmap): ArrayList<View> {
        val rescaledBitmap = bitmap.scale(size * count, size * count)
        val viewList = arrayListOf<View>()
        for (i in 0 until count) {
            for (j in 0 until count) {
                if (i == count - 1 && j == count - 1) break
                val view = ImageView(context).apply {
                    layoutParams = LayoutParams(size, size)
                    setImageBitmap(Bitmap.createBitmap(rescaledBitmap, j * size, i * size, size, size))
                }
                viewList.add(view)
            }
        }
        return viewList
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
                val isLeftCollision = dx < 0
                val snapValue = child.width / 2
                return when {
                    left < 0 + snapValue && isLeftCollision -> 0
                    left + child.width > width - snapValue && !isLeftCollision -> width - child.width
                    checkIfCollidesHorizonal(this@CustomView, child, isLeftCollision, left) -> child.x.toInt()
                    checkIfShouldSnapHorizontally(this@CustomView, child, isLeftCollision, left, snapValue) -> {

                        val rect = getRectOfView(this@CustomView, child) {
                            it.contains(if (isLeftCollision) {
                                child.left - snapValue
                            } else {
                                child.right + snapValue
                            }, child.top)
                        }

                        return if (rect != null) {
                            return if (isLeftCollision) {
                                rect.right
                            } else rect.left - child.width
                        } else child.x.toInt() + (if (isLeftCollision) -snapValue else snapValue)

                    }
                    else -> left
                }
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                Log.d("clampY", "${top} + ${dy}")
                val isBottomCollision = dy > 0
                val snapValue = child.height / 2
                return when {
                    top < 0 + snapValue && !isBottomCollision -> 0
                    top + child.height > height - snapValue && isBottomCollision -> height - child.height
                    checkIfCollidesVerticaly(this@CustomView, child, isBottomCollision, top) -> child.y.toInt()
                    checkIfShouldSnapVertically(this@CustomView, child, isBottomCollision, top, snapValue) -> {

                        val rect = getRectOfView(this@CustomView, child) {
                            it.contains(child.x.toInt(), if (isBottomCollision) {
                                Log.d("is-bottom", "${child.y.toInt()}")
                                child.bottom + snapValue
                            } else {
                                child.y.toInt() - child.height + snapValue
                            })
                        }

                        return if (rect != null) {
                            return if (isBottomCollision) {
                                rect.top - child.height
                            } else rect.bottom
                        } else child.y.toInt() + (if (isBottomCollision) snapValue else -snapValue)

                    }
                    else -> top
                }
            }

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                Log.d("clampChanged", "${left} ${top} ${dx} ${dy}")
            }
        }
    }

    private fun checkIfShouldSnapVertically(viewGroup: ViewGroup, view: View, isBottomCollision: Boolean, top: Int, snapValue: Int): Boolean {
        val snap = if (isBottomCollision) snapValue else 0 - snapValue
        val y: Int = (if (isBottomCollision) top + view.height else top) + snap
        val leftX: Int = view.left
        val rightX: Int = view.right
        val centerX: Int = (leftX + rightX) / 2
        return checkForEveryViewRect(viewGroup, view) {
            contains(it, leftX, y) || contains(it, rightX, y) || contains(it, centerX, y)
        }
    }

    private fun checkIfShouldSnapHorizontally(viewGroup: ViewGroup, view: View, isLeftCollision: Boolean, left: Int, snapValue: Int): Boolean {
        val snap = if (isLeftCollision) -snapValue else snapValue
        val x: Int = (if (isLeftCollision) left else left + view.width) + snap
        val topY: Int = view.top
        val lowY: Int = view.bottom
        val centerY: Int = (topY + lowY) / 2
        return checkForEveryViewRect(viewGroup, view) {
            contains(it, x, topY) || contains(it, x, lowY) || contains(it, x, centerY)
        }
    }

    private fun checkIfCollidesHorizonal(viewGroup: ViewGroup, view: View, leftCollision: Boolean, left: Int): Boolean {
        Log.d("clampHotizontalCheck", "${leftCollision}")
        val x: Int = if (leftCollision) left else left + view.width
        val topY: Int = view.top
        val lowY: Int = view.bottom
        val centerY: Int = (topY + lowY) / 2
        return checkForEveryViewRect(viewGroup, view) {
            contains(it, x, topY) || contains(it, x, lowY) || contains(it, x, centerY)
        }
    }

    private fun checkIfCollidesVerticaly(viewGroup: ViewGroup, view: View, isBottomCollision: Boolean, top: Int): Boolean {
        Log.d("clampVerticalCheck", "${isBottomCollision}")
        val y: Int = if (isBottomCollision) top + view.height else top
        val leftX: Int = view.left
        val rightX: Int = view.right
        val centerX: Int = (leftX + rightX) / 2
        return checkForEveryViewRect(viewGroup, view) {
            contains(it, leftX, y) || contains(it, rightX, y) || contains(it, centerX, y)
        }
    }

    private fun checkForEveryViewRect(viewGroup: ViewGroup,
                                      view: View,
                                      checkIfContains: (Rect) -> Boolean): Boolean {
        viewGroup.children
                .filter { it != view }
                .forEach {
                    val bounds = Rect()
                    it.getHitRect(bounds)
                    if (checkIfContains(bounds)) return true
                }
        return false
    }

    private fun getRectOfView(viewGroup: ViewGroup,
                              view: View,
                              checkIfContains: (Rect) -> Boolean): Rect? {
        viewGroup.children
                .filter { it != view }
                .forEach {
                    val bounds = Rect()
                    it.getHitRect(bounds)
                    if (checkIfContains(bounds)) return bounds
                }
        return null
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    private fun contains(rect: Rect, x: Int, y: Int): Boolean {
        with(rect) {
            return (left < right && top < bottom  // check for empty first

                    && x > left && x < right && y > top && y < bottom)
        }
    }
}


