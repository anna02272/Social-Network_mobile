package com.example.socialnetwork.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.activities.LoginActivity
import com.example.socialnetwork.adapters.MemberAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Banned
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class MembersFragment : Fragment(), MemberAdapter.AcceptButtonClickListener {
    private var token: String? = null
    private lateinit var group: Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_members, container, false)

        token = PreferencesManager.getToken(requireContext())
        if (token == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return view
        }

        @Suppress("DEPRECATION")
        group = arguments?.getParcelable("group")!!
        group.id?.let { fetchGroupMembersFromServer(token!!, it) }

        return view
    }

    private fun fetchGroupMembersFromServer(token: String, groupId: Long) {
        val groupRequestService = ClientUtils.getGroupRequestService(token)
        val call = groupRequestService.getApprovedUsersForGroup(groupId)

        call.enqueue(object : Callback<Set<User>> {
            override fun onResponse(call: Call<Set<User>>, response: Response<Set<User>>) {
                if (response.isSuccessful) {
                    val approvedMembers = response.body() ?: emptySet()
                    updateListView(approvedMembers)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    showToast("Failed to load reports")
                }
            }

            override fun onFailure(call: Call<Set<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun handleTokenExpired() {
        PreferencesManager.clearToken(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateListView(approvedMembers: Set<User>) {
        val listView: ListView = requireView().findViewById(R.id.membersListView)
        val adapter = MemberAdapter(
            requireContext(),
            ArrayList(approvedMembers),
            getString(R.string.block_user)
        )

        adapter.acceptButtonClickListener = this
        listView.adapter = adapter
    }

    override fun onAcceptButtonClick(userId: Long) {
        val banned = Banned(
            id = null,
            timeStamp = LocalDate.now(),
            isBlocked = true,
            groupAdmin = null,
            group = null,
            bannedUser = null,
            user = null
        )
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val bannedService = ClientUtils.getBannedService(token)

        bannedService.blockGroupUser(userId, group.id!!, banned).enqueue(object : Callback<Banned> {
            override fun onResponse(call: Call<Banned>, response: Response<Banned>) {
                if (response.isSuccessful) {
                    showToast("User blocked")
                    fetchGroupMembersFromServer(token, group.id!!)
                } else {
                    showToast("Failed to block user")
                }
            }

            override fun onFailure(call: Call<Banned>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
