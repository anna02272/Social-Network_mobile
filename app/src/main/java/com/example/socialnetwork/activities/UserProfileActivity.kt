package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.BulletPointAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EReportReason
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.CircleTransform
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class UserProfileActivity : AppCompatActivity() {
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
    private lateinit var reportUserButton: Button
    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var user: User? = null
    private var currentToast: Toast? = null
    private lateinit var dimBackgroundView: View
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        initializeViews()
        setupBottomNavigation()

        val token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        @Suppress("DEPRECATION")
        user = intent.getParcelableExtra("user")

        user?.id?.let { loadProfileImage(it, profileImageView) }
        user?.let { populateUserData(it) }
        user?.id?.let { fetchUserGroups(it, token) }
        user?.id?.let { fetchFriends(it, token) }

        fetchUserData()
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
        reportUserButton = findViewById(R.id.reportUserButton)
        dimBackgroundView = findViewById(R.id.dimBackgroundView)

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        reportUserButton.setOnClickListener {
            currentUser?.let { it1 -> user?.let { it2 -> showReportPopup(it1, it2) } }
        }
    }

    private fun loadProfileImage(userId: Long, profileImageView: ImageView) {
        val ref = storageReference!!.child("profile_images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.smiley_circle)
        }
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

                    val recyclerView: RecyclerView = findViewById(R.id.groupsProfileRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@UserProfileActivity)
                    recyclerView.adapter = BulletPointAdapter(this@UserProfileActivity, groupNames)
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
                    recyclerView.layoutManager = LinearLayoutManager(this@UserProfileActivity)
                    recyclerView.adapter = BulletPointAdapter(this@UserProfileActivity, friendUsernames)
                } else {
                    showToast("Failed to load friends")
                }
            }

            override fun onFailure(call: Call<Set<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showReportPopup(currentUser: User, user: User) {
        val reportPopup = findViewById<RelativeLayout>(R.id.createReportPopup)
        val closeButton = findViewById<ImageView>(R.id.closeReportPopupButton)
        val createButton = findViewById<Button>(R.id.popupCreateReportButton)
        val popupProfileImage = findViewById<ImageView>(R.id.popupProfileImage)
        val usernameTextView = findViewById<TextView>(R.id.usernameReportTextView)

        reportPopup.visibility = View.VISIBLE
        dimBackgroundView.visibility = View.VISIBLE

        closeButton.setOnClickListener {
            reportPopup.visibility = View.GONE
            dimBackgroundView.visibility = View.GONE
        }

        dimBackgroundView.setOnClickListener {
            reportPopup.visibility = View.GONE
            dimBackgroundView.visibility = View.GONE
        }

        if (usernameTextView != null) {
            usernameTextView.text = currentUser.profileName?.takeIf { it.isNotEmpty() } ?: currentUser.username
        }
        popupProfileImage?.let { imageView ->
            currentUser.id?.let { loadProfileImage(it, imageView) }
        }

        fillReportSpinner()

        createButton.setOnClickListener {
            submitReport(user)
        }
    }

    private fun fillReportSpinner() {
        val reportReasons = EReportReason.values().map { it.name.replace('_', ' ') }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportReasons)

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val reasonSpinner: Spinner = findViewById(R.id.popupReasonSpinner)
        reasonSpinner.adapter = spinnerAdapter

        reasonSpinner.setSelection(0)
    }

    private fun submitReport(user: User) {
        val reasonSpinner: Spinner = findViewById(R.id.popupReasonSpinner)
        val selectedReason = reasonSpinner.selectedItem.toString()
        val reason = EReportReason.valueOf(selectedReason.replace(' ', '_'))

        val report = Report(
            id = 0,
            reason = reason,
            timestamp = LocalDate.now(),
            accepted = false,
            isDeleted = false,
            user = user,
            post = null,
            comment = null,
            reportedUser = null
        )

        user.id?.let { userId ->
            val token = PreferencesManager.getToken(this) ?: return
            val reportService = ClientUtils.getReportService(token)
            reportService.reportUser(userId, report).enqueue(object : Callback<Report> {
                override fun onResponse(call: Call<Report>, response: Response<Report>) {
                    if (response.isSuccessful) {
                        showToast("Report submitted successfully")

                        findViewById<RelativeLayout>(R.id.createReportPopup).visibility = View.GONE
                        dimBackgroundView.visibility = View.GONE
                    } else {
                        showToast("Failed to submit report: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Report>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        }
    }

    private fun fetchUserData() {
        val token = PreferencesManager.getToken(this) ?: return
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                        val profileImage = findViewById<ImageView>(R.id.postProfileImage)
                        user.id?.let {
                            if (profileImage != null) {
                                loadProfileImage(it, profileImage)
                            }
                        }
                    }
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_home

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

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}
