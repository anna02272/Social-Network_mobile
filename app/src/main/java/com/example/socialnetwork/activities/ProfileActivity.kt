package com.example.socialnetwork.activities
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.BulletPointAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var upperFirstNameTextView: TextView
    private lateinit var upperUsernameTextView: TextView
    private lateinit var lastSeenTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var profileNameEditText: EditText
    private lateinit var aboutEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupBottomNavigation()

        val token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        fetchUserData(token)
    }

    private fun initializeViews() {
        profileImageView = findViewById(R.id.profileImageView)
        upperFirstNameTextView = findViewById(R.id.upperFirstNameTextView)
        upperUsernameTextView = findViewById(R.id.upperUsernameTextView)
        lastSeenTextView = findViewById(R.id.lastSeenTextView)
        dateTextView = findViewById(R.id.dateTextView)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        profileNameEditText = findViewById(R.id.profileNameEditText)
        aboutEditText = findViewById(R.id.aboutEditText)
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_profile

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
                R.id.bottom_profile -> true
                else -> false
            }
        }
    }
    private fun fetchUserData(token: String) {
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        populateUserData(user)
                        user.id?.let {
                            fetchUserGroups(it, token);
                            fetchFriends(it, token)
                        }
                    }
                } else {
                    showToast("Failed to load user data")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun populateUserData(user: User) {
        upperFirstNameTextView.text = user.firstName + " " + user.lastName
        upperUsernameTextView.text = user.profileName?.takeIf { it.isNotEmpty() } ?: user.username
        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        dateTextView.text =  user.lastLogin?.format(formatter) ?: ""
        firstNameEditText.setText(user.firstName)
        lastNameEditText.setText(user.lastName)
        usernameEditText.setText(user.username)
        emailEditText.setText(user.email)
        profileNameEditText.setText(user.profileName)
        aboutEditText.setText(user.description)

//        user.image?.let {
//        }

    }
    private fun fetchUserGroups(userId: Long, token: String) {
        val groupRequestService = ClientUtils.getGroupRequestService(token)
        val groupCall = groupRequestService.getApprovedGroupsForUser(userId)

        groupCall.enqueue(object : Callback<Set<Group>> {
            override fun onResponse(call: Call<Set<Group>>, response: Response<Set<Group>>) {
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptySet()

                    val groupNames = groups.mapNotNull { group ->
                        group.name?.takeIf { it.isNotEmpty() }
                    }

//                    GroupRecyclerViewAdapter(groupNames)
                    val recyclerView: RecyclerView = findViewById(R.id.groupsProfileRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    recyclerView.adapter = BulletPointAdapter(this@ProfileActivity, groupNames)
                } else {
                    showToast("Failed to load groups")
                }
            }

            override fun onFailure(call: Call<Set<Group>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun fetchFriends(userId: Long, token: String) {
        val friendRequestService = ClientUtils.getFriendRequestService(token)
        val friendCall = friendRequestService.getApprovedFriendsForUser(userId)

        friendCall.enqueue(object : Callback<Set<User>> {
            override fun onResponse(call: Call<Set<User>>, response: Response<Set<User>>) {
                if (response.isSuccessful) {
                    val friends = response.body() ?: emptySet()

                    val friendUsernames = friends.mapNotNull { user ->
                        user.username?.takeIf { it.isNotEmpty() }
                    }

                    val recyclerView: RecyclerView = findViewById(R.id.friendsRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    recyclerView.adapter = BulletPointAdapter(this@ProfileActivity, friendUsernames)
                } else {
                    showToast("Failed to load friends")
                }
            }

            override fun onFailure(call: Call<Set<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
