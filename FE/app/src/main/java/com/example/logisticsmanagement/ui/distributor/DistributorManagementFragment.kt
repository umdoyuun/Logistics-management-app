package com.example.logisticsmanagement.ui.distributor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logisticsmanagement.databinding.FragmentDistributorManagementBinding
import com.example.logisticsmanagement.ui.adapter.DistributorAdapter

class DistributorManagementFragment : Fragment() {

    private var _binding: FragmentDistributorManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DistributorViewModel
    private lateinit var distributorAdapter: DistributorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDistributorManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DistributorViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        distributorAdapter = DistributorAdapter(
            onEditClick = { distributor ->
                showAddEditDialog(distributor)
            },
            onDeleteClick = { distributor ->
                viewModel.deactivateDistributor(distributor.id)
            }
        )

        binding.rvDistributors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = distributorAdapter
        }
    }

    private fun setupObservers() {
        viewModel.distributors.observe(viewLifecycleOwner) { distributors ->
            distributorAdapter.submitList(distributors)
            binding.tvEmpty.visibility = if (distributors.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(context, "유통사가 저장되었습니다", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "저장 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
                viewModel.clearResults()
            }
        }

        // 삭제 결과 Observer 추가
        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(context, "유통사가 비활성화되었습니다", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "비활성화 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
                viewModel.clearResults()
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
        binding.btnAddDistributor.setOnClickListener {
            showAddEditDialog(null)
        }

        binding.btnAddSampleData.setOnClickListener {
            viewModel.addSampleDistributors()
        }
    }

    private fun showAddEditDialog(distributor: com.example.logisticsmanagement.data.model.Distributor?) {
        AddEditDistributorDialogFragment.newInstance(distributor).show(
            parentFragmentManager,
            "AddEditDistributorDialog"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}