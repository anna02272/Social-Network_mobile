package com.example.socialnetwork.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.GroupRequestAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.GroupRequest
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupRequestsActivity : AppCompatActivity(),
    GroupRequestAdapter.AcceptButtonClickListener,
    GroupRequestAdapter.DeleteButtonClickListener {
    private var token: String? = null
    private lateinit var group: Group
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_requests)

        token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        @Suppress("DEPRECATION")
        group = intent.getParcelableExtra("group")!!
        group.id?.let { fetchGroupRequestsFromServer(token!!, it) }

        setupBottomNavigation()
    }

    private fun fetchGroupRequestsFromServer(token: String, groupId: Long) {
        val groupRequestService = ClientUtils.getGroupRequestService(token)

        groupId?.let { groupRequestService.getGroupRequestsByGroup(it) }
            ?.enqueue(object : Callback<List<GroupRequest>> {
                override fun onResponse(
                    call: Call<List<GroupRequest>>,
                    response: Response<List<GroupRequest>>
                ) {
                    if (response.isSuccessful) {
                        val groupRequests = response.body()?.filter { it.at == null } ?: arrayListOf()
                        updateListView(groupRequests)
                    } else if (response.code() == 401) {
                        handleTokenExpired()
                    } else {
                        showToast("Failed to load requests")
                    }
                }

                override fun onFailure(call: Call<List<GroupRequest>>, t: Throwable) {
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

    private fun updateListView(groupRequests: List<GroupRequest>) {
        val listView: ListView = findViewById(R.id.groupRequestsListView)
        val adapter = GroupRequestAdapter(
            this,
            ArrayList(groupRequests),
            getString(R.string.unblock_user))

        adapter.acceptButtonClickListener = this
        adapter.deleteButtonClickListener = this
        listView.adapter = adapter
    }

    override fun onAcceptButtonClick(groupRequestId: Long) {
        val groupRequestService = ClientUtils.getGroupRequestService(token)

        groupRequestService.approveGroupRequest(groupRequestId).enqueue(object : Callback<GroupRequest> {
            override fun onResponse(call: Call<GroupRequest>, response: Response<GroupRequest>) {
                if (response.isSuccessful) {
                    showToast("Group request is accepted!")
                    token?.let { fetchGroupRequestsFromServer(it, groupRequestId) }
                } else {
                    showToast("Failed to accept group request.")
                }
            }

            override fun onFailure(call: Call<GroupRequest>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    override fun onDeleteButtonClick(groupRequestId: Long) {
        val groupRequestService = ClientUtils.getGroupRequestService(token)

        groupRequestService.declineGroupRequest(groupRequestId).enqueue(object : Callback<GroupRequest> {
            override fun onResponse(call: Call<GroupRequest>, response: Response<GroupRequest>) {
                if (response.isSuccessful) {
                    showToast("Group request is declined!")
                    token?.let { fetchGroupRequestsFromServer(it, groupRequestId) }
                } else {
                    showToast("Failed to decline group request.")
                }
            }

            override fun onFailure(call: Call<GroupRequest>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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