package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonLanguageBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.extensions.px
import com.tpay.sdk.internal.Language


internal class ButtonLanguage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ButtonLanguageBinding.inflate(LayoutInflater.from(context), this, true)
    var languageChangeListener: LanguageChangeListener? = null

    var language: Language = Language.ENGLISH
        set(value) {
            binding.run {
                languagePicker.run {
                    removeAllViews()
                    Language.fromConfiguration.filter { it != Language.DEFAULT }.forEach { lang ->
                        if (value == lang) {
                            selectedLanguage.text = value.languageTag.uppercase()
                        }
                        addView(
                            TextView(context).apply {
                                text = lang.languageTag.uppercase()
                                background = ContextCompat.getDrawable(
                                    context,
                                    if (value == lang) LanguageState.SELECTED.background else LanguageState.NOT_SELECTED.background
                                )
                                setTextAppearance(R.style.Headline2)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        if (value == lang) LanguageState.SELECTED.textColor else LanguageState.NOT_SELECTED.textColor
                                    )
                                )
                                gravity = Gravity.CENTER_VERTICAL
                                setPadding(PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL)
                                onClick { language = lang }
                            }
                        )
                    }
                }
            }
            state = LanguageButtonState.COLLAPSED
            languageChangeListener?.onChange(value)
            field = value
        }

    var state: LanguageButtonState = LanguageButtonState.EXPANDED
        set(value) {
            binding.run {
                if (value == LanguageButtonState.EXPANDED) {
                    languagePicker.background = ContextCompat.getDrawable(context, value.background)
                } else {
                    root.background = ContextCompat.getDrawable(context, value.background)
                }
                selectedLanguage.isVisible = value == LanguageButtonState.COLLAPSED
                icon.isVisible = value == LanguageButtonState.COLLAPSED
                languagePicker.isVisible = value == LanguageButtonState.EXPANDED
            }
            field = value
        }

    init {
        state = LanguageButtonState.COLLAPSED
        language = Language.ENGLISH

        binding.root.onClick {
            state = LanguageButtonState.EXPANDED
        }
    }

    enum class LanguageState(val background: Int, val textColor: Int) {
        SELECTED(R.drawable.language_button_enabled, R.color.colorPrimary500),
        NOT_SELECTED(R.drawable.language_button_not_selected, R.color.colorNeutral500)
    }

    enum class LanguageButtonState(val background: Int) {
        COLLAPSED(R.drawable.button_secondary_language_states),
        EXPANDED(R.drawable.button_language_expanded_background)
    }

    internal interface LanguageChangeListener {
        fun onChange(language: Language)
    }

    companion object {
        private val PADDING_HORIZONTAL = 11.px
        private val PADDING_VERTICAL = 8.px
    }
}