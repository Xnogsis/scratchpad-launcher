package app.olauncher.helper

import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAppearanceTest {
    @Test
    fun `presets fit every editor control`() {
        val fonts = ClockAppearance.fonts.map { it.second }
        val weights = ClockAppearance.weights.map { it.second }

        assertTrue(ClockAppearance.presets.size >= 16)
        ClockAppearance.presets.forEach { preset ->
            with(preset.style) {
                assertTrue(clockFont in fonts && dateFont in fonts)
                assertTrue(clockSize in 40..96 && dateSize in 14..32)
                assertTrue(clockWeight in weights && dateWeight in weights)
                assertTrue(clockSpacing in -0.05f..0.20f && dateSpacing in -0.05f..0.20f)
                assertTrue(lineSpacing in 0..24)
                assertTrue(dateFormat in ClockAppearance.dateFormats.indices)
            }
        }
    }
}
