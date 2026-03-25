package com.astrolog.app.ui.wishlist

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.repository.AstroRepository
import com.astrolog.app.databinding.FragmentWishlistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class WishlistViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AstroRepository

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
    }

    val allObjects = repo.allObjects

    fun addObjectWithVisibility(
        name: String, filter: String,
        mar: String, abr: String, may: String, jun: String
    ) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repo.insertObject(
            AstroObject(
                name = name, mainFilter = filter, status = "Pendiente",
                visibilityMarch = mar, visibilityApril = abr,
                visibilityMay = may, visibilityJune = jun,
                visibilityMonth1 = mar, visibilityMonth2 = abr,
                visibilityMonth3 = may, visibilityMonth4 = jun
            )
        )
    }

    fun addObject(name: String, filter: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repo.insertObject(AstroObject(name = name, mainFilter = filter, status = "Pendiente"))
    }

    fun toggleAlert(obj: AstroObject, enabled: Boolean, months: String) = viewModelScope.launch {
        repo.updateObject(obj.copy(alertEnabled = enabled, alertMonths = months))
    }

    fun cycleStatus(obj: AstroObject) = viewModelScope.launch {
        val next = when (obj.status) {
            "Pendiente" -> "En curso"
            "En curso" -> "Completado"
            else -> "Pendiente"
        }
        repo.updateObject(obj.copy(status = next))
    }

    fun deleteObject(obj: AstroObject) = viewModelScope.launch {
        repo.deleteObject(obj)
    }
}

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WishlistViewModel by viewModels()
    private lateinit var adapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WishlistAdapter(
            onStatusClick = { viewModel.cycleStatus(it) },
            onAlertClick = { obj -> showAlertDialog(obj) },
            onDeleteClick = { obj ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Eliminar objeto")
                    .setMessage("¿Eliminar ${obj.name} de la lista?")
                    .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteObject(obj) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWishlist.adapter = adapter

        viewModel.allObjects.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.fabAddObject.setOnClickListener { showAddObjectDialog() }
    }

    private fun showAddObjectDialog() {
        val dialogView = layoutInflater.inflate(com.astrolog.app.R.layout.dialog_add_object, null)
        val nameField = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_dialog_name)
        val filterField = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_dialog_filter)

        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        val visValues = arrayOf("★", "✓", "~", "—")
        val marSel = intArrayOf(3); val abrSel = intArrayOf(3)
        val maySel = intArrayOf(3); val junSel = intArrayOf(3)

        val marSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_mar)
        val abrSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_abr)
        val maySpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_may)
        val junSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_jun)

        val spinnerAdapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, visOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        listOf(marSpinner, abrSpinner, maySpinner, junSpinner).forEach { spinner ->
            spinner?.adapter = spinnerAdapter
            spinner?.setSelection(3)
        }

        marSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { marSel[0] = pos }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        abrSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { abrSel[0] = pos }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        maySpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { maySel[0] = pos }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        junSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { junSel[0] = pos }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir objeto")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val name = nameField?.text.toString()
                val filter = filterField?.text.toString()
                viewModel.addObjectWithVisibility(
                    name, filter,
                    visValues[marSel[0]], visValues[abrSel[0]],
                    visValues[maySel[0]], visValues[junSel[0]]
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAlertDialog(obj: AstroObject) {
        val months = arrayOf("Marzo", "Abril", "Mayo", "Junio")
        val currentMonths = obj.alertMonths.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val checked = months.map { it in currentMonths }.toBooleanArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alerta de visibilidad\n${obj.name}")
            .setMultiChoiceItems(months, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("Activar alerta") { _, _ ->
                val selected = months.filterIndexed { i, _ -> checked[i] }.joinToString(",")
                viewModel.toggleAlert(obj, selected.isNotEmpty(), selected)
            }
            .setNeutralButton("Desactivar") { _, _ -> viewModel.toggleAlert(obj, false, "") }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
