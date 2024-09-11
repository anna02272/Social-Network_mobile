package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import com.example.socialnetwork.model.entity.EReactionType
import com.example.socialnetwork.model.entity.Reaction
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.services.CommentService
import com.example.socialnetwork.services.ReactionService
import com.example.socialnetwork.services.ReportService
import com.example.socialnetwork.services.UserService
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class CommentActivity : BottomSheetDialogFragment(),
    CommentAdapter.ReplyButtonClickListener,
    CommentAdapter.EditButtonClickListener,
    CommentAdapter.DeleteButtonClickListener,
    CommentAdapter.ReportButtonClickListener,
    CommentAdapter.LikeButtonClickListener,
    CommentAdapter.DislikeButtonClickListener,
    CommentAdapter.HeartButtonClickListener
   {
    private var postId: Long? = null
    private var token: String? = null
    private lateinit var contentEditText: EditText
    private lateinit var errorMessage: TextView
    private var currentToast: Toast? = null
       private lateinit var commentService: CommentService
       private lateinit var reactionService: ReactionService
       private lateinit var userService: UserService
       private lateinit var reportService: ReportService
       private var currentUser: User? = null

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
            initializeServices()
            fetchCommentsFromServer(token!!, postId!!)
        }

        fetchUserData()

        contentEditText = view.findViewById(R.id.contentEditText)
        errorMessage = view.findViewById(R.id.errorMessage)

        view.findViewById<Button>(R.id.createCommentButton).setOnClickListener {
            postId?.let { it1 -> createComment(it1) }
        }

    }
       private fun initializeServices() {
           commentService = ClientUtils.getCommentService(token)
           reactionService = ClientUtils.getReactionService(token)
           userService = ClientUtils.getUserService(token)
           reportService = ClientUtils.getReportService(token)
       }
       override fun onReplyButtonClick(comment: Comment) {
           TODO("Not yet implemented")
       }

       override fun onEditButtonClick(comment: Comment) {
           TODO("Not yet implemented")
       }

       override fun onDeleteButtonClick(comment: Comment) {
           TODO("Not yet implemented")
       }

       override fun onReportButtonClick(comment: Comment) {
           TODO("Not yet implemented")
       }

       override fun onLikeButtonClick(comment: Comment, view: View) {
           reactToComment(comment, EReactionType.LIKE, view)
       }

       override fun onDislikeButtonClick(comment: Comment, view: View) {
           reactToComment(comment, EReactionType.DISLIKE, view)
       }

       override fun onHeartButtonClick(comment: Comment, view: View) {
           reactToComment(comment, EReactionType.HEART, view)
       }

    private fun updateRecyclerView(comments: List<Comment>) {
        val recyclerView: RecyclerView = view?.findViewById(R.id.commentsRecyclerView) ?: return
        val adapter = CommentAdapter(requireContext(), comments)
        adapter.replyButtonClickListener = this
        adapter.editButtonClickListener = this
        adapter.deleteButtonClickListener = this
        adapter.reportButtonClickListener = this
        adapter.likeButtonClickListener = this
        adapter.dislikeButtonClickListener = this
        adapter.heartButtonClickListener = this
        recyclerView.adapter = adapter
    }


    private fun fetchCommentsFromServer(token: String, postId: Long) {
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
       private fun fetchUserData() {
           val call = userService.whoAmI()

           call.enqueue(object : Callback<User> {
               override fun onResponse(call: Call<User>, response: Response<User>) {
                   if (response.isSuccessful) {
                       val user = response.body()
                       if (user != null) {
                           currentUser = user
                       }
                   } else {
                   }
               }

               override fun onFailure(call: Call<User>, t: Throwable) {
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
        currentToast?.cancel()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
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

        val call = commentService.create(postId, comment)

        call.enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.isSuccessful) {
                    showToast("Comment created successfully")
                    token?.let { fetchCommentsFromServer(it, postId) }
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
       private fun reactToComment(comment: Comment, reactionType: EReactionType, view: View) {
           val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)
           val dislikeCountTextView: TextView = view.findViewById(R.id.dislikeCountTextView)
           val heartCountTextView: TextView = view.findViewById(R.id.heartCountTextView)
           val likeButton: ImageButton = view.findViewById(R.id.likeButton)
           val dislikeButton: ImageButton = view.findViewById(R.id.dislikeButton)
           val heartButton: ImageButton = view.findViewById(R.id.heartButton)
           val reaction =
               currentUser?.let { Reaction(0, reactionType, LocalDate.now(), it, null, comment) }
           reaction?.let { reactionService.reactToComment(comment.id, it) }
               ?.enqueue(object : Callback<Reaction> {
                   override fun onResponse(call: Call<Reaction>, response: Response<Reaction>) {
                       if (response.isSuccessful) {
                           CommentAdapter.updateReactionCounts(requireContext(), comment, likeCountTextView, dislikeCountTextView, heartCountTextView)
                           currentUser?.let {
                              CommentAdapter.checkUserReaction(requireContext(), it, comment, likeButton, dislikeButton, heartButton) }
                           showToast("Reacted successfully")
                       }
                   }

                   override fun onFailure(call: Call<Reaction>, t: Throwable) {
                       showToast("Error: ${t.message}")
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
