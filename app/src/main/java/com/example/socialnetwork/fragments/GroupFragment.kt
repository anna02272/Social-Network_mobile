package com.example.socialnetwork.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.activities.GroupMembersActivity
import com.example.socialnetwork.activities.GroupRequestsActivity
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EUserType
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.GroupAdmin
import com.example.socialnetwork.model.entity.GroupRequest
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GroupFragment : Fragment() {
    private lateinit var group: Group
    private var currentUser: User? = null
    private var currentToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        @Suppress("DEPRECATION")
        arguments?.let {
            group = it.getParcelable("group")!!
        }

        bindGroupData(view)
        fetchUserData(view)

        return view
    }

    private fun bindGroupData(view: View) {
        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val adminTextView = view.findViewById<TextView>(R.id.adminTextView)
        val moreOptionsButton = view.findViewById<ImageButton>(R.id.moreOptionsButton)
        val joinButton = view.findViewById<Button>(R.id.joinButton)

        nameTextView.text = group.name
        descriptionTextView.text = group.description
        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        dateTextView.text = group.creationDate.format(formatter)

        if (group.groupAdmin.isNotEmpty()) {
            val adminNames = group.groupAdmin.joinToString { it.user?.username ?: "" }
            adminTextView.text = adminNames
        } else {
            adminTextView.text = "No admins"
        }

        moreOptionsButton.setOnClickListener { showPopupMenu(it, group) }
        joinButton.setOnClickListener{joinGroup(group)}
        joinButton.visibility = View.GONE
    }
    companion object {
        fun newInstance(group: Group): GroupFragment {
            val fragment = GroupFragment()
            val args = Bundle()
            args.putParcelable("group", group)
            fragment.arguments = args
            return fragment
        }
    }
    private fun joinGroup(group: Group?) {
        if (currentUser == null || group == null) {
            showToast("User or Group information is missing")
            return
        }

        val groupRequest = GroupRequest(
            id = null,
            approved = false,
            created_at = LocalDateTime.now(),
            user = currentUser!!,
            group = group,
        )

        val token = PreferencesManager.getToken(requireContext()) ?: return
        val groupRequestService = ClientUtils.getGroupRequestService(token)
        val call = groupRequestService.createGroupRequest(group.id!!, groupRequest)
        call.enqueue(object : Callback<GroupRequest> {
            override fun onResponse(call: Call<GroupRequest>, response: Response<GroupRequest>) {
                if (response.isSuccessful) {
                    showToast("Group request sent successfully")
                } else {
                   showToast("Failed to send group request")
                }
            }

            override fun onFailure(call: Call<GroupRequest>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun fetchUserData(view: View) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                        checkGroupRequestStatus(view)
                    }
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast( "Error: ${t.message}")
            }
        })
    }
    private fun checkGroupRequestStatus(view: View) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val groupRequestService = ClientUtils.getGroupRequestService(token)
        val call = groupRequestService.getGroupRequestByUserAndGroup(currentUser?.id ?: return, group.id ?: return)

        val isUserGroupAdmin = group.groupAdmin.any { it.user?.username == currentUser?.username }

        call.enqueue(object : Callback<GroupRequest> {
            override fun onResponse(call: Call<GroupRequest>, response: Response<GroupRequest>) {
                if (response.isSuccessful) {
                    val groupRequest = response.body()
                    if (groupRequest != null && groupRequest.approved) {
                        if (isUserGroupAdmin) {
                            view.findViewById<Button>(R.id.joinButton).visibility = View.GONE
                        }
                    } else {
                        if (!isUserGroupAdmin) {
                            view.findViewById<Button>(R.id.joinButton).visibility = View.VISIBLE
                        }
                    }
                } else {
                    if (isUserGroupAdmin) {
                        view.findViewById<Button>(R.id.joinButton).visibility = View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<GroupRequest>, t: Throwable) {
                if (isUserGroupAdmin) {
                    view.findViewById<Button>(R.id.joinButton).visibility = View.GONE
                } else {
                    view.findViewById<Button>(R.id.joinButton).visibility = View.VISIBLE
                }
            }
        })
    }

    private fun showPopupMenu(view: View, group: Group) {
        val popup = PopupMenu(requireContext(), view)

        val menu = popup.menu
        if (currentUser?.type == EUserType.ADMIN ||
            group.groupAdmin.any { it.user?.username == currentUser?.username }) {
            menu.add(0, R.id.edit, 1, requireContext().getString(R.string.group_requests))
        }

        if (currentUser?.type == EUserType.ADMIN ||
            group.groupAdmin.any { it.user?.username == currentUser?.username }) {
            menu.add(0, R.id.delete, 2, requireContext().getString(R.string.group_members))
        }
        if (currentUser?.type == EUserType.ADMIN) {
            menu.add(0, R.id.report, 2, requireContext().getString(R.string.remove_group_admin))
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    openGroupRequestActivity(group)
                    true
                }
                R.id.delete -> {
                    openGroupMembersActivity(group)
                    true
                }
                R.id.report -> {
                    removeGroupAdmin(group)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
    private fun openGroupRequestActivity(group: Group){
        val intent = Intent(requireContext(), GroupRequestsActivity::class.java)
        intent.putExtra("group", group)
        requireContext().startActivity(intent)
    }
    private fun openGroupMembersActivity(group: Group){
        val intent = Intent(requireContext(), GroupMembersActivity::class.java)
        intent.putExtra("group", group)
        requireContext().startActivity(intent)
    }
    private fun removeGroupAdmin(group: Group) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val groupAdminService = ClientUtils.getGroupAdminService(token)

        group.id?.let { groupAdminService.removeGroupAdmin(it) }
            ?.enqueue(object : Callback<GroupAdmin> {
                override fun onResponse(call: Call<GroupAdmin>, response: Response<GroupAdmin>) {
                    if (response.isSuccessful) {
                        showToast("Group admin removed successfully")
                        fetchUpdatedGroupData(group.id)
                    } else {
                        showToast("Failed to remove group admin")
                    }
                }

                override fun onFailure(call: Call<GroupAdmin>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
    }
    private fun fetchUpdatedGroupData(groupId: Long) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val groupService = ClientUtils.getGroupService(token)

        groupService.getById(groupId).enqueue(object : Callback<Group> {
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) {
                    response.body()?.let { updatedGroup ->
                        group = updatedGroup
                        view?.let { bindGroupData(it) }
                    }
                } else {
                    showToast("Failed to fetch updated group data")
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}
