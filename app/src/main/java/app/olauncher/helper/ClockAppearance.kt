package app.olauncher.helper

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import app.olauncher.R
import app.olauncher.data.Prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class ClockStyle(
    val clockFont: String,
    val dateFont: String,
    val clockSize: Int,
    val dateSize: Int,
    val clockWeight: Int,
    val dateWeight: Int,
    val clockSpacing: Float,
    val dateSpacing: Float,
    val lineSpacing: Int,
    val dateFormat: Int,
)

data class ClockPreset(val name: String, val style: ClockStyle)

object ClockAppearance {
    val fonts = listOf(
        "Sans serif" to "sans",
        "Serif" to "serif",
        "Monospace" to "mono",
        "Condensed" to "condensed",
        "Space Grotesk" to "space_grotesk",
        "Fraunces" to "fraunces",
        "Fredoka" to "fredoka",
        "Caveat" to "caveat",
    )
    val weights = listOf("Regular" to 400, "Medium" to 500, "Bold" to 700)
    val dateFormats = listOf(
        "Thu, 1 Oct" to "EEE, d MMM",
        "Thursday, 1 October" to "EEEE, d MMMM",
        "1 October 2026" to "d MMMM yyyy",
        "Oct 1" to "MMM d",
        "2026-10-01" to "yyyy-MM-dd",
    )
    val presets = listOf(
        preset("Minimal · Quiet", "sans", "sans", 58, 18, 400, 400, -0.02f, 0.02f, 4, 0),
        preset("Minimal · Air", "sans", "sans", 54, 16, 400, 400, 0.12f, 0.10f, 14, 3),
        preset("Minimal · Mono", "mono", "mono", 54, 16, 400, 400, 0.04f, 0.06f, 8, 4),
        preset("Clean · Focus", "sans", "sans", 72, 18, 700, 500, -0.03f, 0.02f, 3, 0),
        preset("Clean · Compact", "condensed", "condensed", 68, 18, 500, 400, 0.02f, 0.05f, 2, 3),
        preset("Clean · Classic", "serif", "sans", 64, 17, 700, 400, 0f, 0.04f, 6, 0),
        preset("Modern · Grid", "space_grotesk", "space_grotesk", 64, 17, 500, 400, -0.01f, 0.08f, 6, 4),
        preset("Modern · Signal", "space_grotesk", "mono", 72, 16, 700, 500, -0.04f, 0.10f, 4, 3),
        preset("Modern · Wide", "space_grotesk", "space_grotesk", 56, 17, 400, 500, 0.10f, 0.12f, 12, 0),
        preset("Editorial · Daily", "fraunces", "sans", 70, 18, 700, 500, -0.03f, 0.01f, 4, 1),
        preset("Editorial · Book", "fraunces", "serif", 62, 20, 500, 400, 0.01f, 0.02f, 5, 2),
        preset("Editorial · Masthead", "fraunces", "condensed", 76, 16, 700, 500, -0.05f, 0.14f, 1, 3),
        preset("Playful · Bubble", "fredoka", "fredoka", 68, 18, 700, 500, 0.01f, 0.04f, 6, 0),
        preset("Playful · Weekend", "fredoka", "caveat", 62, 23, 500, 700, 0.03f, 0.01f, 3, 1),
        preset("Playful · Scribble", "caveat", "caveat", 76, 24, 700, 500, -0.01f, 0.02f, 0, 0),
        preset("Playful · Note", "caveat", "sans", 70, 17, 500, 500, 0.02f, 0.08f, 2, 3),
    )

    private val typefaces = mutableMapOf<String, Typeface>()

    fun read(prefs: Prefs) = ClockStyle(
        prefs.clockFont, prefs.dateFont, prefs.clockTextSize, prefs.dateTextSize,
        prefs.clockFontWeight, prefs.dateFontWeight, prefs.clockLetterSpacing,
        prefs.dateLetterSpacing, prefs.clockDateSpacing, prefs.dateFormat.coerceIn(dateFormats.indices),
    )

    fun write(prefs: Prefs, style: ClockStyle) {
        prefs.updateClockAppearance(
            style.clockFont, style.dateFont, style.clockSize, style.dateSize,
            style.clockWeight, style.dateWeight, style.clockSpacing,
            style.dateSpacing, style.lineSpacing, style.dateFormat,
        )
    }

    fun apply(context: Context, clock: TextView, date: TextView, style: ClockStyle) {
        clock.typeface = typeface(context, style.clockFont, style.clockWeight)
        date.typeface = typeface(context, style.dateFont, style.dateWeight)
        clock.textSize = style.clockSize.toFloat()
        date.textSize = style.dateSize.toFloat()
        clock.letterSpacing = style.clockSpacing
        date.letterSpacing = style.dateSpacing
        date.updatePadding(top = (style.lineSpacing * context.resources.displayMetrics.density).roundToInt())
    }

    fun formatDate(style: ClockStyle, now: Date = Date()): String =
        SimpleDateFormat(dateFormats[style.dateFormat].second, Locale.getDefault()).format(now).replace(".,", ",")

    private fun typeface(context: Context, font: String, weight: Int): Typeface =
        typefaces.getOrPut("$font-$weight") {
            val resource = when (font) {
                "space_grotesk" -> customFont(weight, R.font.space_grotesk_400, R.font.space_grotesk_500, R.font.space_grotesk_700)
                "fraunces" -> customFont(weight, R.font.fraunces_400, R.font.fraunces_500, R.font.fraunces_700)
                "fredoka" -> customFont(weight, R.font.fredoka_400, R.font.fredoka_500, R.font.fredoka_700)
                "caveat" -> customFont(weight, R.font.caveat_400, R.font.caveat_500, R.font.caveat_700)
                else -> 0
            }
            if (resource != 0) ResourcesCompat.getFont(context, resource)!!
            else {
                val family = when (font) {
                    "serif" -> "serif"
                    "mono" -> "monospace"
                    "condensed" -> "sans-serif-condensed"
                    else -> "sans-serif"
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Typeface.create(Typeface.create(family, Typeface.NORMAL), weight, false)
                } else {
                    val legacyFamily = if (weight == 500 && font == "sans") "sans-serif-medium" else family
                    Typeface.create(legacyFamily, if (weight == 700) Typeface.BOLD else Typeface.NORMAL)
                }
            }
        }

    private fun customFont(weight: Int, regular: Int, medium: Int, bold: Int) =
        when (weight) { 500 -> medium; 700 -> bold; else -> regular }

    private fun preset(
        name: String, clockFont: String, dateFont: String, clockSize: Int, dateSize: Int,
        clockWeight: Int, dateWeight: Int, clockSpacing: Float, dateSpacing: Float,
        lineSpacing: Int, dateFormat: Int,
    ) = ClockPreset(name, ClockStyle(clockFont, dateFont, clockSize, dateSize, clockWeight, dateWeight, clockSpacing, dateSpacing, lineSpacing, dateFormat))
}
