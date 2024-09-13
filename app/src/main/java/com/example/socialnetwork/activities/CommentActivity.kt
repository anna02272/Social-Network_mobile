package com.example.socialnetwork.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
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
import com.example.socialnetwork.model.entity.EReportReason
import com.example.socialnetwork.model.entity.Reaction
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.services.CommentService
import com.example.socialnetwork.services.ReactionService
import com.example.socialnetwork.services.ReportService
import com.example.socialnetwork.services.UserService
import com.example.socialnetwork.utils.CircleTransform
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
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
       private lateinit var dimBackgroundView: View
       private lateinit var storageReference: StorageReference
       private var commentToEdit: Comment? = null
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
            fetchCommentsFromServer(postId!!)
        }

        fetchUserData()
        initializeFirebaseStorage()

        contentEditText = view.findViewById(R.id.contentEditText)
        errorMessage = view.findViewById(R.id.errorMessage)
        dimBackgroundView = view.findViewById(R.id.dimBackgroundView)

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

       private fun initializeFirebaseStorage() {
           if (FirebaseApp.getApps(requireContext()).isEmpty()) {
               FirebaseApp.initializeApp(requireContext())
           }
           storageReference = FirebaseStorage.getInstance().reference
       }

       override fun onReplyButtonClick(comment: Comment) {
           TODO("Not yet implemented")
       }

       override fun onEditButtonClick(comment: Comment, updateButton: Button, commentContentEditText: EditText) {
           commentToEdit = comment
           setupEditView(updateButton, commentContentEditText)
       }

       override fun onDeleteButtonClick(comment: Comment) {
           deleteComment(comment)
       }

       override fun onReportButtonClick(comment: Comment) {
           showReportPopup(comment)
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


    private fun fetchCommentsFromServer(postId: Long) {
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
                           val profileImage = view?.findViewById<ImageView>(R.id.profileImage)
                           user.id?.let {
                               if (profileImage != null) {
                                   loadProfileImage(it, profileImage)
                               }
                           }
                       }
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
           context?.let {
               currentToast?.cancel()
               currentToast = Toast.makeText(it, message, Toast.LENGTH_SHORT)
               currentToast?.show()
           }
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
                    token?.let { fetchCommentsFromServer(postId) }
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
       private fun setupEditView(updateButton: Button, commentContentEditText: EditText) {
           commentContentEditText.isFocusable = true
           commentContentEditText.isFocusableInTouchMode = true
           commentContentEditText.isClickable = true
           commentContentEditText.requestFocus()
           updateButton.visibility = View.VISIBLE

           commentToEdit?.let {
               commentContentEditText.setText(it.text)
           }

           updateButton.setOnClickListener {
               onUpdateButtonClick(commentContentEditText, updateButton)
           }
       }
       private fun onUpdateButtonClick(commentContentEditText: EditText, updateButton: Button) {
           val updatedText = commentContentEditText.text.toString().trim()

           if (updatedText.isEmpty()) {
               showValidationError("Text cannot be empty")
               commentContentEditText.background =
                   ContextCompat.getDrawable(requireContext(), R.drawable.rounded_grey_background_red_border)
               return
           }

           val updatedComment = CreateCommentRequest(
               text = updatedText
           )

           commentToEdit?.let { comment ->
               commentService.update(comment.id, updatedComment).enqueue(object : Callback<Comment> {
                   override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                       if (response.isSuccessful) {
                           showToast("Comment updated successfully")
                           commentContentEditText.isFocusable = false
                           commentContentEditText.isFocusableInTouchMode = false
                           commentContentEditText.isClickable = false
                           updateButton.visibility = View.GONE
                           resetUI()
                           fetchCommentsFromServer(comment.post.id)
                       } else {
                           handleError(response)
                       }
                   }

                   override fun onFailure(call: Call<Comment>, t: Throwable) {
                       showValidationError("Error: ${t.message}")
                   }
               })
           }
       }
       private fun deleteComment(comment: Comment){
           comment.id.let { commentId ->
               commentService.delete(commentId).enqueue(object : Callback<Comment> {
                   override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                       if (response.isSuccessful) {
                           showToast("Comment deleted successfully")
                           token?.let { fetchCommentsFromServer(comment.post.id) }
                       } else {
                           showToast("Failed to delete comment: ${response.message()}")
                       }
                   }

                   override fun onFailure(call: Call<Comment>, t: Throwable) {
                       showToast("Error: ${t.message}")
                   }
               })
           }
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

       private fun showReportPopup(comment: Comment) {
           val reportPopup = view?.findViewById<RelativeLayout>(R.id.createReportPopup)
           val popupProfileImage = view?.findViewById<ImageView>(R.id.popupProfileImage)
           val usernameTextView = view?.findViewById<TextView>(R.id.usernameReportTextView)

           if (reportPopup != null) {
               reportPopup.visibility = View.VISIBLE
           }
           dimBackgroundView.visibility = View.VISIBLE

           view?.findViewById<ImageView>(R.id.closeReportPopupButton)?.setOnClickListener {
               if (reportPopup != null) {
                   reportPopup.visibility = View.GONE
               }
               dimBackgroundView.visibility = View.GONE
           }

           dimBackgroundView.setOnClickListener {
               if (reportPopup != null) {
                   reportPopup.visibility = View.GONE
               }
               dimBackgroundView.visibility = View.GONE
           }

           currentUser?.let { user ->
               if (usernameTextView != null) {
                   usernameTextView.text = user.profileName?.takeIf { it.isNotEmpty() } ?: user.username
               }
               popupProfileImage?.let { imageView ->
                   user.id?.let { loadProfileImage(it, imageView) }
               }
           }

           fillReportSpinner()

           view?.findViewById<Button>(R.id.popupCreateReportButton)?.setOnClickListener {
               submitReport(comment)
           }
       }

       private fun fillReportSpinner() {
           val reportReasons = EReportReason.values().map { it.name.replace('_', ' ') }

           val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reportReasons)
           spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

           view?.findViewById<Spinner>(R.id.popupReasonSpinner)?.let { reasonSpinner ->
               reasonSpinner.adapter = spinnerAdapter
               reasonSpinner.setSelection(0)
           }
       }

       private fun submitReport(comment: Comment) {
           view?.findViewById<Spinner>(R.id.popupReasonSpinner)?.let { reasonSpinner ->
               val selectedReason = reasonSpinner.selectedItem.toString()
               val reason = EReportReason.valueOf(selectedReason.replace(' ', '_'))

               val report = currentUser?.let {
                   Report(
                       id = 0,
                       reason = reason,
                       timestamp = LocalDate.now(),
                       accepted = false,
                       isDeleted = false,
                       user = it,
                       post = null,
                       comment = comment,
                       reportedUser = null
                   )
               }

               comment.id?.let { commentId ->
                   if (report != null) {
                       reportService.reportComment(commentId, report).enqueue(object : Callback<Report> {
                           override fun onResponse(call: Call<Report>, response: Response<Report>) {
                               if (response.isSuccessful) {
                                   showToast("Report submitted successfully")

                                   view?.findViewById<RelativeLayout>(R.id.createReportPopup)?.visibility = View.GONE
                                   dimBackgroundView.visibility = View.GONE
                               } else {
                                   showToast("Failed to submit report: ${response.message()}")
                               }
                           }

                           override fun onFailure(call: Call<Report>, t: Throwable) {
                               showToast("Error: ${t.message}")
                           }
                       })
                   }
               }
           }
       }

       private fun loadProfileImage(userId: Long, profileImageView: ImageView) {
           val ref = storageReference!!.child("profile_images/$userId")
           ref.downloadUrl.addOnSuccessListener { uri ->
               Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
           }.addOnFailureListener {
               profileImageView.setImageResource(R.drawable.smiley_circle)
           }
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
