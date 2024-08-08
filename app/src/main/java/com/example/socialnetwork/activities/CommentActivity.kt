package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.CommentAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Comment
import com.example.socialnetwork.model.entity.CreateCommentRequest
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentActivity : BottomSheetDialogFragment() {
    private var postId: Long? = null
    private var token: String? = null
    private lateinit var contentEditText: EditText
    private lateinit var errorMessage: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView = inflater.inflate(R.layout.activity_comment, container, false)
        @Suppress("DEPRECATION")
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        dialog?.setOnShowListener { dialog ->
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheetInternal = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetInternal?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        fragmentView.doOnNextLayout {
            fragmentView.setOnApplyWindowInsetsListener { v, insets ->
                val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                val imeHeight = imeInsets.bottom
                v.setPadding(0, 0, 0, imeHeight)
                insets
            }
        }

        return fragmentView
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

        contentEditText = view.findViewById(R.id.contentEditText)
        errorMessage = view.findViewById(R.id.errorMessage)

        view.findViewById<Button>(R.id.createCommentButton).setOnClickListener {
            postId?.let { it1 -> createComment(it1) }
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

    private fun createComment(postId: Long) {
        val text = contentEditText.text.toString().trim()

        if (text.isEmpty()) {
            showValidationError("Text cannot be empty")
            contentEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.rounded_grey_background_red_border)
            return
        }

        val comment = CreateCommentRequest(
            text = text
        )

        val token = PreferencesManager.getToken(requireContext()) ?: return

        val commentService = ClientUtils.getCommentService(token)
        val call = commentService.create(postId, comment)

        call.enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.isSuccessful) {
                    showToast("Comment created successfully")
                    fetchCommentsFromServer(token, postId)
                    resetUI()
                } else {
                    handleError(response)
                }
            }

            override fun onFailure(call: Call<Comment>, t: Throwable) {
                showValidationError("Error: ${t.message}")
            }
        })
    }
    private fun handleError(response: Response<Comment>) {
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        errorMessage.text = "Creating comment failed: $errorBody"
        errorMessage.visibility = View.VISIBLE
    }
    private fun showValidationError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
    }
    private fun resetUI() {
        contentEditText.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_grey_background)
        contentEditText.text.clear()
        errorMessage.visibility = View.GONE
    }
}
