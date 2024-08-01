package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.socialnetwork.R
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.dto.UserDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var errorMessageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.register_username)
        passwordEditText = findViewById(R.id.register_password)
        emailEditText = findViewById(R.id.register_email)
        firstNameEditText = findViewById(R.id.register_firstname)
        lastNameEditText = findViewById(R.id.register_lastname)
        errorMessageTextView = findViewById(R.id.register_error_message)

        register()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val redirectButton = findViewById<TextView>(R.id.loginRedirectText)
        redirectButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun register() {
        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()

        usernameEditText.background = ContextCompat.getDrawable(this, R.drawable.border_yellow)
        passwordEditText.background = ContextCompat.getDrawable(this, R.drawable.border_yellow)
        emailEditText.background = ContextCompat.getDrawable(this, R.drawable.border_yellow)
        firstNameEditText.background = ContextCompat.getDrawable(this, R.drawable.border_yellow)
        lastNameEditText.background = ContextCompat.getDrawable(this, R.drawable.border_yellow)
        errorMessageTextView.visibility = View.GONE

        return when {
            username.isEmpty() -> {
                showValidationError(usernameEditText, "Username cannot be empty")
                false
            }
            username.length < 4 -> {
                showValidationError(usernameEditText, "Username must be at least 4 characters long")
                false
            }
            password.isEmpty() -> {
                showValidationError(passwordEditText, "Password cannot be empty")
                false
            }
            password.length < 8 -> {
                showValidationError(passwordEditText, "Password must be at least 8 characters long")
                false
            }
            email.isEmpty() -> {
                showValidationError(emailEditText, "Email cannot be empty")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showValidationError(emailEditText, "Please enter a valid email address")
                false
            }
            firstName.isEmpty() -> {
                showValidationError(firstNameEditText, "First name cannot be empty")
                false
            }
            lastName.isEmpty() -> {
                showValidationError(lastNameEditText, "Last name cannot be empty")
                false
            }
            else -> true
        }
    }

    private fun showValidationError(editText: EditText, message: String) {
        editText.background = ContextCompat.getDrawable(this, R.drawable.border_red)
        errorMessageTextView.text = message
        errorMessageTextView.visibility = View.VISIBLE
    }


    private fun performRegistration() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()

        val userService = ClientUtils.userService
        val user = UserDTO(username, password, email, firstName, lastName)
        val call = userService.create(user)

        call.enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    handleError(response)
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                errorMessageTextView.text = "Registration error: ${t.message}"
                errorMessageTextView.visibility = View.VISIBLE
            }
        })
    }

    private fun handleError(response: Response<UserDTO>) {
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        errorMessageTextView.text = "Registration failed: $errorBody"
        errorMessageTextView.visibility = View.VISIBLE
    }
}
