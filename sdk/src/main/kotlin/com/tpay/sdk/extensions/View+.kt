package com.tpay.sdk.extensions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

internal fun View.updateMargins(
    start: Int = (this.layoutParams as ViewGroup.MarginLayoutParams).marginStart,
    top: Int = (this.layoutParams as ViewGroup.MarginLayoutParams).topMargin,
    end: Int = (this.layoutParams as ViewGroup.MarginLayoutParams).marginEnd,
    bottom: Int = (this.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
) {
    (this.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start
    (this.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
    (this.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = end
    (this.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = bottom
}

internal fun View.focusAndShowKeyboard() {
    fun View.showTheKeyboardNow() {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    requestFocus()

    if (hasWindowFocus()) {
        showTheKeyboardNow()
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

internal fun View.onClick(func: () -> Unit) {
    this.setOnClickListener {
        func()
    }
}

internal var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

internal var View.isInvisible: Boolean
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }