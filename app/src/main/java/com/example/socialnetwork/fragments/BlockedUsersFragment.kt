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
import com.example.socialnetwork.adpters.ReportAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Banned
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedUsersFragment : Fragment(),
    ReportAdapter.AcceptButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blocked_users, container, false)

        val token = PreferencesManager.getToken(requireContext())
        if (token == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return view
        }

//        fetchBlockedUsersFromServer(token)

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
//                    updateListView(banned)
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

//    private fun updateListView(banned: List<Banned>) {
//        val listView: ListView = requireView().findViewById(R.id.blockedUsersListView)
//        val adapter = ReportAdapter(
//            requireContext(),
//            ArrayList(banned),
//            null,
//            null,
//            getString(R.string.unblock_user),
//            null)
//
//        adapter.acceptButtonClickListener = this
//        listView.adapter = adapter
//    }

    override fun onAcceptButtonClick() {
        showConfirmationDialog()
    }
    override fun onDialogPositiveClick() {
        Toast.makeText(requireContext(),  "User is unblocked", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {
        Toast.makeText(requireContext(), "Unblocking canceled", Toast.LENGTH_SHORT).show()
    }
    private fun showConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_unblock))
        dialog.listener = this
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}