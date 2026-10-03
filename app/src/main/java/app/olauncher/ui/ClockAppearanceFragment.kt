package app.olauncher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import app.olauncher.R
import app.olauncher.data.Prefs
import app.olauncher.databinding.FragmentClockAppearanceBinding
import app.olauncher.helper.ClockAppearance
import app.olauncher.helper.ClockStyle
import kotlin.math.roundToInt

class ClockAppearanceFragment : BaseFragment() {
    private var _binding: FragmentClockAppearanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs
    private lateinit var style: ClockStyle
    private var settingControls = true
    private var previewDateFormat = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentClockAppearanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)
        prefs = Prefs(requireContext())
        style = ClockAppearance.read(prefs)
        configureSpinners()
        configureSliders()
        binding.back.setOnClickListener { findNavController().navigateUp() }
        binding.reset.setOnClickListener {
            style = ClockStyle("sans", "sans", 66, 24, 500, 500, 0f, 0f, 0, 0)
            showStyle()
        }
        showStyle()
    }

    private fun configureSpinners() {
        binding.preset.adapter = adapter(listOf(getString(R.string.choose_preset)) + ClockAppearance.presets.map { it.name })
        binding.clockFont.adapter = adapter(ClockAppearance.fonts.map { it.first })
        binding.dateFont.adapter = adapter(ClockAppearance.fonts.map { it.first })
        binding.clockWeight.adapter = adapter(ClockAppearance.weights.map { it.first })
        binding.dateWeight.adapter = adapter(ClockAppearance.weights.map { it.first })
        binding.timeFormat.adapter = adapter(ClockAppearance.hourFormats.map { it.first })
        binding.dateFormat.adapter = adapter(ClockAppearance.dateFormats.map { it.first })

        binding.preset.onItemSelected { position ->
            if (!settingControls && position > 0) {
                style = ClockAppearance.presets[position - 1].style
                showStyle(false)
            }
        }
        binding.clockFont.onItemSelected { position ->
            val font = ClockAppearance.fonts[position].second
            if (font != style.clockFont) update { copy(clockFont = font) }
        }
        binding.dateFont.onItemSelected { position ->
            val font = ClockAppearance.fonts[position].second
            if (font != style.dateFont) update { copy(dateFont = font) }
        }
        binding.clockWeight.onItemSelected { position ->
            val weight = ClockAppearance.weights[position].second
            if (weight != style.clockWeight) update { copy(clockWeight = weight) }
        }
        binding.timeFormat.onItemSelected { position ->
            val mode = ClockAppearance.hourFormats[position].second
            if (!settingControls && mode != prefs.clockHourFormat) {
                prefs.clockHourFormat = mode
                ClockAppearance.applyHourFormat(binding.previewClock, mode)
            }
        }
        binding.dateWeight.onItemSelected { position ->
            val weight = ClockAppearance.weights[position].second
            if (weight != style.dateWeight) update { copy(dateWeight = weight) }
        }
        binding.dateFormat.onItemSelected { if (it != style.dateFormat) update { copy(dateFormat = it) } }
    }

    private fun configureSliders() {
        binding.clockSize.listen { update(false) { copy(clockSize = it + 40) } }
        binding.dateSize.listen { update(false) { copy(dateSize = it + 14) } }
        binding.clockSpacing.listen { update(false) { copy(clockSpacing = (it - 5) / 100f) } }
        binding.dateSpacing.listen { update(false) { copy(dateSpacing = (it - 5) / 100f) } }
        binding.lineSpacing.listen { update(false) { copy(lineSpacing = it) } }
    }

    private fun update(persist: Boolean = true, change: ClockStyle.() -> ClockStyle) {
        if (settingControls) return
        style = style.change()
        if (binding.preset.selectedItemPosition != 0) {
            settingControls = true
            binding.preset.setSelection(0)
            settingControls = false
        }
        applyStyle(persist)
    }

    private fun showStyle(clearPreset: Boolean = true) {
        settingControls = true
        if (clearPreset) binding.preset.setSelection(0)
        binding.clockFont.setSelection(ClockAppearance.fonts.indexOfFirst { it.second == style.clockFont }.coerceAtLeast(0))
        binding.dateFont.setSelection(ClockAppearance.fonts.indexOfFirst { it.second == style.dateFont }.coerceAtLeast(0))
        binding.clockWeight.setSelection(ClockAppearance.weights.indexOfFirst { it.second == style.clockWeight }.coerceAtLeast(0))
        binding.dateWeight.setSelection(ClockAppearance.weights.indexOfFirst { it.second == style.dateWeight }.coerceAtLeast(0))
        binding.timeFormat.setSelection(ClockAppearance.hourFormats.indexOfFirst { it.second == prefs.clockHourFormat }.coerceAtLeast(0))
        binding.dateFormat.setSelection(style.dateFormat)
        binding.clockSize.progress = style.clockSize - 40
        binding.dateSize.progress = style.dateSize - 14
        binding.clockSpacing.progress = (style.clockSpacing * 100).roundToInt() + 5
        binding.dateSpacing.progress = (style.dateSpacing * 100).roundToInt() + 5
        binding.lineSpacing.progress = style.lineSpacing
        settingControls = false
        applyStyle()
    }

    private fun applyStyle(persist: Boolean = true) {
        if (persist) ClockAppearance.write(prefs, style)
        ClockAppearance.apply(requireContext(), binding.previewClock, binding.previewDate, style)
        ClockAppearance.applyHourFormat(binding.previewClock, prefs.clockHourFormat)
        if (previewDateFormat != style.dateFormat) {
            binding.previewDate.text = ClockAppearance.formatDate(style)
            previewDateFormat = style.dateFormat
        }
        binding.clockSizeValue.text = getString(R.string.size_sp, style.clockSize)
        binding.dateSizeValue.text = getString(R.string.size_sp, style.dateSize)
        binding.clockSpacingValue.text = getString(R.string.letter_spacing_em, style.clockSpacing)
        binding.dateSpacingValue.text = getString(R.string.letter_spacing_em, style.dateSpacing)
        binding.lineSpacingValue.text = getString(R.string.spacing_dp, style.lineSpacing)
    }

    private fun adapter(items: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

    private fun Spinner.onItemSelected(selected: (Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = selected(position)
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun SeekBar.listen(changed: (Int) -> Unit) {
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) changed(progress)
            }
            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = ClockAppearance.write(prefs, style)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previewDateFormat = -1
        _binding = null
    }

    override fun onPause() {
        ClockAppearance.write(prefs, style)
        super.onPause()
    }
}
