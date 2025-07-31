package com.example.logisticsmanagement.ui.distributor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.logisticsmanagement.databinding.DialogAddEditDistributorBinding
import com.example.logisticsmanagement.data.model.Distributor

class AddEditDistributorDialogFragment : DialogFragment() {

    private var _binding: DialogAddEditDistributorBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DistributorViewModel
    private var editingDistributor: Distributor? = null

    companion object {
        private const val ARG_DISTRIBUTOR = "distributor"

        fun newInstance(distributor: Distributor?): AddEditDistributorDialogFragment {
            return AddEditDistributorDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_DISTRIBUTOR, distributor)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditDistributorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // requireParentFragment() 대신 requireActivity() 사용
        viewModel = ViewModelProvider(requireActivity())[DistributorViewModel::class.java]
        editingDistributor = arguments?.getParcelable(ARG_DISTRIBUTOR)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // 분류 스피너 설정
        val categories = listOf("농산", "축산", "수산", "가공식품", "기타")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // 수정 모드인 경우 기존 데이터 설정
        editingDistributor?.let { distributor ->
            binding.etName.setText(distributor.name)
            binding.etCode.setText(distributor.code)
            binding.etAddress.setText(distributor.address)
            binding.etPhone.setText(distributor.phone)
            binding.etEmail.setText(distributor.email)
            binding.etContactPerson.setText(distributor.contactPerson)

            val categoryIndex = categories.indexOf(distributor.mainCategory)
            if (categoryIndex >= 0) {
                binding.spinnerCategory.setSelection(categoryIndex)
            }

            binding.tvTitle.text = "유통사 수정"
            binding.btnSave.text = "수정"
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveDistributor()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveDistributor() {
        val name = binding.etName.text.toString().trim()
        val code = binding.etCode.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val address = binding.etAddress.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val contactPerson = binding.etContactPerson.text.toString().trim()

        val distributor = (editingDistributor ?: Distributor()).copy(
            name = name,
            code = code,
            mainCategory = category,
            address = address,
            phone = phone,
            email = email,
            contactPerson = contactPerson
        )

        viewModel.saveDistributor(distributor)
        dismiss()
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