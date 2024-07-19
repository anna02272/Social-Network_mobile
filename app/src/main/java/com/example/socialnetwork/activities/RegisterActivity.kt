package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.R

class RegisterActivity : AppCompatActivity() {
    override fun onBackPressed() {
        return
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val createPostButton = findViewById<TextView>(R.id.loginRedirectText)

        createPostButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}