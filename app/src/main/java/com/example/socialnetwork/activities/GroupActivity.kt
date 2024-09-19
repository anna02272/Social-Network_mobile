package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.PostAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.fragments.GroupFragment
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.services.PostService
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupActivity : AppCompatActivity() {
    private lateinit var group: Group
    private lateinit var postService: PostService
    private var sortingOrder = "descending"
    private var currentToast: Toast? = null
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        @Suppress("DEPRECATION")
        group = intent.getParcelableExtra("group")!!
        val groupFragment = GroupFragment.newInstance(group)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, groupFragment)
            .commit()

        initializeServices()
        fetchPostsFromServer(group.id)
        setupBottomNavigation()
    }

    private fun initializeServices() {
        val token = PreferencesManager.getToken(this) ?: return
        postService = ClientUtils.getPostService(token)
    }

    private fun fetchPostsFromServer(groupId: Long?) {
        val call = groupId?.let { postService.getByGroup(it) }

        call?.enqueue(object : Callback<List<Post>> {
            override fun onResponse(
                call: Call<List<Post>>,
                response: Response<List<Post>>
            ) {
                if (response.isSuccessful) {
                    val posts = response.body() ?: arrayListOf()
                    Log.d("GroupActivity", "Posts fetched: ${posts.size}, Posts: $posts")
                    updateListView(posts)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    Log.d("GroupActivity", "Error: ${response.code()}")
                    showToast("Failed to load posts")
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.e("GroupActivity", "Error: ${t.message}")
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun updateListView(posts: List<Post>) {
        val listView: ListView = findViewById(R.id.groupPostsListView)
        adapter = PostAdapter(this, posts)
        listView.adapter = adapter
    }

    private fun handleTokenExpired() {
        PreferencesManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_groups

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, PostsActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_search -> {
                    startActivity(Intent(applicationContext, SearchActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_groups -> {
                    startActivity(Intent(applicationContext, GroupsActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_notifications -> {
                    startActivity(Intent(applicationContext, NotificationsActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
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
}