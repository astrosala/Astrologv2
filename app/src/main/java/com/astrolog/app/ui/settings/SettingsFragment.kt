package com.astrolog.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.astrolog.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireContext().getSharedPreferences("astrolog_prefs", Context.MODE_PRIVATE)

        binding.switchLpro.isChecked = prefs.getBoolean("show_lpro", true)
        binding.switchHa.isChecked = prefs.getBoolean("show_ha", true)
        binding.switchOiii.isChecked = prefs.getBoolean("show_oiii", true)
        binding.switchSii.isChecked = prefs.getBoolean("show_sii", false)
        binding.switchLext.isChecked = prefs.getBoolean("show_lext", false)
        binding.switchCustom1.isChecked = prefs.getBoolean("show_custom1", false)
        binding.switchCustom2.isChecked = prefs.getBoolean("show_custom2", false)
        binding.editCustom1Name.setText(prefs.getString("custom1_name", "Filtro personalizado 1"))
        binding.editCustom2Name.setText(prefs.getString("custom2_name", "Filtro personalizado 2"))

        binding.buttonSaveSettings.setOnClickListener {
            prefs.edit().apply {
                putBoolean("show_lpro", binding.switchLpro.isChecked)
                putBoolean("show_ha", binding.switchHa.isChecked)
                putBoolean("show_oiii", binding.switchOiii.isChecked)
                putBoolean("show_sii", binding.switchSii.isChecked)
                putBoolean("show_lext", binding.switchLext.isChecked)
                putBoolean("show_custom1", binding.switchCustom1.isChecked)
                putBoolean("show_custom2", binding.switchCustom2.isChecked)
                putString("custom1_name", binding.editCustom1Name.text.toString().ifBlank { "Filtro personalizado 1" })
                putString("custom2_name", binding.editCustom2Name.text.toString().ifBlank { "Filtro personalizado 2" })
                apply()
            }
            Toast.makeText(requireContext(), "Ajustes guardados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
