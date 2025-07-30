package com.example.logisticsmanagement.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.logisticsmanagement.databinding.FragmentReportBinding
import com.example.logisticsmanagement.utils.DateUtils

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        setupSpinners()
        setupObservers()
        setupClickListeners()
    }

    private fun setupSpinners() {
        // 연도 스피너 (2024년부터 현재 연도까지)
        val currentYear = DateUtils.getCurrentYear()
        val years = (2024..currentYear).map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

        // 현재 연도 선택
        val currentYearIndex = years.indexOf(currentYear.toString())
        if (currentYearIndex >= 0) {
            binding.spinnerYear.setSelection(currentYearIndex)
        }

        // 월 스피너
        val months = (1..12).map { "${it}월" }
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        // 현재 월 선택
        val currentMonth = DateUtils.getCurrentMonth()
        binding.spinnerMonth.setSelection(currentMonth - 1)
    }

    private fun setupObservers() {
        viewModel.monthlySummary.observe(viewLifecycleOwner) { summary ->
            if (summary != null) {
                binding.cardSummary.visibility = View.VISIBLE
                binding.btnDownloadCsv.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE

                binding.tvSummaryTitle.text = "${summary.year}년 ${summary.month}월 집계"
                binding.tvTotalPallets.text = summary.totalPallets.toString()
                binding.tvTotalRecords.text = summary.totalRecords.toString()
                binding.tvTotalDistributors.text = summary.totalDistributors.toString()
            } else {
                binding.cardSummary.visibility = View.GONE
                binding.btnDownloadCsv.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text = "해당 월의 데이터가 없습니다"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLoadData.setOnClickListener {
            val selectedYear = binding.spinnerYear.selectedItem.toString().toInt()
            val selectedMonth = binding.spinnerMonth.selectedItemPosition + 1

            viewModel.currentUser.value?.let { user ->
                viewModel.loadMonthlySummary(user.companyId, selectedYear, selectedMonth)
            }
        }

        binding.btnDownloadCsv.setOnClickListener {
            val selectedYear = binding.spinnerYear.selectedItem.toString().toInt()
            val selectedMonth = binding.spinnerMonth.selectedItemPosition + 1

            viewModel.generateExcelReport(selectedYear, selectedMonth)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
