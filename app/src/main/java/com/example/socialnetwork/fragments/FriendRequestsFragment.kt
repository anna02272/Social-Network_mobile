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
import com.example.socialnetwork.adapters.FriendRequestAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.FriendRequest
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendRequestsFragment : Fragment(),
    FriendRequestAdapter.DeleteButtonClickListener,
    FriendRequestAdapter.AcceptButtonClickListener {

    private var userId: Long? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_requests, container, false)

        token = PreferencesManager.getToken(requireContext())
        if (token == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return view
        }

        fetchUserData(token!!)

        return view
    }
    private fun fetchFriendRequestsFromServer(token: String, userId: Long) {
        val friendRequestService = ClientUtils.getFriendRequestService(token)

        userId?.let { friendRequestService.getByUser(userId) }
            ?.enqueue(object : Callback<List<FriendRequest>> {
                override fun onResponse(
                    call: Call<List<FriendRequest>>,
                    response: Response<List<FriendRequest>>
                ) {
                    if (response.isSuccessful) {
                        val friendRequest = response.body() ?: arrayListOf()
                        updateListView(friendRequest)
                    } else if (response.code() == 401) {
                        handleTokenExpired()
                    } else {
                        showToast("Failed to load reports")
                    }
                }

                override fun onFailure(call: Call<List<FriendRequest>>, t: Throwable) {
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
    private fun updateListView(friendRequests: List<FriendRequest>) {
        val filteredFriendRequests = friendRequests.filter { friendRequest ->
            friendRequest.at == null
        }
        val listView: ListView = requireView().findViewById(R.id.friendRequestsListView)
        val adapter = FriendRequestAdapter(
            requireContext(),
            ArrayList(filteredFriendRequests),
            getString(R.string.accept),
            getString(R.string.delete_request)
        )

        adapter.deleteButtonClickListener = this
        adapter.acceptButtonClickListener = this
        listView.adapter = adapter
    }

    private fun fetchUserData(token: String) {
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        userId = user.id
                        userId?.let { fetchFriendRequestsFromServer(token, it) }
                    }
                } else {
                    showToast("Failed to load user data")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    override fun onDeleteButtonClick(friendRequestId: Long) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val friendRequestService = ClientUtils.getFriendRequestService(token)

        friendRequestService.decline(friendRequestId).enqueue(object : Callback<FriendRequest> {
            override fun onResponse(call: Call<FriendRequest>, response: Response<FriendRequest>) {
                if (response.isSuccessful) {
                    showToast("Friend request declined")
                    userId?.let { fetchFriendRequestsFromServer(token, it) }
                } else {
                    showToast("Failed to decline friend request")
                }
            }

            override fun onFailure(call: Call<FriendRequest>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    override fun onAcceptButtonClick(friendRequestId: Long) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val friendRequestService = ClientUtils.getFriendRequestService(token)

        friendRequestService.approve(friendRequestId).enqueue(object : Callback<FriendRequest> {
            override fun onResponse(call: Call<FriendRequest>, response: Response<FriendRequest>) {
                if (response.isSuccessful) {
                    showToast("Friend request approved")
                    userId?.let { fetchFriendRequestsFromServer(token, it) }
                } else {
                    showToast("Failed to approve friend request")
                }
            }

            override fun onFailure(call: Call<FriendRequest>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}