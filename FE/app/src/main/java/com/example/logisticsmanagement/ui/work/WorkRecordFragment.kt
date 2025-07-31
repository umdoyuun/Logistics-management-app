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

        // ⭐ 수정: Activity 스코프로 변경하여 다이얼로그와 동일한 ViewModel 인스턴스 사용
        viewModel = ViewModelProvider(requireActivity())[WorkRecordViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // ⭐ 추가: Fragment가 표시될 때마다 데이터 새로고침
        refreshData()

        Log.d(TAG, "초기화 완료")
    }

    private fun refreshData() {
        Log.d(TAG, "데이터 새로고침 시작")
        // 유통사 목록 로드
        viewModel.loadDistributors()

        // 현재 사용자의 회사 ID로 오늘의 작업 기록 로드
        viewModel.currentUser.value?.let { user ->
            viewModel.loadTodayWorkRecords(user.companyId)
        }
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

        // ⭐ 수정: saveResult 옵저버 - 다이얼로그에서 이미 처리하므로 제거
        // viewModel.saveResult는 다이얼로그에서 처리

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Log.d(TAG, "삭제 성공 - 자동 새로고침")
                        Toast.makeText(context, "작업 기록이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        // ⭐ 수정: ViewModel에서 이미 새로고침하므로 중복 호출 제거
                        // refreshData() - ViewModel의 deleteWorkRecord에서 이미 처리됨
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "삭제 실패: ${exception.message}")
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

    // ⭐ 수정: Fragment가 다시 보일 때마다 데이터 새로고침
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - 데이터 새로고침")
        // onResume에서는 굳이 새로고침하지 않음 (ViewModel이 이미 최신 상태 유지)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView 호출")
        _binding = null
    }
}