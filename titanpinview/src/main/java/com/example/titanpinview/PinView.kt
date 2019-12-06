package com.example.titanpinview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat

class PinView : AppCompatEditText {


    private val MIN_LINE_LENGTH = dpToPx(50f)
    private val spaceBetweenLines = dpToPx(23f)
    private val bottomSpace = dpToPx(20f)
    private var fontSize = 42
    private var placeholder: String = "."
    private var numDigits = 4
    private var showValue: Boolean = false

    private lateinit var linePaint: Paint
    private lateinit var placeholderPaint: Paint

    private var onPinEnteredListener: OnPinEnteredListener? = null


    constructor(context: Context) : super(context)


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (attrs != null)
            init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (attrs != null)
            init(context, attrs)
    }

    fun setOnPinEnteredListener(onPinEnteredListener: OnPinEnteredListener?) {
        this.onPinEnteredListener = onPinEnteredListener
    }

    fun showValue(value: Boolean) {
        this.showValue = value
        requestLayout()
    }

    fun isShowValue(): Boolean {
        return showValue
    }

    fun setNumDigits(numDigits: Int) {
        this.numDigits = numDigits
        setMaxDigits(numDigits)
        requestLayout()
    }

    private fun setMaxDigits(numDigits: Int) {
        this.filters = arrayOf(*this.filters, LengthFilter(numDigits))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width: Int
        var height: Int
        when (widthMode) {
            MeasureSpec.EXACTLY -> width = widthSize
            MeasureSpec.UNSPECIFIED -> width = calculateWidth()
            MeasureSpec.AT_MOST -> {
                width = calculateWidth()
                if (width > widthSize) width = widthSize
            }
            else -> width = widthSize
        }
        when (heightMode) {
            MeasureSpec.EXACTLY -> height = heightSize
            MeasureSpec.UNSPECIFIED -> height = calculateHeight()
            MeasureSpec.AT_MOST -> {
                height = calculateHeight()
                if (height > heightSize) height = heightSize
            }
            else -> height = heightSize
        }
        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas) {
        val leftStartPadding = ViewCompat.getPaddingStart(this)
        val availableWidth = width - ViewCompat.getPaddingEnd(this) - leftStartPadding
        val lineSize =
            (availableWidth - spaceBetweenLines * (numDigits - 1)) / numDigits
        var startX = leftStartPadding
        val startY = height - paddingBottom
        var stopX = startX + lineSize
        val text = text
        if (text != null) {
            val textLength = text.length
            for (i in 0 until numDigits) {
                canvas.drawLine(
                    startX.toFloat(),
                    startY.toFloat(),
                    (startX + lineSize),
                    startY.toFloat(),
                    linePaint
                )
                if (textLength > i) {
                    val middle = (startX + stopX) / 2
                    canvas.drawText(
                        if (showValue) text.toString()[i].toString() else placeholder,
                        middle - placeholderPaint.measureText(placeholder) / 2,
                        startY - bottomSpace,
                        placeholderPaint
                    )
                }
                startX += lineSize.toInt() + spaceBetweenLines.toInt()
                stopX = startX + lineSize
            }
        }
    }

    private fun init(
        context: Context,
        attrs: AttributeSet
    ) {
        initAttributes(context, attrs)
        setMaxDigits(numDigits)
        disableCopyPaste()
        setCursorToLastPositionAlways()
        setCallbackToListener()
        setBackgroundResource(0)
        inputType = InputType.TYPE_CLASS_TEXT
        this.isCursorVisible = false
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR)
    }

    private fun disableCopyPaste() {
        this.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean {
                return false
            }

            override fun onActionItemClicked(
                mode: ActionMode,
                item: MenuItem
            ): Boolean {
                return false
            }
        }
        setOnLongClickListener { v: View? -> true }
    }

    private fun setCursorToLastPositionAlways() {
        setOnClickListener { v: View? ->
            val text = text
            if (text != null) setSelection(text.length)
        }
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PinView)

        try {
            numDigits = typedArray.getInt(R.styleable.PinView_num_digits, 4)
            val bottomLineColor = typedArray.getColor(
                R.styleable.PinView_bottom_line_color,
                context.resources.getColor(R.color.grey)
            )
            linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            linePaint.color = bottomLineColor
            linePaint.strokeWidth = dpToPx(2f)
            val circleColor = typedArray.getColor(
                R.styleable.PinView_placeholder_color,
                context.resources.getColor(R.color.green)
            )
            placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            placeholderPaint.color = circleColor

            val holder = typedArray.getString(R.styleable.PinView_placeholder_character)
            if (!holder.isNullOrBlank()) {
                placeholder = holder[0].toString()
            }

            fontSize =
                typedArray.getDimensionPixelSize(
                    R.styleable.PinView_placeholder_font_size,
                    fontSize
                )
            placeholderPaint.textSize = fontSize.toFloat()

        } finally {
            typedArray.recycle()
        }
    }

    private fun setCallbackToListener() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (onPinEnteredListener != null && s.length == numDigits) {
                    onPinEnteredListener!!.onPinEntered(s.toString())
                }
            }
        })
    }

    private fun calculateHeight(): Int {
        return (paddingBottom + bottomSpace + fontSize + linePaint.strokeWidth + paddingTop).toInt()
    }

    private fun calculateWidth(): Int {
        val measuredLinesWidth = numDigits * MIN_LINE_LENGTH
        val measuredLinesSpacing = (numDigits - 1) * spaceBetweenLines
        return (ViewCompat.getPaddingStart(this) + measuredLinesWidth + measuredLinesSpacing + ViewCompat.getPaddingEnd(
            this
        )).toInt()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }


    @FunctionalInterface
    interface OnPinEnteredListener {
        fun onPinEntered(pin: String)
    }
}