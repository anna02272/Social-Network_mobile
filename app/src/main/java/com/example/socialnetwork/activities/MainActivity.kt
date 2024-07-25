package com.example.socialnetwork.activities

import com.example.socialnetwork.model.Post
import com.example.socialnetwork.adpters.PostAdapter
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.socialnetwork.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), PostAdapter.CommentButtonClickListener  {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val createPostButton = findViewById<Button>(R.id.createPostButton)
        val createPostPopup = findViewById<RelativeLayout>(R.id.createPostPopup)
        val closePopupButton = findViewById<ImageView>(R.id.closePopupButton)
        val dimBackgroundView = findViewById<View>(R.id.dimBackgroundView)
        val createPostTextView = findViewById<EditText>(R.id.popupPostEditText)
        val listView: ListView = findViewById(R.id.postsListView)

      createPostButton.setOnClickListener {
            dimBackgroundView.visibility = View.VISIBLE
            createPostPopup.visibility = View.VISIBLE
        }

        closePopupButton.setOnClickListener {
            dimBackgroundView.visibility = View.GONE
            createPostPopup.visibility = View.GONE

            createPostTextView.text.clear()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(createPostPopup.windowToken, 0)
        }

        bottomNavigationView.selectedItemId = R.id.bottom_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_search -> {
                    startActivity(Intent(applicationContext, SearchActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
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

        val posts = ArrayList<Post>()
        posts.add(Post(R.drawable.smiley_circle, "User1", "12 Jan 2024", "This is an example post content. It can be any text that a user might post on social network app."))
        posts.add(Post(R.drawable.smiley_circle, "User2", "13 Jan 2024", "Post content 2"))
        posts.add(Post(R.drawable.smiley_circle, "User3", "12 Jan 2024", "Post content 3"))
        posts.add(Post(R.drawable.smiley_circle, "User4", "13 Jan 2024", "Post content 4"))
        posts.add(Post(R.drawable.smiley_circle, "User1", "12 Jan 2024", "Post content 5"))
        posts.add(Post(R.drawable.smiley_circle, "User2", "13 Jan 2024", "Post content 6"))

        val adapter = PostAdapter(this, posts)
        adapter.commentButtonClickListener = this
        listView.adapter = adapter


    }
    override fun onCommentButtonClick(post: Post) {
        val commentActivity = CommentActivity()
        commentActivity.show(supportFragmentManager, "commentActivity")
    }
}
