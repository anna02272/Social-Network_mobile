package com.example.socialnetwork.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username or password cannot be empty", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserTokenState>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Login error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}