package com.example.logisticsmanagement.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logisticsmanagement.databinding.FragmentDashboardBinding
import com.example.logisticsmanagement.ui.adapter.TopDistributorAdapter
import com.example.logisticsmanagement.utils.DateUtils

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var topDistributorAdapter: TopDistributorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        topDistributorAdapter = TopDistributorAdapter()
        binding.rvTopDistributors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = topDistributorAdapter
        }
    }

    private fun setupObservers() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            binding.tvTodayPallets.text = data.todayTotalPallets.toString()
            binding.tvTodayRecords.text = data.todayRecordCount.toString()
            binding.tvMonthlyPallets.text = data.monthlyTotalPallets.toString()
            binding.tvActiveDistributors.text = data.activeDistributorCount.toString()

            val currentMonth = DateUtils.getCurrentMonth()
            binding.tvMonthlyTitle.text = "${currentMonth}월 현황"

            topDistributorAdapter.submitList(data.topDistributors)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvWelcome.text = "${it.name}님, 안녕하세요!"
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}