package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.CommentAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Comment
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentActivity : BottomSheetDialogFragment() {
    private var postId: Long? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.commentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        token = PreferencesManager.getToken(requireContext())
        postId = arguments?.getLong("postId")

        if (token == null || postId == null) {
            handleTokenExpired()
        } else {
            fetchCommentsFromServer(token!!, postId!!)
        }
    }

    private fun updateRecyclerView(comments: List<Comment>) {
        val recyclerView: RecyclerView = view?.findViewById(R.id.commentsRecyclerView) ?: return
        val adapter = CommentAdapter(requireContext(), comments)
        recyclerView.adapter = adapter
    }


    private fun fetchCommentsFromServer(token: String, postId: Long) {
        val commentService = ClientUtils.getCommentService(token)
        val call = commentService.getCommentsByPostId(postId)

        call.enqueue(object : Callback<List<Comment>> {
            override fun onResponse(
                call: Call<List<Comment>>,
                response: Response<List<Comment>>
            ) {
                if (response.isSuccessful) {
                    val comments = response.body() ?: listOf()
                    updateRecyclerView(comments)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    showToast("Failed to load comments")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }


    private fun handleTokenExpired() {
        PreferencesManager.clearToken(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
