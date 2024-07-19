package com.example.socialnetwork.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.socialnetwork.R

class LoginActivity : AppCompatActivity() {
    override fun onBackPressed() {
        return
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val createPostButton = findViewById<TextView>(R.id.registerRedirectText)

        createPostButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}