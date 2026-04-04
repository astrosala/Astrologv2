package com.astrolog.app.ui.newsession

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.astrolog.app.databinding.FragmentNewSessionBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class NewSessionFragment : Fragment() {

    private var _binding: FragmentNewSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewSessionViewModel by viewModels()
    private val args: NewSessionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Edición
        if (args.sessionId > 0) {
            viewModel.loadSession(args.sessionId.toLong())
            binding.buttonSave.text = "Actualizar sesión"
        }

        // Mostrar filtros configurables según Ajustes
        binding.cardLpro.visibility = if (viewModel.showLpro) View.VISIBLE else View.GONE
        binding.cardHa.visibility = if (viewModel.showHa) View.VISIBLE else View.GONE
        binding.cardOiii.visibility = if (viewModel.showOiii) View.VISIBLE else View.GONE
        binding.cardSii.visibility = if (viewModel.showSii) View.VISIBLE else View.GONE
        binding.cardLext.visibility = if (viewModel.showLext) View.VISIBLE else View.GONE
        binding.cardCustom1.visibility = if (viewModel.showCustom1) View.VISIBLE else View.GONE
        binding.cardCustom2.visibility = if (viewModel.showCustom2) View.VISIBLE else View.GONE
        binding.textCustom1Label.text = viewModel.custom1Name
        binding.textCustom2Label.text = viewModel.custom2Name

        // Fecha
        binding.editDate.setOnClickListener { showDatePicker() }
        binding.editDate.isFocusable = false
        if (args.sessionId <= 0) {
            val cal = Calendar.getInstance()
            binding.editDate.setText("%02d/%02d/%04d".format(
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)
            ))
        }

      // --- NUEVO CÓDIGO PARA EL DESPLEGABLE ---
val prefs = requireContext().getSharedPreferences("astrolog_prefs", android.content.Context.MODE_PRIVATE)
val activeSeasonId = prefs.getLong("active_season_id", 0L)

viewModel.allObjects.observe(viewLifecycleOwner) { listaDeObjetos ->
    // Filtramos para que solo salgan objetos de la temporada elegida en ajustes
    val objetosFiltrados = listaDeObjetos.filter { it.seasonId == activeSeasonId }
    val nombres = objetosFiltrados.map { it.name }
    
    val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    binding.spinnerObjectName.adapter = adapter
}
// -----------------------------------------

        // Seeing
        val seeingDots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4, binding.dot5)
        seeingDots.forEachIndexed { i, dot -> dot.setOnClickListener { viewModel.seeing.value = i + 1 } }
        viewModel.seeing.observe(viewLifecycleOwner) { value ->
            seeingDots.forEachIndexed { i, dot -> dot.isSelected = i < value }
            binding.textSeeingValue.text = "Seeing $value/5"
        }

        // TextWatcher para recalcular HH:MM en tiempo real
        val recalcWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val b = _binding ?: return
                viewModel.lproSubs.value = b.editLproSubs.text.toString().toIntOrNull() ?: 0
                viewModel.lproExpSec.value = b.editLproExp.text.toString().toIntOrNull() ?: 0
                viewModel.haSubs.value = b.editHaSubs.text.toString().toIntOrNull() ?: 0
                viewModel.haExpSec.value = b.editHaExp.text.toString().toIntOrNull() ?: 0
                viewModel.oiiiSubs.value = b.editOiiiSubs.text.toString().toIntOrNull() ?: 0
                viewModel.oiiiExpSec.value = b.editOiiiExp.text.toString().toIntOrNull() ?: 0
                viewModel.siiSubs.value = b.editSiiSubs.text.toString().toIntOrNull() ?: 0
                viewModel.siiExpSec.value = b.editSiiExp.text.toString().toIntOrNull() ?: 0
                viewModel.lextSubs.value = b.editLextSubs.text.toString().toIntOrNull() ?: 0
                viewModel.lextExpSec.value = b.editLextExp.text.toString().toIntOrNull() ?: 0
                viewModel.custom1Subs.value = b.editCustom1Subs.text.toString().toIntOrNull() ?: 0
                viewModel.custom1ExpSec.value = b.editCustom1Exp.text.toString().toIntOrNull() ?: 0
                viewModel.custom2Subs.value = b.editCustom2Subs.text.toString().toIntOrNull() ?: 0
                viewModel.custom2ExpSec.value = b.editCustom2Exp.text.toString().toIntOrNull() ?: 0
                viewModel.recalcTimes()
            }
        }

        listOf(
            binding.editLproSubs, binding.editLproExp,
            binding.editHaSubs, binding.editHaExp,
            binding.editOiiiSubs, binding.editOiiiExp,
            binding.editSiiSubs, binding.editSiiExp,
            binding.editLextSubs, binding.editLextExp,
            binding.editCustom1Subs, binding.editCustom1Exp,
            binding.editCustom2Subs, binding.editCustom2Exp
        ).forEach { it.addTextChangedListener(recalcWatcher) }

        // Observar tiempos
        viewModel.lproTime.observe(viewLifecycleOwner) { binding.textLproTime.text = it }
        viewModel.haTime.observe(viewLifecycleOwner) { binding.textHaTime.text = it }
        viewModel.oiiiTime.observe(viewLifecycleOwner) { binding.textOiiiTime.text = it }
        viewModel.siiTime.observe(viewLifecycleOwner) { binding.textSiiTime.text = it }
        viewModel.lextTime.observe(viewLifecycleOwner) { binding.textLextTime.text = it }
        viewModel.custom1Time.observe(viewLifecycleOwner) { binding.textCustom1Time.text = it }
        viewModel.custom2Time.observe(viewLifecycleOwner) { binding.textCustom2Time.text = it }
        viewModel.totalTime.observe(viewLifecycleOwner) { binding.textTotalTime.text = "Total: $it" }

        // Rellenar al editar
        viewModel.objectName.observe(viewLifecycleOwner) { if (it.isNotEmpty() && binding.editObjectName.text.isNullOrEmpty()) binding.editObjectName.setText(it) }
        viewModel.date.observe(viewLifecycleOwner) { if (it.isNotEmpty() && binding.editDate.text.isNullOrEmpty()) binding.editDate.setText(it) }
        viewModel.conditions.observe(viewLifecycleOwner) { if (it.isNotEmpty() && binding.editConditions.text.isNullOrEmpty()) binding.editConditions.setText(it) }
        viewModel.notes.observe(viewLifecycleOwner) { if (it.isNotEmpty() && binding.editNotes.text.isNullOrEmpty()) binding.editNotes.setText(it) }
        viewModel.lproSubs.observe(viewLifecycleOwner) { if (it > 0 && binding.editLproSubs.text.isNullOrEmpty()) binding.editLproSubs.setText(it.toString()) }
        viewModel.lproExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editLproExp.text.isNullOrEmpty()) binding.editLproExp.setText(it.toString()) }
        viewModel.haSubs.observe(viewLifecycleOwner) { if (it > 0 && binding.editHaSubs.text.isNullOrEmpty()) binding.editHaSubs.setText(it.toString()) }
        viewModel.haExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editHaExp.text.isNullOrEmpty()) binding.editHaExp.setText(it.toString()) }
        viewModel.oiiiSubs.observe(viewLifecycleOwner) { if (it > 0 && binding.editOiiiSubs.text.isNullOrEmpty()) binding.editOiiiSubs.setText(it.toString()) }
        viewModel.oiiiExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editOiiiExp.text.isNullOrEmpty()) binding.editOiiiExp.setText(it.toString()) }
        viewModel.siiSubs.observe(viewLifecycleOwner) { if (it > 0 && binding.editSiiSubs.text.isNullOrEmpty()) binding.editSiiSubs.setText(it.toString()) }
        viewModel.siiExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editSiiExp.text.isNullOrEmpty()) binding.editSiiExp.setText(it.toString()) }
        viewModel.lextSubs.observe(viewLifecycleOwner) { if (it > 0 && binding.editLextSubs.text.isNullOrEmpty()) binding.editLextSubs.setText(it.toString()) }
        viewModel.lextExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editLextExp.text.isNullOrEmpty()) binding.editLextExp.setText(it.toString()) }
        viewModel.custom1Subs.observe(viewLifecycleOwner) { if (it > 0 && binding.editCustom1Subs.text.isNullOrEmpty()) binding.editCustom1Subs.setText(it.toString()) }
        viewModel.custom1ExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editCustom1Exp.text.isNullOrEmpty()) binding.editCustom1Exp.setText(it.toString()) }
        viewModel.custom2Subs.observe(viewLifecycleOwner) { if (it > 0 && binding.editCustom2Subs.text.isNullOrEmpty()) binding.editCustom2Subs.setText(it.toString()) }
        viewModel.custom2ExpSec.observe(viewLifecycleOwner) { if (it > 0 && binding.editCustom2Exp.text.isNullOrEmpty()) binding.editCustom2Exp.setText(it.toString()) }

        // Guardar
        binding.buttonSave.setOnClickListener {
            viewModel.objectName.value = binding.editObjectName.text.toString()
            viewModel.date.value = binding.editDate.text.toString()
            viewModel.conditions.value = binding.editConditions.text.toString()
            viewModel.notes.value = binding.editNotes.text.toString()
            viewModel.saveSession()
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                true -> { Snackbar.make(binding.root, "Sesión guardada", Snackbar.LENGTH_SHORT).show(); findNavController().popBackStack() }
                false -> Snackbar.make(binding.root, "Completa objeto y fecha", Snackbar.LENGTH_SHORT).show()
                null -> {}
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            binding.editDate.setText("%02d/%02d/%04d".format(day, month + 1, year))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
