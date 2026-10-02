package app.olauncher.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatEditText
import app.olauncher.helper.CheckboxSpan

/**
 * Scratchpad [AppCompatEditText] that additionally detects taps landing on a rendered
 * [CheckboxSpan] glyph and toggles the underlying `[ ]`/`[x]` markdown text in place.
 * Any other touch falls through to normal cursor placement/selection.
 */
class MarkdownEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var pressedCheckbox: CheckboxSpan? = null
    private var downX = 0f
    private var downY = 0f
    private var moved = false
    private var pressedBlankSpace = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var onBlankLongClick: (() -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            pressedCheckbox = checkboxAt(event.x, event.y)
            downX = event.x
            downY = event.y
            moved = false
            pressedBlankSpace = !textAt(event.x, event.y)
        }
        if (kotlin.math.abs(event.x - downX) > touchSlop || kotlin.math.abs(event.y - downY) > touchSlop) {
            moved = true
        }
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) pressedBlankSpace = false
        pressedCheckbox?.let { span ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (!moved && event.eventTime - event.downTime < ViewConfiguration.getLongPressTimeout()
                    && checkboxAt(event.x, event.y) === span) {
                    val editable = text ?: return true
                    val index = editable.getSpanStart(span) + 1
                    editable.replace(index, index + 1, if (span.checked) " " else "x")
                    performClick()
                }
                pressedCheckbox = null
            } else if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
                pressedCheckbox = null
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    override fun performLongClick(): Boolean {
        val onBlank = onBlankLongClick
        if (pressedBlankSpace && !moved && onBlank != null) {
            onBlank()
            performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            return true
        }
        return super.performLongClick()
    }

    private fun textAt(touchX: Float, touchY: Float): Boolean {
        if (text.isNullOrEmpty()) return false
        val currentLayout = layout ?: return false
        val x = touchX - totalPaddingLeft + scrollX
        val y = touchY - totalPaddingTop + scrollY
        if (y < 0 || y >= currentLayout.height) return false
        val line = currentLayout.getLineForVertical(y.toInt())
        return x >= currentLayout.getLineLeft(line) && x < currentLayout.getLineRight(line)
    }

    private fun checkboxAt(touchX: Float, touchY: Float): CheckboxSpan? {
        val editable = text ?: return null
        val currentLayout = layout ?: return null
        val x = touchX - totalPaddingLeft + scrollX
        val y = touchY - totalPaddingTop + scrollY
        if (y < 0 || y >= currentLayout.height) return null
        val line = currentLayout.getLineForVertical(y.toInt())
        return editable.getSpans(currentLayout.getLineStart(line), currentLayout.getLineEnd(line), CheckboxSpan::class.java)
            .firstOrNull { span ->
                val start = currentLayout.getPrimaryHorizontal(editable.getSpanStart(span))
                val end = currentLayout.getPrimaryHorizontal(editable.getSpanEnd(span))
                x >= minOf(start, end) && x < maxOf(start, end)
            }
    }
}
