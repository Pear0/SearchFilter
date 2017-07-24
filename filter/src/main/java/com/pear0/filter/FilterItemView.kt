package com.pear0.filter

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.TextView
import com.yalantis.filter.R
import android.content.ContextWrapper
import android.app.Activity
import java.util.logging.Handler


/**
 * Created by William on 7/22/2017.
 */
class FilterItemView : TextView {

    enum class Type {
        EXPANDED,
        COLLAPSED,
        ANIMATED
    }

    var index: Int = 0

    var color: Int = 0
        set(value) {
            field = value

            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.rounded_rect, null)!!
            drawable.setColorFilter(value, PorterDuff.Mode.MULTIPLY)
            setBackgroundDrawable(drawable)

        }

    var type = Type.EXPANDED
        private set

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        setBackgroundResource(R.drawable.rounded_rect)
        setExpanded()
    }

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    fun setText(text: String) {
        this.text = text
    }


    fun setExpanded() {
        type = Type.EXPANDED
    }

    fun setCollapsed() {
        type = Type.COLLAPSED
        setText("X")
    }

    private fun removeSelf() {
        (parent as ViewGroup).postDelayed({
            getActivity()!!.runOnUiThread {
                (parent as ViewGroup).removeView(this)
            }
        }, 100)
    }

    fun setTransitioning(collapsed: FilterItemView, expanded: FilterItemView, collapsedYOffset: Int, reverse: Boolean = false) {
        type = Type.ANIMATED

        val animations = ArrayList<Animator>()

        fun animate(animator: ValueAnimator, func: (ValueAnimator) -> Unit) {
            animations.add(animator.apply {
                addUpdateListener {
                    func(it)
                }
            })
        }

        animate(ValueAnimator.ofFloat(collapsed.x, expanded.x)) {
            x = it.animatedValue as Float
        }

        animate(ValueAnimator.ofFloat(collapsed.y + collapsedYOffset, expanded.y)) {
            y = it.animatedValue as Float
        }

        animate(ValueAnimator.ofInt(collapsed.width, expanded.width)) {

            val l = layoutParams
            l.width = it.animatedValue as Int
            layoutParams = l
            requestLayout()
        }

        animate(ValueAnimator.ofInt(collapsed.height, expanded.height)) {
            val l = layoutParams
            l.height = it.animatedValue as Int
            layoutParams = l
            requestLayout()
        }

        val set = AnimatorSet()
        set.playTogether(animations)
        if (reverse) {
            set.interpolator = Interpolator { input -> 1f - input }
        }
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {


                Log.i("FilterItemView", "${parent}")
                removeSelf()
            }

            override fun onAnimationCancel(animation: Animator?) {
                removeSelf()
            }

            override fun onAnimationStart(animation: Animator?) {
                Log.i("FilterItemView", "onStart")
                visibility = View.VISIBLE
            }
        })
        set.start()

        Log.i("FilterItemView", "setTransitioning()")

    }

}