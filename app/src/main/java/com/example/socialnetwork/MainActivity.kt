package com.example.socialnetwork

import Post
import PostAdapter
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val createPostButton = findViewById<Button>(R.id.createPostButton)
        val createPostForm = findViewById<ConstraintLayout>(R.id.createPostForm)
        val postEditText = findViewById<EditText>(R.id.postEditText)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        postEditText.setOnClickListener {
            createPostForm.visibility = View.VISIBLE
        }
        //ostaviti
//        createPostButton.setOnClickListener {
//            createPostForm.visibility = View.VISIBLE
//        }

        closeButton.setOnClickListener {
            createPostForm.visibility = View.GONE
        }

       //obrisati
        createPostButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        bottomNavigationView.selectedItemId = R.id.bottom_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_search -> {
                    startActivity(Intent(applicationContext, SearchActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_groups -> {
                    startActivity(Intent(applicationContext, GroupsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_notifications -> {
                    startActivity(Intent(applicationContext, NotificationsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                else -> false
            }
        }

        val posts = ArrayList<Post>()
        posts.add(Post(R.drawable.smiley_circle, "User1", "12 Jan 2024", "This is an example post content. It can be any text that a user might post on social network app."))
        posts.add(Post(R.drawable.smiley_circle, "User2", "13 Jan 2024", "Post content 2"))
        posts.add(Post(R.drawable.smiley_circle, "User3", "12 Jan 2024", "Post content 3"))
        posts.add(Post(R.drawable.smiley_circle, "User4", "13 Jan 2024", "Post content 4"))
        posts.add(Post(R.drawable.smiley_circle, "User1", "12 Jan 2024", "Post content 5"))
        posts.add(Post(R.drawable.smiley_circle, "User2", "13 Jan 2024", "Post content 6"))

        val adapter = PostAdapter(this, posts)

        val listView: ListView = findViewById(R.id.postsListView)
        listView.adapter = adapter

    }
}
