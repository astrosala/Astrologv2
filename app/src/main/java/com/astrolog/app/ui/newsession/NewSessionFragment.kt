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
import com.astrolog.app.R
import com.astrolog.app.databinding.FragmentNewSessionBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class NewSessionFragment : Fragment() {

    private var _binding: FragmentNewSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewSessionViewModel by viewModels()
    private val args: NewSessionFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si venimos de edición, cargar sesión
        if (args.sessionId > 0) {
            viewModel.loadSession(args.sessionId)
            binding.buttonSave.text = "Actualizar sesión"
        }

        // Fecha: picker de fecha (no teclado)
        binding.editDate.setOnClickListener { showDatePicker() }
        binding.editDate.isFocusable = false

        // Fecha por defecto: hoy
        if (args.sessionId <= 0) {
            val cal = Calendar.getInstance()
            binding.editDate.setText("%02d/%02d/%04d".format(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)))
        }

        // Autocompletado de objetos (offline, desde la DB)
        viewModel.objectNames.observe(viewLifecycleOwner) { names ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            binding.editObjectName.setAdapter(adapter)
        }

        // Seeing selector
        val seeingDots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4, binding.dot5)
        seeingDots.forEachIndexed { i, dot ->
            dot.setOnClickListener {
                viewModel.seeing.value = i + 1
                updateSeeingUI(seeingDots, i + 1)
            }
        }
        viewModel.seeing.observe(viewLifecycleOwner) { updateSeeingUI(seeingDots, it) }

        // ── Cálculo automático HH:MM en tiempo real ──
        fun addTimeWatcher(subsField: com.google.android.material.textfield.TextInputEditText,
                           expField: com.google.android.material.textfield.TextInputEditText) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    viewModel.lproSubs.value = binding.editLproSubs.text.toString().toIntOrNull() ?: 0
                    viewModel.lproExpSec.value = binding.editLproExp.text.toString().toIntOrNull() ?: 0
                    viewModel.haSubs.value = binding.editHaSubs.text.toString().toIntOrNull() ?: 0
                    viewModel.haExpSec.value = binding.editHaExp.text.toString().toIntOrNull() ?: 0
                    viewModel.oiiiSubs.value = binding.editOiiiSubs.text.toString().toIntOrNull() ?: 0
                    viewModel.oiiiExpSec.value = binding.editOiiiExp.text.toString().toIntOrNull() ?: 0
                    viewModel.recalcTimes()
                }
            }
            subsField.addTextChangedListener(watcher)
            expField.addTextChangedListener(watcher)
        }

        addTimeWatcher(binding.editLproSubs, binding.editLproExp)
        addTimeWatcher(binding.editHaSubs, binding.editHaExp)
        addTimeWatcher(binding.editOiiiSubs, binding.editOiiiExp)

        // Observar tiempos calculados
        viewModel.lproTime.observe(viewLifecycleOwner) { binding.textLproTime.text = it }
        viewModel.haTime.observe(viewLifecycleOwner) { binding.textHaTime.text = it }
        viewModel.oiiiTime.observe(viewLifecycleOwner) { binding.textOiiiTime.text = it }
        viewModel.totalTime.observe(viewLifecycleOwner) { binding.textTotalTime.text = "Total: $it" }

        // Cargar campos al editar
        viewModel.objectName.observe(viewLifecycleOwner) { if (it.isNotEmpty()) binding.editObjectName.setText(it) }
        viewModel.date.observe(viewLifecycleOwner) { if (it.isNotEmpty()) binding.editDate.setText(it) }
        viewModel.conditions.observe(viewLifecycleOwner) { binding.editConditions.setText(it) }
        viewModel.notes.observe(viewLifecycleOwner) { binding.editNotes.setText(it) }
        viewModel.lproSubs.observe(viewLifecycleOwner) { if (it > 0) binding.editLproSubs.setText(it.toString()) }
        viewModel.lproExpSec.observe(viewLifecycleOwner) { if (it > 0) binding.editLproExp.setText(it.toString()) }
        viewModel.haSubs.observe(viewLifecycleOwner) { if (it > 0) binding.editHaSubs.setText(it.toString()) }
        viewModel.haExpSec.observe(viewLifecycleOwner) { if (it > 0) binding.editHaExp.setText(it.toString()) }
        viewModel.oiiiSubs.observe(viewLifecycleOwner) { if (it > 0) binding.editOiiiSubs.setText(it.toString()) }
        viewModel.oiiiExpSec.observe(viewLifecycleOwner) { if (it > 0) binding.editOiiiExp.setText(it.toString()) }

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
                true -> {
                    Snackbar.make(binding.root, "Sesión guardada", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
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

    private fun updateSeeingUI(dots: List<View>, value: Int) {
        dots.forEachIndexed { i, dot -> dot.isSelected = i < value }
        binding.textSeeingValue.text = "Seeing $value/5"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
