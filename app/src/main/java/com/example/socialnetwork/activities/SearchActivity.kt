package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.SearchAdapter
import com.example.socialnetwork.model.User
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupBottomNavigation()

        fillSearchResultsArray()
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_search

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                R.id.bottom_search -> true
                R.id.bottom_groups -> {
                    startActivity(Intent(applicationContext, GroupsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                R.id.bottom_notifications -> {
                    startActivity(Intent(applicationContext, NotificationsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fillSearchResultsArray() {
        val listView: ListView = findViewById(R.id.searchListView)

        val users = ArrayList<User>()
        users.add(User("User1", "User1" ))
        users.add(User("User2", "User2"))
        users.add(User("User3", "User3"))
        users.add(User("User4", "User4"))
        users.add(User("User5", "User5"))


        val adapter = SearchAdapter(this, users)

        listView.adapter = adapter
    }
}
