package com.astrolog.app.ui.sessions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astrolog.app.R
import com.astrolog.app.databinding.FragmentSessionsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SessionsFragment : Fragment() {

    private var _binding: FragmentSessionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SessionsViewModel by viewModels()
    private lateinit var adapter: SessionsAdapter

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.importFromExcel(uri) { count ->
                    Toast.makeText(requireContext(), "$count sesiones importadas", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val exportXlsxLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.exportToExcel(uri) { ok ->
                    val msg = if (ok) "Excel exportado correctamente" else "Error al exportar"
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val exportCsvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.exportToCsv(uri) { ok ->
                    val msg = if (ok) "CSV exportado correctamente" else "Error al exportar"
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Conectar toolbar con la Activity para que aparezca el menú
        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        // RecyclerView
        adapter = SessionsAdapter { session ->
            val action = SessionsFragmentDirections.actionSessionsToNewSession(session.id.toInt())
            findNavController().navigate(action)
        }
        binding.recyclerSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSessions.adapter = adapter

        // Swipe para eliminar
        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val session = adapter.getSessionAt(vh.adapterPosition)
                viewModel.deleteSession(session)
                Snackbar.make(binding.root, "Sesión eliminada", Snackbar.LENGTH_LONG).show()
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(binding.recyclerSessions)

        viewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            adapter.submitList(sessions)
            binding.textEmpty.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabNewSession.setOnClickListener {
            findNavController().navigate(R.id.action_sessions_to_newSession)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sessions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> { showImportDialog(); true }
            R.id.action_export -> { showExportDialog(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showImportDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Importar desde Excel")
            .setMessage("Selecciona tu archivo Excel de control de sesiones. Las sesiones ya registradas no se duplicarán.")
            .setPositiveButton("Seleccionar archivo") { _, _ ->
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                }
                importLauncher.launch(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showExportDialog() {
        val options = arrayOf("Exportar como Excel (.xlsx)", "Exportar como CSV")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exportar datos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            putExtra(Intent.EXTRA_TITLE, "astrolog_sesiones.xlsx")
                        }
                        exportXlsxLauncher.launch(intent)
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TITLE, "astrolog_sesiones.csv")
                        }
                        exportCsvLauncher.launch(intent)
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
