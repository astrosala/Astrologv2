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
import com.astrolog.app.data.repository.AstroRepository
import com.astrolog.app.databinding.FragmentCalendarBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CalendarViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AstroRepository
    val objects = MutableLiveData<List<AstroObject>>()

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
    }

    fun load() = viewModelScope.launch {
        objects.value = repo.getAllObjectsOnce()
    }

    fun updateVisibility(
        obj: AstroObject,
        mar: String, abr: String, may: String, jun: String,
        filter: String
    ) = viewModelScope.launch {
        repo.updateObject(
            obj.copy(
                visibilityMarch = mar, visibilityApril = abr,
                visibilityMay = may, visibilityJune = jun,
                mainFilter = filter
            )
        )
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
        viewModel.load()
    }

    private fun showEditDialog(obj: AstroObject) {
        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        val visValues = arrayOf("★", "✓", "~", "—")
        fun indexOf(v: String) = visValues.indexOfFirst { it == v }.takeIf { it >= 0 } ?: 3

        val dialogView = layoutInflater.inflate(
            com.astrolog.app.R.layout.dialog_edit_visibility, null
        )
        val marSpinner = dialogView.findViewById<android.widget.Spinner>(
            com.astrolog.app.R.id.spinner_edit_mar
        )
        val abrSpinner = dialogView.findViewById<android.widget.Spinner>(
            com.astrolog.app.R.id.spinner_edit_abr
        )
        val maySpinner = dialogView.findViewById<android.widget.Spinner>(
            com.astrolog.app.R.id.spinner_edit_may
        )
        val junSpinner = dialogView.findViewById<android.widget.Spinner>(
            com.astrolog.app.R.id.spinner_edit_jun
        )
        val filterEdit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.astrolog.app.R.id.edit_vis_filter
        )

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            visOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listOf(marSpinner, abrSpinner, maySpinner, junSpinner).forEach { it.adapter = spinnerAdapter }

        marSpinner.setSelection(indexOf(obj.visibilityMarch))
        abrSpinner.setSelection(indexOf(obj.visibilityApril))
        maySpinner.setSelection(indexOf(obj.visibilityMay))
        junSpinner.setSelection(indexOf(obj.visibilityJune))
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

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
