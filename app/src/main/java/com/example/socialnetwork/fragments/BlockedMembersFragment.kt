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
import com.example.socialnetwork.adapters.BannedAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Banned
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedMembersFragment : Fragment(),
    BannedAdapter.AcceptButtonClickListener{
        private var token: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blocked_members, container, false)

        token = PreferencesManager.getToken(requireContext())
        if (token == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return view
        }

//        fetchBlockedUsersFromServer(token!!)

        return view
    }
    private fun fetchBlockedUsersFromServer(token: String) {
        val bannedService = ClientUtils.getBannedService(token)
        val call = bannedService.getAllBlockedUsers()

        call.enqueue(object : Callback<List<Banned>> {
            override fun onResponse(
                call: Call<List<Banned>>,
                response: Response<List<Banned>>
            ) {
                if (response.isSuccessful) {
                    val banned = response.body() ?: arrayListOf()
                    updateListView(banned)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    showToast("Failed to load reports")
                }
            }

            override fun onFailure(call: Call<List<Banned>>, t: Throwable) {
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

    private fun updateListView(banned: List<Banned>) {
        val listView: ListView = requireView().findViewById(R.id.blockedMembersListView)
        val adapter = BannedAdapter(
            requireContext(),
            ArrayList(banned),
            getString(R.string.unblock_user))

        adapter.acceptButtonClickListener = this
        listView.adapter = adapter
    }

    override fun onAcceptButtonClick(bannedId: Long) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val bannedService = ClientUtils.getBannedService(token)

        bannedService.unblockUser(bannedId).enqueue(object : Callback<Banned> {
            override fun onResponse(call: Call<Banned>, response: Response<Banned>) {
                if (response.isSuccessful) {
                    showToast("User unblocked")
                    fetchBlockedUsersFromServer(token)
                } else {
                    showToast("Failed to unblock user")
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