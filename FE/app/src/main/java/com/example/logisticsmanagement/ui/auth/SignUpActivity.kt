package com.example.logisticsmanagement.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.logisticsmanagement.R
import com.example.logisticsmanagement.data.model.User
import com.example.logisticsmanagement.data.model.UserRole
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        val spinnerRole = findViewById<Spinner>(R.id.spinnerRole)
        val roleAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            UserRole.values().map { it.displayName }
        )
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter
    }

    private fun setupObservers() {
        viewModel.signUpResult.observe(this) { result ->
            result?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onFailure = { exception ->
                        Toast.makeText(this, "회원가입 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            val btnSignUp = findViewById<Button>(R.id.btnSignUp)

            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSignUp.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val etName = findViewById<TextInputEditText>(R.id.etName)
            val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
            val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
            val etPosition = findViewById<TextInputEditText>(R.id.etPosition)
            val etPhoneNumber = findViewById<TextInputEditText>(R.id.etPhoneNumber)
            val spinnerRole = findViewById<Spinner>(R.id.spinnerRole)

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val position = etPosition.text.toString().trim()
            val phoneNumber = etPhoneNumber.text.toString().trim()
            val selectedRole = UserRole.values()[spinnerRole.selectedItemPosition]

            val user = User(
                name = name,
                email = email,
                position = position,
                phoneNumber = phoneNumber,
                role = selectedRole,
                companyId = "logistics_center_seoul_001"
            )

            viewModel.signUp(email, password, user)
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}