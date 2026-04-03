package com.astrolog.app.ui.calendar

import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Season
import com.astrolog.app.data.repository.AstroRepository
import com.astrolog.app.databinding.FragmentCalendarBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CalendarViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AstroRepository
    val objects = MutableLiveData<List<AstroObject>>()
    val activeSeason = MutableLiveData<Season?>()
    val allSeasons = MutableLiveData<List<Season>>()

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
    }

    fun load() = viewModelScope.launch {
        val season = repo.getActiveSeason()
        activeSeason.value = season

        // Filtrar objetos por temporada activa
        val allObjects = repo.getAllObjectsOnce()
        objects.value = if (season != null) {
            allObjects.filter { it.seasonId == season.id }
        } else {
            allObjects
        }

        allSeasons.value = repo.getAllSeasonsOnce()
    }

    fun updateVisibility(
        obj: AstroObject,
        m1: String, m2: String, m3: String, m4: String,
        filter: String
    ) = viewModelScope.launch {
        repo.updateObject(obj.copy(
            visibilityMonth1 = m1, visibilityMonth2 = m2,
            visibilityMonth3 = m3, visibilityMonth4 = m4,
            visibilityMarch = m1, visibilityApril = m2,
            visibilityMay = m3, visibilityJune = m4,
            mainFilter = filter
        ))
        load()
    }

    fun createSeason(name: String, m1: String, m2: String, m3: String, m4: String) = viewModelScope.launch {
        repo.insertSeason(Season(name = name, month1 = m1, month2 = m2, month3 = m3, month4 = m4))
        load()
    }

    fun setActiveSeason(season: Season) = viewModelScope.launch {
        repo.setActiveSeason(season)
        load()
    }

    fun deleteSeason(season: Season) = viewModelScope.launch {
        repo.deleteSeason(season)
        load()
    }
}

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var adapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CalendarAdapter { obj -> showEditDialog(obj) }
        binding.recyclerCalendar.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCalendar.adapter = adapter

        viewModel.objects.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.activeSeason.observe(viewLifecycleOwner) { season ->
            if (season != null) {
                binding.textSeasonName.text = season.name
                binding.textSeasonMonths.text = "${season.month1} · ${season.month2} · ${season.month3} · ${season.month4}"
            } else {
                binding.textSeasonName.text = "Sin temporada activa"
                binding.textSeasonMonths.text = "Crea una temporada para empezar"
            }
        }

        binding.buttonNewSeason.setOnClickListener { showNewSeasonDialog() }
        binding.buttonChangeSeason.setOnClickListener { showChangeSeasonDialog() }

        viewModel.load()
    }

    private fun showNewSeasonDialog() {
        val dialogView = layoutInflater.inflate(
            com.astrolog.app.R.layout.dialog_new_season, null
        )
        val nameEdit = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_season_name)
        val m1Edit = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_month1)
        val m2Edit = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_month2)
        val m3Edit = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_month3)
        val m4Edit = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_month4)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva temporada")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val name = nameEdit?.text.toString().ifBlank { "Nueva temporada" }
                val m1 = m1Edit?.text.toString().ifBlank { "Mes 1" }
                val m2 = m2Edit?.text.toString().ifBlank { "Mes 2" }
                val m3 = m3Edit?.text.toString().ifBlank { "Mes 3" }
                val m4 = m4Edit?.text.toString().ifBlank { "Mes 4" }
                viewModel.createSeason(name, m1, m2, m3, m4)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showChangeSeasonDialog() {
        val seasons = viewModel.allSeasons.value ?: return
        if (seasons.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sin temporadas")
                .setMessage("Crea primero una temporada con el botón +")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        val names = seasons.map { "${it.name}${if (it.isActive) " ✓" else ""}" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar temporada activa")
            .setItems(names) { _, which ->
                viewModel.setActiveSeason(seasons[which])
            }
            .setNeutralButton("Eliminar") { _, _ -> showDeleteSeasonDialog(seasons) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteSeasonDialog(seasons: List<Season>) {
        val names = seasons.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar temporada")
            .setItems(names) { _, which ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("¿Eliminar ${seasons[which].name}?")
                    .setMessage("Se elimina la temporada pero no los objetos ni las sesiones.")
                    .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteSeason(seasons[which]) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(obj: AstroObject) {
        val season = viewModel.activeSeason.value
        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        val visValues = arrayOf("★", "✓", "~", "—")
        fun indexOf(v: String) = visValues.indexOfFirst { it == v }.takeIf { it >= 0 } ?: 3

        val dialogView = layoutInflater.inflate(
            com.astrolog.app.R.layout.dialog_edit_visibility, null
        )
        val marSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_edit_mar)
        val abrSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_edit_abr)
        val maySpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_edit_may)
        val junSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_edit_jun)
        val filterEdit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.astrolog.app.R.id.edit_vis_filter
        )

        val label1 = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.label_month1)
        val label2 = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.label_month2)
        val label3 = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.label_month3)
        val label4 = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.label_month4)
        label1?.text = season?.month1 ?: "Mes 1"
        label2?.text = season?.month2 ?: "Mes 2"
        label3?.text = season?.month3 ?: "Mes 3"
        label4?.text = season?.month4 ?: "Mes 4"

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, visOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listOf(marSpinner, abrSpinner, maySpinner, junSpinner).forEach { it.adapter = spinnerAdapter }

        marSpinner.setSelection(indexOf(obj.visibilityMonth1))
        abrSpinner.setSelection(indexOf(obj.visibilityMonth2))
        maySpinner.setSelection(indexOf(obj.visibilityMonth3))
        junSpinner.setSelection(indexOf(obj.visibilityMonth4))
        filterEdit.setText(obj.mainFilter)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(obj.name)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                viewModel.updateVisibility(
                    obj,
                    visValues[marSpinner.selectedItemPosition],
                    visValues[abrSpinner.selectedItemPosition],
                    visValues[maySpinner.selectedItemPosition],
                    visValues[junSpinner.selectedItemPosition],
                    filterEdit.text.toString()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() { super.onResume(); viewModel.load() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
