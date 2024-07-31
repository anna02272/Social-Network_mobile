package com.example.socialnetwork.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.experimental.UseExperimental
import androidx.core.content.ContextCompat
import com.example.socialnetwork.R
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.dto.JwtAuthenticationRequest
import com.example.socialnetwork.model.dto.UserTokenState
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = PreferencesManager.getToken(this)
        if (!token.isNullOrEmpty()) {
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        login()
        redirectToRegister()
    }
    private fun redirectToRegister() {
        val redirectButton = findViewById<TextView>(R.id.registerRedirectText)

        redirectButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    private fun login(){
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            performLogin()
        }
    }
    private fun performLogin() {
        val usernameEditText = findViewById<EditText>(R.id.login_username)
        val passwordEditText = findViewById<EditText>(R.id.login_password)
        val errorMessageTextView = findViewById<TextView>(R.id.login_error_message)

        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        usernameEditText.background = ContextCompat.getDrawable(this, R.drawable.border)
        passwordEditText.background = ContextCompat.getDrawable(this, R.drawable.border)
        errorMessageTextView.visibility = View.GONE

        if (username.isEmpty()) {
            usernameEditText.background = ContextCompat.getDrawable(this, R.drawable.border_red)
            errorMessageTextView.text = "Username cannot be empty"
            errorMessageTextView.visibility = View.VISIBLE
            return
        }

        if (password.isEmpty()) {
            passwordEditText.background = ContextCompat.getDrawable(this, R.drawable.border_red)
            errorMessageTextView.text = "Password cannot be empty"
            errorMessageTextView.visibility = View.VISIBLE
            return
        }

        val userService = ClientUtils.userService
        val loginRequest = JwtAuthenticationRequest(username, password)
        val call = userService.createAuthenticationToken(loginRequest)

        call.enqueue(object : Callback<UserTokenState> {
            override fun onResponse(call: Call<UserTokenState>, response: Response<UserTokenState>) {
                if (response.isSuccessful) {
                    val tokenState = response.body()
                    val token = tokenState?.accessToken

                    if (token != null) {
                        PreferencesManager.saveToken(this@LoginActivity, token)
                    }

                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, PostsActivity::class.java)
                    intent.putExtra("TOKEN", token)
                    startActivity(intent)
                    finish()

                } else {
                    handleError(response)
                }
            }

            override fun onFailure(call: Call<UserTokenState>, t: Throwable) {
                errorMessageTextView.text = "Login error: ${t.message}"
                errorMessageTextView.visibility = View.VISIBLE
            }
        })
    }
    private fun handleError(response: Response<UserTokenState>) {
        val errorMessageTextView = findViewById<TextView>(R.id.login_error_message)
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        errorMessageTextView.text = "Login failed: ${errorBody}"
        errorMessageTextView.visibility = View.VISIBLE
    }
}