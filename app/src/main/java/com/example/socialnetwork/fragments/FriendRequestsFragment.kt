package com.example.socialnetwork.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.FriendRequestAdapter
import com.example.socialnetwork.model.FriendRequest

class FriendRequestsFragment : Fragment() {

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

        friendRequestsListView.adapter = adapter
    }

}