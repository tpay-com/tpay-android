package com.tpay.sdk.designSystem.cards

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.tpay.sdk.databinding.CardErrorBinding


internal class CardError @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = CardErrorBinding.inflate(LayoutInflater.from(context), this, true)

    var text: String
        get() = binding.errorText.text.toString()
        set(value) {
            binding.errorText.text = value
        }

    var isAnimationInProgress: Boolean = false

    fun startAnimation(duration: Long, onEnd: () -> Unit){
        if(!isAnimationInProgress){
            binding.errorProgressBar.run {
                max = PROGRESS_BAR_MAXIMUM_VALUE

                ValueAnimator.ofInt(PROGRESS_BAR_MAXIMUM_VALUE, 0).run {
                    setDuration(duration)
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        progress = it.animatedValue as Int
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(p0: Animator) {
                            isAnimationInProgress = true
                        }
                        override fun onAnimationEnd(p0: Animator) {
                            isAnimationInProgress = false
                            onEnd()
                        }
                        override fun onAnimationCancel(p0: Animator) {
                            isAnimationInProgress = false
                        }
                        override fun onAnimationRepeat(p0: Animator) {}
                    })
                    start()
                }
            }
        }
    }

    companion object {
        const val PROGRESS_BAR_MAXIMUM_VALUE = 1000
    }
}