package com.astrolog.app.ui.wishlist

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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
    val allSeasons: MutableLiveData<List<Season>> = MutableLiveData()

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        activeSeason.value = repo.getActiveSeason()
        repo.allSeasons.observeForever { allSeasons.value = it }
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

        // --- AQUÍ EMPIEZA EL CAMBIO ---
        // 1. Buscamos los "carteles" que creamos en el XML
        val labelM1 = dialogView.findViewById<TextView>(com.astrolog.app.R.id.label_month1)
        val labelM2 = dialogView.findViewById<TextView>(com.astrolog.app.R.id.label_month2)
        val labelM3 = dialogView.findViewById<TextView>(com.astrolog.app.R.id.label_month3)
        val labelM4 = dialogView.findViewById<TextView>(com.astrolog.app.R.id.label_month4)

        // 2. Cogemos la temporada activa y actualizamos los textos
        viewModel.activeSeason.value?.let { active ->
            labelM1?.text = active.month1
            labelM2?.text = active.month2
            labelM3?.text = active.month3
            labelM4?.text = active.month4
        }
        // --- AQUÍ TERMINA EL CAMBIO ---

        val nameField = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_dialog_name)
        val filterField = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_dialog_filter)
        val seasonSpinner = dialogView.findViewById<Spinner>(com.astrolog.app.R.id.spinner_season_selector)

        // Configurar selector de temporadas
        val seasons = viewModel.allSeasons.value ?: emptyList()
        val seasonNames = seasons.map { it.name }
        val sAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, seasonNames)
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seasonSpinner?.adapter = sAdapter

        seasonSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (seasons.isNotEmpty()) {
                    val selectedSeason = seasons[position]
                    labelM1?.text = selectedSeason.month1
                    labelM2?.text = selectedSeason.month2
                    labelM3?.text = selectedSeason.month3
                    labelM4?.text = selectedSeason.month4
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Seleccionar la temporada correspondiente si editamos o si hay una activa
        if (existing != null) {
            val index = seasons.indexOfFirst { it.id == existing.seasonId }
            if (index >= 0) seasonSpinner?.setSelection(index)
        } else {
            val activeIndex = seasons.indexOfFirst { it.id == viewModel.activeSeason.value?.id }
            if (activeIndex >= 0) seasonSpinner?.setSelection(activeIndex)
        }

        // Referencias de subs (Configuración visual igual a la anterior)
        val refLproSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lpro_subs)
        val refLproExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lpro_exp)
        val refLproTime = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_lpro_time)
        val refHaSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_ha_subs)
        val refHaExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_ha_exp)
        val refHaTime = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_ha_time)
        val refOiiiSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_oiii_subs)
        val refOiiiExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_oiii_exp)
        val refOiiiTime = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_oiii_time)
        val refSiiSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_sii_subs)
        val refSiiExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_sii_exp)
        val refSiiTime = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_sii_time)
        val refLextSubs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lext_subs)
        val refLextExp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_lext_exp)
        val refLextTime = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_lext_time)
        val refC1Subs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c1_subs)
        val refC1Exp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c1_exp)
        val refC1Time = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_c1_time)
        val refC2Subs = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c2_subs)
        val refC2Exp = dialogView.findViewById<TextInputEditText>(com.astrolog.app.R.id.edit_ref_c2_exp)
        val refC2Time = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_c2_time)
        val refTotalTime = dialogView.findViewById<TextView>(com.astrolog.app.R.id.text_ref_total_time)

        // Labels y visibilidad de filtros
        dialogView.findViewById<TextView>(com.astrolog.app.R.id.label_ref_c1)?.text = viewModel.custom1Name
        dialogView.findViewById<TextView>(com.astrolog.app.R.id.label_ref_c2)?.text = viewModel.custom2Name

        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_lpro)?.visibility = if (viewModel.showLpro) View.VISIBLE else View.GONE
        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_ha)?.visibility = if (viewModel.showHa) View.VISIBLE else View.GONE
        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_oiii)?.visibility = if (viewModel.showOiii) View.VISIBLE else View.GONE
        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_sii)?.visibility = if (viewModel.showSii) View.VISIBLE else View.GONE
        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_lext)?.visibility = if (viewModel.showLext) View.VISIBLE else View.GONE
        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_c1)?.visibility = if (viewModel.showCustom1) View.VISIBLE else View.GONE
        dialogView.findViewById<View>(com.astrolog.app.R.id.card_ref_c2)?.visibility = if (viewModel.showCustom2) View.VISIBLE else View.GONE

        // Spinners de visibilidad mensual
        val visValues = arrayOf("★", "✓", "~", "—")
        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        fun indexOf(v: String) = visValues.indexOfFirst { it == v }.takeIf { it >= 0 } ?: 3

        val spinners = listOf(
            dialogView.findViewById<Spinner>(com.astrolog.app.R.id.spinner_mar),
            dialogView.findViewById<Spinner>(com.astrolog.app.R.id.spinner_abr),
            dialogView.findViewById<Spinner>(com.astrolog.app.R.id.spinner_may),
            dialogView.findViewById<Spinner>(com.astrolog.app.R.id.spinner_jun)
        )

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, visOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinners.forEach { s ->
            s?.adapter = spinnerAdapter
            s?.setSelection(3)
        }

        existing?.let { obj ->
            nameField?.setText(obj.name)
            filterField?.setText(obj.mainFilter)
            spinners[0]?.setSelection(indexOf(obj.visibilityMonth1))
            spinners[1]?.setSelection(indexOf(obj.visibilityMonth2))
            spinners[2]?.setSelection(indexOf(obj.visibilityMonth3))
            spinners[3]?.setSelection(indexOf(obj.visibilityMonth4))
            
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

        // Lógica de cálculo de tiempos (igual que antes)
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
                // Lógica de guardado con la temporada seleccionada
                val selectedSeasonPos = seasonSpinner?.selectedItemPosition ?: -1
                val finalSeasonId = if (selectedSeasonPos >= 0 && seasons.isNotEmpty()) {
                    seasons[selectedSeasonPos].id
                } else {
                    existing?.seasonId ?: 0L
                }

                val obj = AstroObject(
                    id = existing?.id ?: 0L,
                    name = nameField?.text.toString(),
                    seasonId = finalSeasonId,
                    mainFilter = filterField?.text.toString(),
                    status = existing?.status ?: "Pendiente",
                    alertEnabled = existing?.alertEnabled ?: false,
                    alertMonths = existing?.alertMonths ?: "",
                    visibilityMonth1 = visValues[spinners[0]?.selectedItemPosition ?: 3],
                    visibilityMonth2 = visValues[spinners[1]?.selectedItemPosition ?: 3],
                    visibilityMonth3 = visValues[spinners[2]?.selectedItemPosition ?: 3],
                    visibilityMonth4 = visValues[spinners[3]?.selectedItemPosition ?: 3],
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
        // 1. Buscamos a qué temporada pertenece este objeto concreto
        val mySeason = viewModel.allSeasons.value?.find { it.id == obj.seasonId }
        
        // 2. Creamos la lista de meses usando los nombres de esa temporada
        val months = arrayOf(
            mySeason?.month1 ?: "Mes 1",
            mySeason?.month2 ?: "Mes 2",
            mySeason?.month3 ?: "Mes 3",
            mySeason?.month4 ?: "Mes 4"
        )
        
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
