package com.tpay.sdk.extensions

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.tpay.sdk.R

internal data class TextToSpan(
    val text: String,
    val typefaceStyle: TypefaceStyle,
    val url: String? = null
)

internal fun prepareBoldSpannedURLText(
    texts: List<TextToSpan>,
    context: Context
): SpannableString = texts.joinToString(" ") { it.text }.let { fullText ->
    SpannableString(fullText).apply {
        texts.forEach { textToSpan ->
            val indexOfStart = fullText.indexOf(textToSpan.text)
            val indexOfEnd = indexOfStart + textToSpan.text.length

            if (textToSpan.typefaceStyle == TypefaceStyle.MEDIUM) {
                setSpan(
                    ResourcesCompat.getFont(context, textToSpan.typefaceStyle.font)?.run {
                        CustomTypefaceSpan(this)
                    } ?: StyleSpan(Typeface.BOLD),
                    indexOfStart,
                    indexOfEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(context.getColor(R.color.colorPrimary500)),
                    indexOfStart,
                    indexOfEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            textToSpan.url?.let { url ->
                setSpan(
                    object : ClickableSpan() {
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                        }

                        override fun onClick(p0: View) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }
                    },
                    indexOfStart,
                    indexOfEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}

internal class CustomTypefaceSpan(private val font: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint?) {
        paint?.typeface = font
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = font
    }
}