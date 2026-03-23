package com.astrolog.app.ui.stats

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.astrolog.app.databinding.FragmentStatsBinding

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()
    private lateinit var summaryAdapter: ObjectSummaryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        summaryAdapter = ObjectSummaryAdapter()
        binding.recyclerSummary.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSummary.adapter = summaryAdapter

        viewModel.stats.observe(viewLifecycleOwner) { data ->
            binding.textTotalSessions.text = data.totalSessions.toString()
            binding.textTotalTime.text = data.totalTime
            binding.textTotalSubs.text = data.totalSubs.toString()
            binding.textAvgSeeing.text = "%.1f".format(data.avgSeeing)
            binding.textLproTime.text = data.lproTime
            binding.textHaTime.text = data.haTime
            binding.textOiiiTime.text = data.oiiiTime
            summaryAdapter.submitList(data.summaryByObject)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
