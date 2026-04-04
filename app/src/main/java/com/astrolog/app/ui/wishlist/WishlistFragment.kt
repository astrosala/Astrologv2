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
    
    // Ahora el delegado 'by viewModels' encontrará la clase correctamente
    private val viewModel: WishlistViewModel by viewModels()
    private lateinit var adapter: WishlistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WishlistAdapter(
            onStatusClick = { obj -> viewModel.cycleStatus(obj) },
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
        
        viewModel.allObjects.observe(viewLifecycleOwner) { list -> 
            adapter.submitList(list) 
        }
        
        binding.fabAddObject.setOnClickListener { showObjectDialog(null) }
    }

   private fun showObjectDialog(existing: AstroObject?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_object, null)

        val nameField = dialogView.findViewById<TextInputEditText>(R.id.edit_dialog_name)
        val filterField = dialogView.findViewById<TextInputEditText>(R.id.edit_dialog_filter)
        val seasonSpinner = dialogView.findViewById<Spinner>(R.id.spinner_season_selector)
        val totalText = dialogView.findViewById<TextView>(R.id.text_ref_total_time)

        val labels = listOf(
            dialogView.findViewById<TextView>(R.id.label_month1),
            dialogView.findViewById<TextView>(R.id.label_month2),
            dialogView.findViewById<TextView>(R.id.label_month3),
            dialogView.findViewById<TextView>(R.id.label_month4)
        )

        val visValues = arrayOf("★", "✓", "~", "—")
        val visOptions = arrayOf("★ Óptimo", "✓ Buena", "~ Baja", "— No visible")
        val spinners = listOf<Spinner?>(
            dialogView.findViewById(R.id.spinner_mar),
            dialogView.findViewById(R.id.spinner_abr),
            dialogView.findViewById(R.id.spinner_may),
            dialogView.findViewById(R.id.spinner_jun)
        )

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, visOptions)
        spinners.forEach { it?.adapter = spinnerAdapter }

        val seasonsList = viewModel.allSeasons.value ?: emptyList()
        val sAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, seasonsList.map { it.name })
        seasonSpinner?.adapter = sAdapter

        // --- LÓGICA DE CARGA DE DATOS EXISTENTES ---
        if (existing != null) {
            nameField?.setText(existing.name)
            filterField?.setText(existing.mainFilter)
            
            // 1. Encontrar y seleccionar la temporada correcta en el Spinner
            val seasonIndex = seasonsList.indexOfFirst { it.id == existing.seasonId }
            if (seasonIndex >= 0) {
                seasonSpinner?.setSelection(seasonIndex)
                // Poner los nombres de los meses de esa temporada en las etiquetas
                val s = seasonsList[seasonIndex]
                labels[0]?.text = s.month1
                labels[1]?.text = s.month2
                labels[2]?.text = s.month3
                labels[3]?.text = s.month4
            }

            // 2. Cargar las estrellas/visibilidad guardadas
            spinners[0]?.setSelection(visValues.indexOf(existing.visibilityMonth1).coerceAtLeast(0))
            spinners[1]?.setSelection(visValues.indexOf(existing.visibilityMonth2).coerceAtLeast(0))
            spinners[2]?.setSelection(visValues.indexOf(existing.visibilityMonth3).coerceAtLeast(0))
            spinners[3]?.setSelection(visValues.indexOf(existing.visibilityMonth4).coerceAtLeast(0))
            
            // Aquí deberías cargar también los campos de Subs/Exp si los usas
        }

        // --- LISTENER PARA CAMBIO DE TEMPORADA (Si el usuario cambia la temporada en el diálogo) ---
        seasonSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val s = seasonsList[position]
                labels[0]?.text = s.month1
                labels[1]?.text = s.month2
                labels[2]?.text = s.month3
                labels[3]?.text = s.month4
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Añadir objeto" else "Editar ${existing.name}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val selectedPos = seasonSpinner?.selectedItemPosition ?: -1
                val sId = if (selectedPos >= 0 && seasonsList.isNotEmpty()) seasonsList[selectedPos].id else existing?.seasonId ?: 0L

                val obj = (existing ?: AstroObject(name = "")).copy(
                    name = nameField?.text.toString(),
                    mainFilter = filterField?.text.toString(),
                    seasonId = sId,
                    visibilityMonth1 = visValues[spinners[0]?.selectedItemPosition ?: 3],
                    visibilityMonth2 = visValues[spinners[1]?.selectedItemPosition ?: 3],
                    visibilityMonth3 = visValues[spinners[2]?.selectedItemPosition ?: 3],
                    visibilityMonth4 = visValues[spinners[3]?.selectedItemPosition ?: 3]
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
