package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edu.tlucontact.MainActivity
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.LoginActivityBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            validateAndLogin()
        }

        binding.forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        var isValid = true

        // Validate Email
        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = getString(R.string.email_required_login)
            isValid = false
        } else if (!Pattern.compile("^[a-zA-Z0-9._%+-]+@(tlu\\.edu\\.vn|e\\.tlu\\.edu\\.vn)$").matcher(email).matches()) {
            binding.emailTextInputLayout.error = getString(R.string.invalid_email_domain_login)
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        // Validate Password
        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_required_login)
            isValid = false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        if (isValid) {
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
                }
            }
    }
}