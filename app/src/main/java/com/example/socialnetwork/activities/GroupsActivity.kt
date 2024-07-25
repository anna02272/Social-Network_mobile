package com.example.socialnetwork.activities

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
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.CommentAdapter
import com.example.socialnetwork.adpters.GroupAdapter
import com.example.socialnetwork.model.Comment
import com.example.socialnetwork.model.Group
import com.google.android.material.bottomnavigation.BottomNavigationView

class GroupsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val groupsListView = findViewById<ListView>(R.id.groupsListView)
        val createGroupButton = findViewById<Button>(R.id.createGroupButton)
        val createGroupPopup = findViewById<RelativeLayout>(R.id.createGroupPopup)
        val closePopupButton = findViewById<ImageView>(R.id.closePopupButton)
        val dimBackgroundView = findViewById<View>(R.id.dimBackgroundView)
        val popupNameEditText = findViewById<EditText>(R.id.popupNameEditText)
        val popupDescriptionEditText = findViewById<EditText>(R.id.popupDescriptionEditText)

        createGroupButton.setOnClickListener {
            dimBackgroundView.visibility = View.VISIBLE
            createGroupPopup.visibility = View.VISIBLE
        }

        closePopupButton.setOnClickListener {
            dimBackgroundView.visibility = View.GONE
            createGroupPopup.visibility = View.GONE

            popupNameEditText.text.clear()
            popupDescriptionEditText.text.clear()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(createGroupPopup.windowToken, 0)
        }

        bottomNavigationView.selectedItemId = R.id.bottom_groups

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

        val groups = ArrayList<Group>()
        groups.add(Group("Group Name", "Group Description", "12 Jan 2024", "Group Admin"))
        groups.add(Group("Group Name", "Group Description", "12 Jan 2024", "Group Admin"))
        groups.add(Group("Group Name", "Group Description", "12 Jan 2024", "Group Admin"))
        groups.add(Group("Group Name", "Group Description", "12 Jan 2024", "Group Admin"))
        groups.add(Group("Group Name", "Group Description", "12 Jan 2024", "Group Admin"))
        groups.add(Group("Group Name", "Group Description", "12 Jan 2024", "Group Admin"))

        val adapter = GroupAdapter(this, groups)

        groupsListView.adapter = adapter

    }
}
