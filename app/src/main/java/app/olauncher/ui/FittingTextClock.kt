package app.olauncher.ui

import android.content.Context
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextClock
import kotlin.math.min

class FittingTextClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : TextClock(context, attrs, defStyleAttr) {
    private var preferredSizePx = textSize
    private val sizingPaint = TextPaint()

    init {
        maxLines = 1
        setHorizontallyScrolling(false)
    }

    fun setPreferredSizeSp(size: Int) {
        val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size.toFloat(), resources.displayMetrics)
        if (preferredSizePx != pixels) {
            preferredSizePx = pixels
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var fittedSize = preferredSizePx
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            val width = (MeasureSpec.getSize(widthMeasureSpec) - compoundPaddingLeft - compoundPaddingRight - 2).coerceAtLeast(1)
            sizingPaint.set(paint)
            sizingPaint.textSize = preferredSizePx
            val textWidth = Layout.getDesiredWidth(text, sizingPaint)
            if (textWidth > 0) fittedSize = min(preferredSizePx, preferredSizePx * width / textWidth)
        }
        if (textSize != fittedSize) super.setTextSize(TypedValue.COMPLEX_UNIT_PX, fittedSize)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
