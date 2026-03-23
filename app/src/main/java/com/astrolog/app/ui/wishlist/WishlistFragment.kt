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
    private val repo: AstroRepository = run {
        val db = AstroDatabase.getDatabase(app)
        AstroRepository(db.sessionDao(), db.astroObjectDao())
    }

    val allObjects = repo.allObjects

    fun addObject(name: String, filter: String, alertMonths: String, alertEnabled: Boolean) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repo.insertObject(
            AstroObject(
                name = name,
                mainFilter = filter,
                status = "Pendiente",
                alertEnabled = alertEnabled,
                alertMonths = alertMonths
            )
        )
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir objeto")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val name = nameField?.text.toString()
                val filter = filterField?.text.toString()
                viewModel.addObject(name, filter, "", false)
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
            .setNeutralButton("Desactivar") { _, _ ->
                viewModel.toggleAlert(obj, false, "")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
