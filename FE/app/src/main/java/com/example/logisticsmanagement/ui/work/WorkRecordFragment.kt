package com.example.logisticsmanagement.ui.work

import android.os.Bundle
import android.util.Log
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
    private val TAG = "WorkRecordFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView 호출")
        _binding = FragmentWorkRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated 호출")

        viewModel = ViewModelProvider(this)[WorkRecordViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        Log.d(TAG, "초기화 완료")
    }

    private fun setupRecyclerView() {
        workRecordAdapter = WorkRecordAdapter(
            onEditClick = { workRecord ->
                // TODO: 수정 다이얼로그 표시
                Log.d(TAG, "수정 버튼 클릭: ${workRecord.distributorName}")
            },
            onDeleteClick = { workRecord ->
                Log.d(TAG, "삭제 버튼 클릭: ${workRecord.distributorName}")
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
            Log.d(TAG, "작업 기록 리스트 업데이트: ${records.size}개")
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
            Log.d(TAG, "추가 버튼 클릭됨")
            try {
                val dialog = AddWorkRecordDialogFragment()
                Log.d(TAG, "다이얼로그 생성 완료")
                dialog.show(parentFragmentManager, "AddWorkRecordDialog")
                Log.d(TAG, "다이얼로그 표시 요청 완료")
            } catch (e: Exception) {
                Log.e(TAG, "다이얼로그 표시 중 오류", e)
                Toast.makeText(context, "다이얼로그 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView 호출")
        _binding = null
    }
}