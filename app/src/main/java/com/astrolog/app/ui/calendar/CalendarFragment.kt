package com.astrolog.app.ui.calendar

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.repository.AstroRepository
import com.astrolog.app.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch

// ── ViewModel ──
class CalendarViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AstroRepository
    val objects = MutableLiveData<List<AstroObject>>()

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao())
    }

    fun load() = viewModelScope.launch {
        objects.value = repo.getAllObjectsOnce()
    }
}

// ── Fragment ──
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var adapter: CalendarAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CalendarAdapter()
        binding.recyclerCalendar.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCalendar.adapter = adapter

        viewModel.objects.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.load()
    }

    override fun onResume() { super.onResume(); viewModel.load() }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
