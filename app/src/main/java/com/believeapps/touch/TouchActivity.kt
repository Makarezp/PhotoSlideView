package com.believeapps.touch

import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.ViewDragHelper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_touch.*

class TouchActivity : AppCompatActivity() {

    private lateinit var mDragHelper: ViewDragHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch)
        mDragHelper = ViewDragHelper.create(rootView,1.0f, createCallback())
    }



    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("dispatch touch", ev.toString())
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            Log.d("touch", it.toString())
        }
        return super.onTouchEvent(event)
    }

    fun createCallback(): ViewDragHelper.Callback {
        return object: ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return true
            }
        }
    }
}
