package com.example.socialnetwork.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.SearchAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.FriendRequest
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.services.FriendRequestService
import com.example.socialnetwork.services.UserService
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class SearchActivity: AppCompatActivity(),
    SearchAdapter.AddFriendButtonClickListener {

    private lateinit var searchEditText: EditText
    private lateinit var listView: ListView
    private lateinit var adapter: SearchAdapter
    private val users = ArrayList<User>()
    private var currentUser: User? = null
    private var token: String? = null
    private lateinit var friendRequestService: FriendRequestService
    private lateinit var userService: UserService
    override fun onCreate(savedInstanceState: Bundle?) {
        token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupBottomNavigation()

        initializeServices()

        fetchUserData()

        initializeSearch()
    }
    private fun initializeServices() {
        val token = PreferencesManager.getToken(this) ?: return
        friendRequestService = ClientUtils.getFriendRequestService(token)
        userService = ClientUtils.getUserService(token)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeSearch() {
        listView = findViewById(R.id.searchListView)
        adapter = SearchAdapter(this, users)
        adapter.addFriendButtonClickListener = this
        listView.adapter = adapter

        searchEditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(createTextWatcher())
        searchEditText.setOnTouchListener(createOnTouchListener())
    }
    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isEmpty()) {
                    clearSearch()
                } else if (s != null && s.length > 2) {
                    searchUsers(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun createOnTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchEditText.right - searchEditText.compoundDrawables[2].bounds.width())) {
                    searchEditText.text.clear()
                    clearSearch()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
    private fun clearSearch() {
        users.clear()
        adapter.notifyDataSetChanged()
    }
    private fun searchUsers(keyword: String) {
        val call = friendRequestService.searchUsers(keyword)
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val filteredUsers = response.body()?.filter { it.id != currentUser?.id } ?: emptyList()
                    users.clear()
                    users.addAll(filteredUsers)
                    adapter.notifyDataSetChanged()
                } else {
                    showToast("Failed to fetch users: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun fetchUserData() {
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    override fun onAddFriendButtonClick(user: User) {
         val userId = user.id
        if (currentUser != null && userId != null) {
            val friendRequest = FriendRequest(
                id = null,
                approved = false,
                created_at = LocalDateTime.now(),
                at = null,
                fromUser = currentUser!!,
                forUser = user
            )

            val call = friendRequestService.create(userId, friendRequest)
            call.enqueue(object : Callback<FriendRequest> {
                override fun onResponse(call: Call<FriendRequest>, response: Response<FriendRequest>) {
                    if (response.isSuccessful) {
                        showToast("Friend request sent successfully")
                    } else {
                        showToast("You already sent friend request to this user.")
                    }
                }

                override fun onFailure(call: Call<FriendRequest>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        } else {
            showToast("Error: Current user or user ID is null")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_search

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, PostsActivity::class.java))
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

}
