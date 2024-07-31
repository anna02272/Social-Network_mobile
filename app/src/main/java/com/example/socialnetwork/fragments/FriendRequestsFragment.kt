package com.example.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.FriendRequestAdapter
import com.example.socialnetwork.model.entity.FriendRequest

class FriendRequestsFragment : Fragment(),
    FriendRequestAdapter.DeleteButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_requests, container, false)

        fillFriendRequestArray(view)

        return view
    }
    private fun fillFriendRequestArray(view: View) {
        val friendRequestsListView = view.findViewById<ListView>(R.id.friendRequestsListView)

        val friendRequests = ArrayList<FriendRequest>()
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))
        friendRequests.add(FriendRequest("User",  "12 Jan 2024"))

        val adapter = FriendRequestAdapter(requireContext(), friendRequests)
        adapter.deleteButtonClickListener = this

        friendRequestsListView.adapter = adapter
    }
    override fun onDeleteButtonClick() {
        showConfirmationDialog()
    }
    override fun onDialogPositiveClick() {
        Toast.makeText(requireContext(),  "Friend Request deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {
        Toast.makeText(requireContext(), "Delete canceled", Toast.LENGTH_SHORT).show()
    }
    private fun showConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_delete_friend_request))
        dialog.listener = this
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }
}