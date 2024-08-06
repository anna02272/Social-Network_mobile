package com.example.socialnetwork.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.GroupAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.CreateGroupRequest
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsActivity : AppCompatActivity(),
    GroupAdapter.DeleteButtonClickListener {

    private lateinit var dimBackgroundView: View
    private lateinit var createGroupPopup: RelativeLayout
    private lateinit var suspendGroupPopup: RelativeLayout
    private lateinit var popupNameEditText: EditText
    private lateinit var popupDescriptionEditText: EditText
    private lateinit var popupReasonEditText: EditText
    private lateinit var closeGroupPopupButton: ImageView
    private lateinit var createGroupButton: Button
    private lateinit var closeSuspendGroupPopupButton: ImageView
    private lateinit var popupSuspendGroupButton: Button
    private lateinit var groupErrorMessage: TextView
    private lateinit var suspendErrorMessage: TextView
    private var groupIdToSuspend: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setupBottomNavigation()

        initializeViews()

        fetchGroupsFromServer(token)
    }
    private fun initializeViews() {
        dimBackgroundView = findViewById(R.id.dimBackgroundView)
        createGroupPopup = findViewById(R.id.createGroupPopup)
        suspendGroupPopup = findViewById(R.id.suspendGroupPopup)
        popupNameEditText = findViewById(R.id.popupNameEditText)
        popupDescriptionEditText = findViewById(R.id.popupDescriptionEditText)
        popupReasonEditText = findViewById(R.id.popupReasonEditText)
        closeGroupPopupButton = findViewById(R.id.closeGroupPopupButton)
        createGroupButton = findViewById(R.id.createGroupButton)
        closeSuspendGroupPopupButton = findViewById(R.id.closeSuspendGroupPopupButton)
        popupSuspendGroupButton = findViewById(R.id.popupSuspendGroupButton)
        groupErrorMessage = findViewById(R.id.group_error_message)
        suspendErrorMessage =  findViewById(R.id.suspend_error_message)

        createGroupButton.setOnClickListener { showGroupPopup() }
        closeGroupPopupButton.setOnClickListener { hideGroupPopup() }

        closeSuspendGroupPopupButton.setOnClickListener { hideSuspendPopup() }
        popupSuspendGroupButton.setOnClickListener { groupIdToSuspend?.let {
            suspendGroup(it)
        } }
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_groups

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
                R.id.bottom_groups -> true
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
    private fun fetchGroupsFromServer(token: String) {
        val groupService = ClientUtils.getGroupService(token)
        val call = groupService.getAll()

        call.enqueue(object : Callback<ArrayList<Group>> {
            override fun onResponse(
                call: Call<ArrayList<Group>>,
                response: Response<ArrayList<Group>>
            ) {
                if (response.isSuccessful) {
                    val groups = response.body() ?: arrayListOf()
                    updateListView(groups)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    showToast("Failed to load groups")

                }
            }

            override fun onFailure(call: Call<ArrayList<Group>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun handleTokenExpired() {
        PreferencesManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun updateListView(groups: ArrayList<Group>) {
        val listView: ListView = findViewById(R.id.groupsListView)
        val adapter = GroupAdapter(this, groups)
        adapter.deleteButtonClickListener = this
        listView.adapter = adapter
    }
    private fun showGroupPopup() {
        dimBackgroundView.visibility = View.VISIBLE
        createGroupPopup.visibility = View.VISIBLE

        val popupCreateGroupButton = findViewById<Button>(R.id.popupCreateGroupButton)
        popupCreateGroupButton.setOnClickListener {
            createGroup()
        }
    }
    override fun onDeleteButtonClick(groupId: Long?) {
        if (groupId != null) {
            showSuspendPopup(groupId)
        }
    }
    private fun hideGroupPopup() {
        dimBackgroundView.visibility = View.GONE
        createGroupPopup.visibility = View.GONE
        popupNameEditText.text.clear()
        popupDescriptionEditText.text.clear()
        groupErrorMessage.visibility = View.GONE
        resetBorders()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(createGroupPopup.windowToken, 0)
    }
    private fun showSuspendPopup(groupId: Long) {
        this.groupIdToSuspend = groupId

        dimBackgroundView.visibility = View.VISIBLE
        suspendGroupPopup.visibility = View.VISIBLE
    }
    private fun hideSuspendPopup() {
        dimBackgroundView.visibility = View.GONE
        suspendGroupPopup.visibility = View.GONE
        popupReasonEditText.text.clear()
        suspendErrorMessage.visibility = View.GONE
        resetBorders()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(suspendGroupPopup.windowToken, 0)
    }
    private fun createGroup() {
        val name = popupNameEditText.text.toString().trim()
        val description = popupDescriptionEditText.text.toString().trim()

        resetBorders()

        if (name.isEmpty()) {
            showCreateGroupValidationError("Name cannot be empty")
            popupNameEditText.background =
                ContextCompat.getDrawable(this, R.drawable.border_red_square)
            return
        }

        if (description.isEmpty()) {
            showCreateGroupValidationError("Description cannot be empty")
            popupDescriptionEditText.background =
                ContextCompat.getDrawable(this, R.drawable.border_red_square)
            return
        }

        val newGroup = CreateGroupRequest(
            name = name,
            description = description
        )

        val token = PreferencesManager.getToken(this) ?: return
        val groupService = ClientUtils.getGroupService(token)
        val call = groupService.create(newGroup)

        call.enqueue(object : Callback<Group> {
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) {
                    showToast("Group created successfully")
                    hideGroupPopup()
                    fetchGroupsFromServer(token)
                } else {
                    handleError(response)
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                showCreateGroupValidationError("Error: ${t.message}")
            }
        })
    }
    private fun handleError(response: Response<Group>) {
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        groupErrorMessage.text = "Creating group failed: $errorBody"
        groupErrorMessage.visibility = View.VISIBLE
    }
    private fun showCreateGroupValidationError(message: String) {
        groupErrorMessage.text = message
        groupErrorMessage.visibility = View.VISIBLE
    }
    private fun resetBorders() {
        popupNameEditText.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_border)
        popupDescriptionEditText.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_border)
        popupReasonEditText.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_border)
    }
    private fun suspendGroup(groupId: Long) {
        val reason = findViewById<EditText>(R.id.popupReasonEditText).text.toString()

        resetBorders()

        if (reason.isEmpty()) {
            showSuspendValidationError("Suspend reason cannot be empty")
            popupReasonEditText.background =
                ContextCompat.getDrawable(this, R.drawable.border_red_square)
            return
        }

        val reasonPart = reason.toRequestBody("text/plain".toMediaTypeOrNull())

        val token = PreferencesManager.getToken(this) ?: return
        val groupService = ClientUtils.getGroupService(token)
        val call = groupService.delete(groupId, reasonPart)

        call.enqueue(object : Callback<Group> {
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) {
                    showToast("Group suspended successfully")
                    hideSuspendPopup()
                    fetchGroupsFromServer(token)
                } else {
                    showSuspendValidationError("Failed to suspend group")
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                showSuspendValidationError("Error: ${t.message}")
            }
        })
    }

    private fun showSuspendValidationError(message: String) {
        suspendErrorMessage.text = message
        suspendErrorMessage.visibility = View.VISIBLE
    }
}


