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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Season
import com.astrolog.app.data.repository.AstroRepository
import com.astrolog.app.databinding.FragmentWishlistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class WishlistViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AstroRepository
    private val prefs = app.getSharedPreferences("astrolog_prefs", Context.MODE_PRIVATE)

    val activeSeason = MutableLiveData<Season?>()

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
        loadActiveSeason()
    }

    private fun loadActiveSeason() = viewModelScope.launch {
        activeSeason.value = repo.getActiveSeason()
    }

    val allSeasons = repo.allSeasons

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

        binding.recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())

        // Observamos las temporadas. Cuando lleguen, creamos el adaptador.
        viewModel.allSeasons.observe(viewLifecycleOwner) { seasonsList ->
            
            adapter = WishlistAdapter(
                seasons = seasonsList,
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
            binding.recyclerWishlist.adapter = adapter
            
            // Si ya hay objetos, los metemos en la lista
            viewModel.allObjects.value?.let { adapter.submitList(it) }
        }

        viewModel.allObjects.observe(viewLifecycleOwner) { lista ->
            if (::adapter.isInitialized) {
                adapter.submitList(lista)
            }
        }

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
            maySpinner?.setSelection(indexOf(obj.visibilityMonth3))
            junSpinner?.setSelection(indexOf(obj.visibilityMonth4))
            if (obj.refLproSubs > 0) refLproSubs?.setText(obj.refLproSubs.toString())
            if (obj.refLproExpSec > 0) refLproExp?.setText(obj.refLproExpSec.toString())
            if (obj.refHaSubs > 0) refHaSubs?.setText(obj.refHaSubs.toString())
            if (obj.refHaExpSec > 0) refHaExp?.setText(obj.refHaExpSec.toString())
            if (obj.refOiiiSubs > 0) refOiiiSubs?.setText(obj.refOiiiSubs.toString())
            if (obj.refOiiiExpSec > 0) refOiiiExp?.setText(obj.refOiiiExpSec.toString())
            if (obj.refSiiSubs > 0) refSiiSubs?.setText(obj.refSiiSubs.toString())
            if (obj.refSiiExpSec > 0) refSiiExp?.setText(obj.refSiiExpSec.toString())
            if (obj.refLextSubs > 0) refLextSubs?.setText(obj.refLextSubs.toString())
            if (obj.refLextExpSec > 0) refLextExp?.setText(obj.refLextExpSec.toString())
            if (obj.refCustom1Subs > 0) refC1Subs?.setText(obj.refCustom1Subs.toString())
            if (obj.refCustom1ExpSec > 0) refC1Exp?.setText(obj.refCustom1ExpSec.toString())
            if (obj.refCustom2Subs > 0) refC2Subs?.setText(obj.refCustom2Subs.toString())
            if (obj.refCustom2ExpSec > 0) refC2Exp?.setText(obj.refCustom2ExpSec.toString())
        }

        // Cálculo automático HH:MM de referencia
        fun formatTime(subs: Int, exp: Int): String {
            val sec = subs * exp
            if (sec == 0) return "00:00"
            return "%02d:%02d".format(sec / 3600, (sec % 3600) / 60)
        }

        fun recalcRef() {
            val ls = refLproSubs?.text.toString().toIntOrNull() ?: 0
            val le = refLproExp?.text.toString().toIntOrNull() ?: 0
            val hs = refHaSubs?.text.toString().toIntOrNull() ?: 0
            val he = refHaExp?.text.toString().toIntOrNull() ?: 0
            val os = refOiiiSubs?.text.toString().toIntOrNull() ?: 0
            val oe = refOiiiExp?.text.toString().toIntOrNull() ?: 0
            val ss = refSiiSubs?.text.toString().toIntOrNull() ?: 0
            val se = refSiiExp?.text.toString().toIntOrNull() ?: 0
            val xs = refLextSubs?.text.toString().toIntOrNull() ?: 0
            val xe = refLextExp?.text.toString().toIntOrNull() ?: 0
            val c1s = refC1Subs?.text.toString().toIntOrNull() ?: 0
            val c1e = refC1Exp?.text.toString().toIntOrNull() ?: 0
            val c2s = refC2Subs?.text.toString().toIntOrNull() ?: 0
            val c2e = refC2Exp?.text.toString().toIntOrNull() ?: 0
            refLproTime?.text = formatTime(ls, le)
            refHaTime?.text = formatTime(hs, he)
            refOiiiTime?.text = formatTime(os, oe)
            refSiiTime?.text = formatTime(ss, se)
            refLextTime?.text = formatTime(xs, xe)
            refC1Time?.text = formatTime(c1s, c1e)
            refC2Time?.text = formatTime(c2s, c2e)
            val totalSec = ls*le + hs*he + os*oe + ss*se + xs*xe + c1s*c1e + c2s*c2e
            refTotalTime?.text = "Total ref: ${"%02d:%02d".format(totalSec / 3600, (totalSec % 3600) / 60)}"
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { recalcRef() }
        }
        listOf(refLproSubs, refLproExp, refHaSubs, refHaExp, refOiiiSubs, refOiiiExp,
               refSiiSubs, refSiiExp, refLextSubs, refLextExp, refC1Subs, refC1Exp,
               refC2Subs, refC2Exp).forEach { it?.addTextChangedListener(watcher) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Añadir objeto" else "Editar ${existing.name}")
            .setView(dialogView)
            .setPositiveButton(if (existing == null) "Añadir" else "Guardar") { _, _ ->
                // Asignar seasonId de la temporada activa al crear objeto nuevo
                val seasonId = if (existing == null) {
                    viewModel.activeSeason.value?.id ?: 0L
                } else {
                    existing.seasonId
                }
                val obj = AstroObject(
                    id = existing?.id ?: 0L,
                    name = nameField?.text.toString(),
                    seasonId = seasonId,
                    mainFilter = filterField?.text.toString(),
                    status = existing?.status ?: "Pendiente",
                    alertEnabled = existing?.alertEnabled ?: false,
                    alertMonths = existing?.alertMonths ?: "",
                    visibilityMonth1 = visValues[marSpinner?.selectedItemPosition ?: 3],
                    visibilityMonth2 = visValues[abrSpinner?.selectedItemPosition ?: 3],
                    visibilityMonth3 = visValues[maySpinner?.selectedItemPosition ?: 3],
                    visibilityMonth4 = visValues[junSpinner?.selectedItemPosition ?: 3],
                    visibilityMarch = visValues[marSpinner?.selectedItemPosition ?: 3],
                    visibilityApril = visValues[abrSpinner?.selectedItemPosition ?: 3],
                    visibilityMay = visValues[maySpinner?.selectedItemPosition ?: 3],
                    visibilityJune = visValues[junSpinner?.selectedItemPosition ?: 3],
                    refLproSubs = refLproSubs?.text.toString().toIntOrNull() ?: 0,
                    refLproExpSec = refLproExp?.text.toString().toIntOrNull() ?: 0,
                    refHaSubs = refHaSubs?.text.toString().toIntOrNull() ?: 0,
                    refHaExpSec = refHaExp?.text.toString().toIntOrNull() ?: 0,
                    refOiiiSubs = refOiiiSubs?.text.toString().toIntOrNull() ?: 0,
                    refOiiiExpSec = refOiiiExp?.text.toString().toIntOrNull() ?: 0,
                    refSiiSubs = refSiiSubs?.text.toString().toIntOrNull() ?: 0,
                    refSiiExpSec = refSiiExp?.text.toString().toIntOrNull() ?: 0,
                    refLextSubs = refLextSubs?.text.toString().toIntOrNull() ?: 0,
                    refLextExpSec = refLextExp?.text.toString().toIntOrNull() ?: 0,
                    refCustom1Subs = refC1Subs?.text.toString().toIntOrNull() ?: 0,
                    refCustom1ExpSec = refC1Exp?.text.toString().toIntOrNull() ?: 0,
                    refCustom2Subs = refC2Subs?.text.toString().toIntOrNull() ?: 0,
                    refCustom2ExpSec = refC2Exp?.text.toString().toIntOrNull() ?: 0
                )
                viewModel.saveObject(obj)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAlertDialog(obj: AstroObject) {
        val months = arrayOf("Marzo", "Abril", "Mayo", "Junio")
        val currentMonths = obj.alertMonths.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val checked = months.map { it in currentMonths }.toBooleanArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alerta — ${obj.name}")
            .setMultiChoiceItems(months, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("Activar") { _, _ ->
                val selected = months.filterIndexed { i, _ -> checked[i] }.joinToString(",")
                viewModel.toggleAlert(obj, selected.isNotEmpty(), selected)
            }
            .setNeutralButton("Desactivar") { _, _ -> viewModel.toggleAlert(obj, false, "") }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
