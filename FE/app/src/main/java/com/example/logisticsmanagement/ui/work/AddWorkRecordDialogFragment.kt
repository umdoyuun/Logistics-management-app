package com.example.logisticsmanagement.ui.work

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.logisticsmanagement.databinding.DialogAddWorkRecordBinding
import com.example.logisticsmanagement.data.model.request.WorkRecordRequest
import com.example.logisticsmanagement.data.model.WorkItem
import com.example.logisticsmanagement.data.model.WorkUnit
import com.example.logisticsmanagement.utils.DateUtils
import java.util.*

class AddWorkRecordDialogFragment : DialogFragment() {

    private var _binding: DialogAddWorkRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkRecordViewModel
    private var selectedDate = Date()
    private val TAG = "AddWorkRecordDialog"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView 호출")
        _binding = DialogAddWorkRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated 호출")

        try {
            // requireParentFragment() 대신 requireActivity() 사용
            viewModel = ViewModelProvider(requireActivity())[WorkRecordViewModel::class.java]
            Log.d(TAG, "ViewModel 생성 성공")

            setupUI()
            setupObservers()
            setupClickListeners()
            Log.d(TAG, "초기화 완료")
        } catch (e: Exception) {
            Log.e(TAG, "초기화 중 오류", e)
            Toast.makeText(context, "다이얼로그 초기화 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        // 날짜 설정
        binding.tvSelectedDate.text = DateUtils.formatDisplayDate(selectedDate)

        // 유통사 스피너는 옵저버에서 설정
    }

    private fun setupObservers() {
        Log.d(TAG, "Observer 설정 시작")

        viewModel.distributors.observe(viewLifecycleOwner) { distributors ->
            Log.d(TAG, "유통사 목록 업데이트: ${distributors.size}개")
            if (distributors.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    distributors.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDistributor.adapter = adapter
            } else {
                Toast.makeText(context, "유통사 목록이 비어있습니다. 먼저 유통사를 추가해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        // saveResult 관찰 - 한 번만 처리하고 다이얼로그 닫기
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Log.d(TAG, "저장 결과 받음: ${it.isSuccess}")
                it.fold(
                    onSuccess = {
                        Log.d(TAG, "저장 성공, 다이얼로그 닫기")
                        Toast.makeText(context, "작업 기록이 저장되었습니다", Toast.LENGTH_SHORT).show()
                        dismiss()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "저장 실패: ${exception.message}")
                        Toast.makeText(context, "저장 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
                // 처리 후 즉시 결과 클리어 (중요!)
                viewModel.clearResults()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Log.d(TAG, "에러 메시지: $message")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        Log.d(TAG, "Observer 설정 완료")
    }

    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveWorkRecord()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { time = selectedDate }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                binding.tvSelectedDate.text = DateUtils.formatDisplayDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveWorkRecord() {
        val distributors = viewModel.distributors.value ?: return
        val selectedDistributorIndex = binding.spinnerDistributor.selectedItemPosition

        if (selectedDistributorIndex < 0 || selectedDistributorIndex >= distributors.size) {
            Toast.makeText(context, "유통사를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDistributor = distributors[selectedDistributorIndex]
        val totalPalletsText = binding.etTotalPallets.text.toString().trim()

        if (totalPalletsText.isEmpty()) {
            Toast.makeText(context, "총 파렛트 수를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPallets = totalPalletsText.toIntOrNull() ?: 0
        if (totalPallets <= 0) {
            Toast.makeText(context, "파렛트 수는 0보다 커야 합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val notes = binding.etNotes.text.toString().trim()

        // 간단한 품목 입력 (선택사항)
        val items = mutableListOf<WorkItem>()
        val itemName = binding.etItemName.text.toString().trim()
        val itemQuantity = binding.etItemQuantity.text.toString().toIntOrNull() ?: 0

        if (itemName.isNotEmpty() && itemQuantity > 0) {
            items.add(
                WorkItem(
                    itemName = itemName,
                    quantity = itemQuantity,
                    unit = WorkUnit.PALLET,
                    category = selectedDistributor.mainCategory,
                    notes = ""
                )
            )
        }

        val request = WorkRecordRequest(
            distributorId = selectedDistributor.id,
            totalPallets = totalPallets,
            items = items,
            workDate = selectedDate,
            notes = notes
        )

        viewModel.saveWorkRecord(request)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}