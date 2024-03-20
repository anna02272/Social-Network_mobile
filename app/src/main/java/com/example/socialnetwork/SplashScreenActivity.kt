package com.example.socialnetwork

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.util.*

class SplashScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val SPLASH_TIME_OUT: Long = 2000
        Timer().schedule(object : TimerTask() {
            override fun run() {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            }
        }, SPLASH_TIME_OUT)
    }
}