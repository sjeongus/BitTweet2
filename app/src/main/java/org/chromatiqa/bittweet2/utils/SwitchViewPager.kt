package org.chromatiqa.bittweet2.utils

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class SwitchViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = false
}