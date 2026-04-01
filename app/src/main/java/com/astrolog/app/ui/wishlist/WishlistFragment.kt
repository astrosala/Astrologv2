package com.astrolog.app.ui.wishlist

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private val prefs = app.getSharedPreferences("astrolog_prefs", Context.MODE_PRIVATE)

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
    }

    val allObjects = repo.allObjects

    val showLpro get() = prefs.getBoolean("show_lpro", true)
    val showHa get() = prefs.getBoolean("show_ha", true)
    val showOiii get() = prefs.getBoolean("show_oiii", true)
    val showSii get() = prefs.getBoolean("show_sii", false)
    val showLext get() = prefs.getBoolean("show_lext", false)
    val showCustom1 get() = prefs.getBoolean("show_custom1", false)
    val showCustom2 get() = prefs.getBoolean("show_custom2", false)
    val custom1Name get() = prefs.getString("custom1_name", "Filtro 1") ?: "Filtro 1"
    val custom2Name get() = prefs.getString("custom2_name", "Filtro 2") ?: "Filtro 2"

    fun saveObject(obj: AstroObject) = viewModelScope.launch {
        if (obj.name.isBlank()) return@launch
        if (obj.id == 0L) repo.insertObject(obj)
        else repo.updateObject(obj)
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
            onEditClick = { obj -> showObjectDialog(obj) },
            onAlertClick = { obj -> showAlertDialog(obj) },
            onDeleteClick = { obj ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Eliminar objeto")
                    .setMessage("¿Eliminar ${obj.name}?")
                    .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteObject(obj) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWishlist.adapter = adapter
        viewModel.allObjects.observe(viewLifecycleOwner) { adapter.submitList(it) }
        binding.fabAddObject.setOnClickListener { showObjectDialog(null) }
    }

    private fun showObjectDialog(existing: AstroObject?) {
        val dialogView = layoutInflater.inflate(
            com.astrolog.app.R.layout.dialog_add_object, null
        )

        val nameField = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_dialog_name)
        val filterField = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_dialog_filter)

        // Referencias de subs
        val refLproSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lpro_subs)
        val refLproExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lpro_exp)
        val refLproTime = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_lpro_time)
        val refHaSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_ha_subs)
        val refHaExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_ha_exp)
        val refHaTime = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_ha_time)
        val refOiiiSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_oiii_subs)
        val refOiiiExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_oiii_exp)
        val refOiiiTime = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_oiii_time)
        val refSiiSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_sii_subs)
        val refSiiExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_sii_exp)
        val refSiiTime = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_sii_time)
        val refLextSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lext_subs)
        val refLextExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lext_exp)
        val refLextTime = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_lext_time)
        val refC1Subs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c1_subs)
        val refC1Exp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c1_exp)
        val refC1Time = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_c1_time)
        val refC2Subs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c2_subs)
        val refC2Exp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c2_exp)
        val refC2Time = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_c2_time)
        val refTotalTime = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.text_ref_total_time)

        // Labels filtros personalizados
        val labelC1 = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.label_ref_c1)
        val labelC2 = dialogView.findViewById<android.widget.TextView>(com.astrolog.app.R.id.label_ref_c2)
        labelC1?.text = viewModel.custom1Name
        labelC2?.text = viewModel.custom2Name

        // Mostrar/ocultar según ajustes
        val cardRefLpro = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_lpro)
        val cardRefHa = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_ha)
        val cardRefOiii = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_oiii)
        val cardRefSii = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_sii)
        val cardRefLext = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_lext)
        val cardRefC1 = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_c1)
        val cardRefC2 = dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_c2)

        cardRefLpro?.visibility = if (viewModel.showLpro) View.VISIBLE else View.GONE
        cardRefHa?.visibility = if (viewModel.showHa) View.VISIBLE else View.GONE
        cardRefOiii?.visibility = if (viewModel.showOiii) View.VISIBLE else View.GONE
        cardRefSii?.visibility = if (viewModel.showSii) View.VISIBLE else View.GONE
        cardRefLext?.visibility = if (viewModel.showLext) View.VISIBLE else View.GONE
        cardRefC1?.visibility = if (viewModel.showCustom1) View.VISIBLE else View.GONE
        cardRefC2?.visibility = if (viewModel.showCustom2) View.VISIBLE else View.GONE

        // Visibilidad spinners
        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        val visValues = arrayOf("★", "✓", "~", "—")
        fun indexOf(v: String) = visValues.indexOfFirst { it == v }.takeIf { it >= 0 } ?: 3

        val marSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_mar)
        val abrSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_abr)
        val maySpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_may)
        val junSpinner = dialogView.findViewById<android.widget.Spinner>(com.astrolog.app.R.id.spinner_jun)

        val spinnerAdapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, visOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listOf(marSpinner, abrSpinner, maySpinner, junSpinner).forEach {
            it?.adapter = spinnerAdapter
            it?.setSelection(3)
        }

        // Si editamos, rellenar campos
        existing?.let { obj ->
            nameField?.setText(obj.name)
            filterField?.setText(obj.mainFilter)
            marSpinner?.setSelection(indexOf(obj.visibilityMonth1))
            abrSpinner?.setSelection(indexOf(obj.visibilityMonth2))
            maySpinner?.setSele
