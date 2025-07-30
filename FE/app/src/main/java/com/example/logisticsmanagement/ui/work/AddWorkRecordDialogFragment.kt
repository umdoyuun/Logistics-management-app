package com.example.logisticsmanagement.ui.work

import android.app.DatePickerDialog
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddWorkRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment())[WorkRecordViewModel::class.java]

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // 날짜 설정
        binding.tvSelectedDate.text = DateUtils.formatDisplayDate(selectedDate)

        // 유통사 스피너는 옵저버에서 설정
    }

    private fun setupObservers() {
        viewModel.distributors.observe(viewLifecycleOwner) { distributors ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                distributors.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDistributor.adapter = adapter
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(context, "작업 기록이 저장되었습니다", Toast.LENGTH_SHORT).show()
                        dismiss()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "저장 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
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
        val totalPallets = binding.etTotalPallets.text.toString().toIntOrNull() ?: 0
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
