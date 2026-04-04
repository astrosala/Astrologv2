package com.astrolog.app.ui.wishlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolog.app.R
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.databinding.FragmentWishlistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

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
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_object, null)

        // 1. Referencias de Vistas Generales
        val nameField = dialogView.findViewById<TextInputEditText>(R.id.edit_dialog_name)
        val filterField = dialogView.findViewById<TextInputEditText>(R.id.edit_dialog_filter)
        val seasonSpinner = dialogView.findViewById<Spinner>(R.id.spinner_season_selector)
        val totalText = dialogView.findViewById<TextView>(R.id.text_ref_total_time)

        // 2. Lógica de Meses Dinámicos
        val mesesAño = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val labels = listOf(
            dialogView.findViewById<TextView>(R.id.label_month1),
            dialogView.findViewById<TextView>(R.id.label_month2),
            dialogView.findViewById<TextView>(R.id.label_month3),
            dialogView.findViewById<TextView>(R.id.label_month4)
        )

        labels.forEach { label ->
            label?.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Seleccionar mes")
                    .setItems(mesesAño) { _, which -> label.text = mesesAño[which] }
                    .show()
            }
        }

        // 3. Configurar Spinners de Visibilidad (Estrellas)
        val visValues = arrayOf("★", "✓", "~", "—")
        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        val spinners = listOf<Spinner?>(
            dialogView.findViewById(R.id.spinner_mar),
            dialogView.findViewById(R.id.spinner_abr),
            dialogView.findViewById(R.id.spinner_may),
            dialogView.findViewById(R.id.spinner_jun)
        )

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, visOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinners.forEach { it?.adapter = spinnerAdapter }

        // 4. Temporadas (Sincronización inicial)
        val seasons = viewModel.allSeasons.value ?: emptyList()
        val sAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, seasons.map { it.name })
        seasonSpinner?.adapter = sAdapter

        seasonSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (seasons.isNotEmpty()) {
                    labels[0]?.text = seasons[pos].month1
                    labels[1]?.text = seasons[pos].month2
                    labels[2]?.text = seasons[pos].month3
                    labels[3]?.text = seasons[pos].month4
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // 5. Referencias de Filtros (Carga de datos y cálculos)
        val refs = mapOf(
            "lpro" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_lpro_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_lpro_exp)),
            "ha" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_ha_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_ha_exp)),
            "oiii" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_oiii_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_oiii_exp)),
            "sii" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_sii_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_sii_exp)),
            "lext" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_lext_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_lext_exp)),
            "c1" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_c1_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_c1_exp)),
            "c2" to Pair(dialogView.findViewById<TextInputEditText>(R.id.edit_ref_c2_subs), dialogView.findViewById<TextInputEditText>(R.id.edit_ref_c2_exp))
        )

        fun updateTotals() {
            var totalSec = 0
            refs.values.forEach { (s, e) ->
                val subs = s?.text.toString().toIntOrNull() ?: 0
                val exp = e?.text.toString().toIntOrNull() ?: 0
                totalSec += (subs * exp)
            }
            totalText?.text = "Total ref: ${"%02d:%02d".format(totalSec / 3600, (totalSec % 3600) / 60)}"
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateTotals() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        refs.values.forEach { (s, e) -> 
            s?.addTextChangedListener(watcher)
            e?.addTextChangedListener(watcher)
        }

        // 6. Cargar datos existentes
        existing?.let { obj ->
            nameField?.setText(obj.name)
            filterField?.setText(obj.mainFilter)
            spinners[0]?.setSelection(visValues.indexOf(obj.visibilityMonth1).coerceAtLeast(0))
            spinners[1]?.setSelection(visValues.indexOf(obj.visibilityMonth2).coerceAtLeast(0))
            spinners[2]?.setSelection(visValues.indexOf(obj.visibilityMonth3).coerceAtLeast(0))
            spinners[3]?.setSelection(visValues.indexOf(obj.visibilityMonth4).coerceAtLeast(0))
            
            // Cargar Subs/Exp
            refs["lpro"]?.first?.setText(if(obj.refLproSubs > 0) obj.refLproSubs.toString() else "")
            refs["lpro"]?.second?.setText(if(obj.refLproExpSec > 0) obj.refLproExpSec.toString() else "")
            refs["ha"]?.first?.setText(if(obj.refHaSubs > 0) obj.refHaSubs.toString() else "")
            refs["ha"]?.second?.setText(if(obj.refHaExpSec > 0) obj.refHaExpSec.toString() else "")
            refs["oiii"]?.first?.setText(if(obj.refOiiiSubs > 0) obj.refOiiiSubs.toString() else "")
            refs["oiii"]?.second?.setText(if(obj.refOiiiExpSec > 0) obj.refOiiiExpSec.toString() else "")
            // (Añadir el resto de filtros si es necesario cargar SII, Lext, etc.)
            updateTotals()
        }

        // 7. Guardado Final
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Añadir objeto" else "Editar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val selectedSeasonPos = seasonSpinner?.selectedItemPosition ?: -1
                val sId = if (selectedSeasonPos >= 0 && seasons.isNotEmpty()) seasons[selectedSeasonPos].id else 0L

                val obj = (existing ?: AstroObject(name = "")).copy(
                    name = nameField?.text.toString(),
                    mainFilter = filterField?.text.toString(),
                    seasonId = sId,
                    visibilityMonth1 = visValues[spinners[0]?.selectedItemPosition ?: 3],
                    visibilityMonth2 = visValues[spinners[1]?.selectedItemPosition ?: 3],
                    visibilityMonth3 = visValues[spinners[2]?.selectedItemPosition ?: 3],
                    visibilityMonth4 = visValues[spinners[3]?.selectedItemPosition ?: 3],
                    refLproSubs = refs["lpro"]?.first?.text.toString().toIntOrNull() ?: 0,
                    refLproExpSec = refs["lpro"]?.second?.text.toString().toIntOrNull() ?: 0,
                    refHaSubs = refs["ha"]?.first?.text.toString().toIntOrNull() ?: 0,
                    refHaExpSec = refs["ha"]?.second?.text.toString().toIntOrNull() ?: 0,
                    refOiiiSubs = refs["oiii"]?.first?.text.toString().toIntOrNull() ?: 0,
                    refOiiiExpSec = refs["oiii"]?.second?.text.toString().toIntOrNull() ?: 0
                )
                viewModel.saveObject(obj)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAlertDialog(obj: AstroObject) {
        val mySeason = viewModel.allSeasons.value?.find { it.id == obj.seasonId }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
