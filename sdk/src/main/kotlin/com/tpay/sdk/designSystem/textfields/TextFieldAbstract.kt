@file:Suppress("LeakingThis", "unused", "MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.tpay.sdk.R
import com.tpay.sdk.databinding.TextFieldAbstractBinding
import com.tpay.sdk.extensions.*


internal abstract class TextFieldAbstract @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    protected val binding =
        TextFieldAbstractBinding.inflate(LayoutInflater.from(context), this, true)
    private var inputValidator: InputValidator = object : InputValidator {
        override fun validate(value: String): String? = null
    }
    private var inputFormatter: InputFormatter? = null
    private var focusListener: FocusListener? = null
    private var onTextChangedListener: OnTextChangedListener? = null
    private var isWritingFirstTime = true
    var modifiedContext: Context? = null
    var isRequired = true

    protected val languageContext: Context
        get() = modifiedContext ?: context

    var textFieldIcons: TextFieldIcons = TextFieldIcons.NONE
        set(value) {
            binding.run {
                textField.setPadding(TEXT_FIELD_PADDING_START, 0, value.textFieldPaddingEnd, 0)
                isNfcIconVisible = value == TextFieldIcons.ALL
                isScanIconVisible = value == TextFieldIcons.ALL
            }
            field = value
        }

    var formattedText: String
        get() = binding.textField.text.toString().trim()
        set(value) {
            binding.textField.setText(value)
        }

    var notFormattedText: String
        get() = inputFormatter?.removeFormatting(formattedText) ?: binding.textField.text.toString()
        set(value) {
            binding.textField.setText(inputFormatter?.format(value) ?: value)
        }

    protected var previousText: String = formattedText

    val text: Observable<Text> = Observable(Text("", ""))

    var hint: String
        get() = binding.textFieldLayout.hint.toString()
        set(value) {
            binding.textFieldLayout.hint = value
        }

    var errorMessage: String?
        get() = binding.textFieldLayout.error.toString()
        set(value) {
            binding.textFieldLayout.run {
                error = value
                isErrorEnabled = value != null
            }
        }

    var isNfcIconVisible: Boolean
        get() = binding.iconNfc.isVisible
        set(value) {
            binding.run {
                iconNfc.isVisible = value
                iconScan.updateMargins(end = if(value) SCAN_ICON_MARGIN_END_WHEN_NFC_VISIBLE else SCAN_ICON_MARGIN_END_WHEN_NFC_GONE)
            }
        }

    var isScanIconVisible: Boolean
        get() = binding.iconScan.isVisible
        set(value) {
            binding.iconScan.isVisible = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.TextFieldAbstract).apply {
                try {
                    formattedText = getString(R.styleable.TextFieldAbstract_textFieldText) ?: ""
                    hint = getString(R.styleable.TextFieldAbstract_textFieldHint) ?: ""

                    getInteger(
                        R.styleable.TextFieldAbstract_textFieldIcons,
                        TextFieldIcons.NONE.ordinal
                    ).let {
                        textFieldIcons =
                            if (it == TextFieldIcons.ALL.ordinal) TextFieldIcons.ALL else TextFieldIcons.NONE
                    }
                } finally {
                    recycle()
                }
            }
        }

        binding.textField.run {
            setOnFocusChangeListener { _, hasFocus ->
                focusListener?.onChange(hasFocus, formattedText.isEmpty())
                errorMessage = if (isEnabled) {
                    if (!hasFocus) {
                        if (formattedText.isBlank() && isRequired) languageContext.getString(R.string.field_required)
                        else inputValidator.validate(inputFormatter?.removeFormatting(formattedText) ?: notFormattedText)
                    } else null
                } else null
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val value = sequence.toString().trim()
                    val notFormatted = inputFormatter?.removeFormatting(value) ?: value
                    onTextChangedListener?.onChange(notFormatted)
                    val validationResult = if (isWritingFirstTime || !isEnabled) null else {
                        when {
                            notFormatted.isBlank() && isRequired -> languageContext.getString(R.string.field_required)
                            else -> inputValidator.validate(notFormatted)
                        }
                    }
                    errorMessage = validationResult
                }

                override fun afterTextChanged(editable: Editable?) {
                    val original = editable?.toString() ?: return
                    if (previousText.length > formattedText.length) isWritingFirstTime = false
                    if (previousText == original) return
                    val formatter = inputFormatter ?: kotlin.run {
                        this@TextFieldAbstract.text.value = Text(original, original)
                        previousText = original
                        return
                    }
                    binding.textField.run {
                        val cursor = selectionEnd
                        val formatted = formatter.format(formatter.removeFormatting(original))
                        try {
                            when (formatter) {
                                is NumberSpacingFormatter -> {
                                    if (original.length > previousText.length){
                                        previousText = formatted
                                        formattedText = formatted
                                        setSelection(
                                            if (cursor == original.length) {
                                                formatted.length
                                            } else {
                                                if (formatted.getOrNull(cursor - 1) == formatter.separator) {
                                                    cursor + 1
                                                } else {
                                                    cursor
                                                }
                                            }
                                        )
                                    } else {
                                        if (formatted.getOrNull(cursor) == formatter.separator) {
                                            val before = formatted.substring(0, cursor - 1)
                                            val after = formatted.substring(cursor)

                                            val full = formatter.format(formatter.removeFormatting("$before$after"))
                                            previousText = full
                                            formattedText = full
                                            setSelection(cursor - 1)
                                        } else {
                                            val adjustedCursor = if (original.lastOrNull() == formatter.separator) cursor - 1 else cursor

                                            val before = formatted.substring(0, adjustedCursor)
                                            val after = formatted.substring(adjustedCursor)

                                            val full = formatter.format(formatter.removeFormatting("$before$after"))
                                            previousText = full
                                            formattedText = full
                                            setSelection(adjustedCursor)
                                        }
                                    }
                                }
                                is ExpirationDateFormatter -> {
                                    original.firstOrNull()?.run {
                                        if (original.length == 1 && isDigit() && digitToInt() in 2..9) {
                                            setText("")
                                            return
                                        }
                                    }
                                    if (original.length > previousText.length){
                                        if (formatted.getOrNull(2) == formatter.separator && cursor == 2) {
                                            previousText = formatted
                                            formattedText = formatted
                                            setSelection(3)
                                        } else {
                                            previousText = formatted
                                            formattedText = formatted
                                            setSelection(
                                                if (cursor == original.length) {
                                                    formatted.length
                                                } else {
                                                    if (formatted.getOrNull(cursor - 1) == formatter.separator) {
                                                        cursor + 1
                                                    } else {
                                                        cursor
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        if (formatted.getOrNull(cursor) == formatter.separator) {
                                            val before = formatted.substring(0, cursor - 1)
                                            val after = formatted.substring(cursor)

                                            val full = formatter.format(formatter.removeFormatting("$before$after"))
                                            previousText = full
                                            formattedText = full
                                            setSelection(cursor - 1)
                                        } else {
                                            if (original.lastOrNull() != formatter.separator && !previousText.endsWith(formatter.separator)) {
                                                val before = formatted.substring(0, cursor)
                                                val after = formatted.substring(cursor)

                                                val full = formatter.format(formatter.removeFormatting("$before$after"))
                                                previousText = full
                                                formattedText = full
                                                setSelection(cursor)
                                            } else {
                                                if (previousText.endsWith(formatter.separator) && !original.contains(formatter.separator)) {
                                                    val before = formatted.substring(0, cursor - 1)
                                                    previousText = before
                                                    formattedText = before
                                                    setSelection(cursor - 1)
                                                } else {
                                                    previousText = original
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            this@TextFieldAbstract.text.value = Text(formatted = formattedText, notFormatted = notFormattedText)
                        } catch (e: Exception) {
                            setSelection(0)
                        }
                    }
                }
            })
        }
    }

    fun onNfcIconClick(func: (View) -> Unit) {
        binding.iconNfc.setOnClickListener(func)
    }

    fun onScanIconClick(func: (View) -> Unit) {
        binding.iconScan.setOnClickListener(func)
    }

    fun setImeOptions(imeOptions: Int) {
        binding.textField.imeOptions = imeOptions
    }

    fun setInputFormatter(inputFormatter: InputFormatter) {
        this.inputFormatter = inputFormatter
    }

    fun setInputValidator(inputValidator: InputValidator) {
        this.inputValidator = inputValidator
    }

    fun setFocusListener(focusListener: FocusListener) {
        this.focusListener = focusListener
    }

    fun setOnTextChangedListener(onTextChangedListener: OnTextChangedListener) {
        this.onTextChangedListener = onTextChangedListener
    }

    fun setTextLength(length: Int) {
        binding.textField.addFilter(InputFilter.LengthFilter(length))
    }

    fun setInputType(textFieldInputType: TextFieldInputType) {
        binding.textField.inputType = when (textFieldInputType) {
            TextFieldInputType.TEXT -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            TextFieldInputType.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            TextFieldInputType.NUMBER -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        }
    }

    fun reset(){
        formattedText = ""
        errorMessage = null
        isWritingFirstTime = true
    }

    fun setEms(ems: Int) {
        binding.textField.maxEms = ems
    }

    fun copyPasteEnabled(enabled: Boolean) {
        binding.textField.run {
            isLongClickable = enabled
            setTextIsSelectable(enabled)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.run {
            textFieldLayout.isEnabled = enabled
            iconScan.isEnabled = enabled
            iconNfc.isEnabled = enabled
        }
    }

    enum class TextFieldIcons(val textFieldPaddingEnd: Int) {
        NONE(TEXT_FIELD_PADDING_END), ALL(TEXT_FIELD_PADDING_END_WITH_ICONS)
    }

    enum class TextFieldInputType {
        TEXT, NUMBER, EMAIL
    }

    interface InputValidator {
        fun validate(value: String): String?
    }

    interface InputFormatter {
        val separator: Char

        fun format(value: String): String
        fun removeFormatting(value: String): String
    }

    protected class NumberSpacingFormatter(
        override val separator: Char,
        val every: Int
    ) : InputFormatter {
        override fun format(value: String): String = value.applyFormatting(every, separator)
        override fun removeFormatting(value: String): String = value.replace(separator.toString(), "")
    }

    protected class ExpirationDateFormatter : InputFormatter {
        override val separator: Char = '/'
        private var lastInput = ""

        override fun format(value: String): String {
            val withoutSlash = value.replace(separator.toString(), "")
            return when {
                withoutSlash.length == 1 -> withoutSlash
                value.length == 3 && lastInput.length < value.length && value.endsWith(separator) -> value
                value.length == 2 && lastInput.length < value.length -> withoutSlash.applyFormattingRightAway(2, separator)
                value.length == 2 && lastInput.length == 2 && !value.contains(separator) -> withoutSlash.applyFormattingRightAway(2, separator)
                else -> withoutSlash.applyFormatting(2, separator)
            }.also { lastInput = value }

        }

        override fun removeFormatting(value: String): String = value
    }

    interface FocusListener {
        fun onChange(hasFocus: Boolean, isEmpty: Boolean)
    }

    data class Text(val formatted: String, val notFormatted: String)

    interface OnTextChangedListener {
        fun onChange(value: String)
    }

    @Suppress("UNCHECKED_CAST")
    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.childrenStates = SparseArray()
        for (i in 0 until childCount) {
            getChildAt(i).saveHierarchyState(ss.childrenStates as SparseArray<Parcelable>)
        }
        return ss
    }

    @Suppress("UNCHECKED_CAST")
    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        for (i in 0 until childCount) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates as SparseArray<Parcelable>)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    private class SavedState(superState: Parcelable?) : BaseSavedState(superState) {
        var childrenStates: SparseArray<Any>? = null

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            childrenStates?.let {
                out.writeSparseArray(it)
            }
        }
    }

    companion object {
        private val TEXT_FIELD_PADDING_START = 15.px
        private val TEXT_FIELD_PADDING_END = 15.px
        private val TEXT_FIELD_PADDING_END_WITH_ICONS = 85.px
        private val SCAN_ICON_MARGIN_END_WHEN_NFC_GONE = 10.px
        private val SCAN_ICON_MARGIN_END_WHEN_NFC_VISIBLE = 0.px
    }
}

internal fun String.applyFormattingRightAway(every: Int, char: Char): String =
    this.chunked(every).joinToString("") { s ->
        if (s.length == every) "$s$char" else s
    }

internal fun String.applyFormatting(every: Int, char: Char): String =
    this.chunked(every)
        .let { chunked ->
            chunked.mapIndexed { index, s ->
                if (s.length == every && chunked.lastIndex != index) "$s$char" else s
            }.joinToString("")
        }