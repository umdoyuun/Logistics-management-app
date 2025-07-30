package com.example.logisticsmanagement.ui.work

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logisticsmanagement.databinding.FragmentWorkRecordBinding
import com.example.logisticsmanagement.ui.adapter.WorkRecordAdapter

class WorkRecordFragment : Fragment() {

    private var _binding: FragmentWorkRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkRecordViewModel
    private lateinit var workRecordAdapter: WorkRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[WorkRecordViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        workRecordAdapter = WorkRecordAdapter(
            onEditClick = { workRecord ->
                // TODO: 수정 다이얼로그 표시
            },
            onDeleteClick = { workRecord ->
                viewModel.deleteWorkRecord(workRecord)
            }
        )

        binding.rvWorkRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workRecordAdapter
        }
    }

    private fun setupObservers() {
        viewModel.workRecords.observe(viewLifecycleOwner) { records ->
            workRecordAdapter.submitList(records)
            binding.tvEmpty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(context, "작업 기록이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "저장 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
                viewModel.clearResults()
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(context, "작업 기록이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "삭제 실패: ${exception.message}", Toast.LENGTH_LONG).show()
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
        binding.btnAddRecord.setOnClickListener {
            AddWorkRecordDialogFragment().show(
                parentFragmentManager,
                "AddWorkRecordDialog"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}