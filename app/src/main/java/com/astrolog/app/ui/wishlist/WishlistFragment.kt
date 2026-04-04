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

        // Inicialización de Vistas
        val nameField = dialogView.findViewById<TextInputEditText>(R.id.edit_dialog_name)
        val filterField = dialogView.findViewById<TextInputEditText>(R.id.edit_dialog_filter)
        val seasonSpinner = dialogView.findViewById<Spinner>(R.id.spinner_season_selector)

        // MESES DINÁMICOS: Lógica de selección "a placer"
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
                    .setItems(mesesAño) { _, which ->
                        label.text = mesesAño[which]
                    }
                    .show()
            }
        }

        // Configurar Spinners de Visibilidad (★, ✓, ~, —)
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
        spinners.forEach { s ->
            s?.adapter = spinnerAdapter
            s?.setSelection(3) // Por defecto: No visible
        }

        // Configurar selector de temporadas (Sincronizar nombres de meses si se elige una)
        val seasons = viewModel.allSeasons.value ?: emptyList()
        val sAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, seasons.map { it.name })
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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

        // --- REFERENCIAS DE TIEMPO Y FILTROS ---
        val editLproSubs = dialogView.findViewById<TextInputEditText>(R.id.edit_ref_lpro_subs)
        val editLproExp = dialogView.findViewById<TextInputEditText>(R.id.edit_ref_lpro_exp)
        val textLproTime = dialogView.findViewById<TextView>(R.id.text_ref_lpro_time)
        // (Añadir aquí el resto de filtros Ha, OIII siguiendo el mismo patrón si quieres ver el tiempo parcial)

        val totalText = dialogView.findViewById<TextView>(R.id.text_ref_total_time)

        fun updateTotals() {
            val s1 = editLproSubs?.text.toString().toIntOrNull() ?: 0
            val e1 = editLproExp?.text.toString().toIntOrNull() ?: 0
            val totalSec = s1 * e1 // Sumar aquí el resto de filtros...
            totalText?.text = "Total ref: ${"%02d:%02d".format(totalSec / 3600, (totalSec % 3600) / 60)}"
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { updateTotals() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        editLproSubs?.addTextChangedListener(watcher)
        editLproExp?.addTextChangedListener(watcher)

        // Cargar datos si editamos
        if (existing != null) {
            nameField?.setText(existing.name)
            filterField?.setText(existing.mainFilter)
            // Aquí cargarías el resto de valores...
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Añadir objeto" else "Editar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                // Lógica de guardado simplificada
                val obj = (existing ?: AstroObject(name = "")).copy(
                    name = nameField?.text.toString(),
                    mainFilter = filterField?.text.toString(),
                    visibilityMonth1 = visValues[spinners[0]?.selectedItemPosition ?: 3]
                    // Mapear aquí el resto de campos para la DB
                )
                viewModel.saveObject(obj)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAlertDialog(obj: AstroObject) { /* Lógica de alertas */ }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
