package com.example.socialnetwork.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.activities.UserProfileActivity
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Comment
import com.example.socialnetwork.model.entity.EReactionType
import com.example.socialnetwork.model.entity.Reaction
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.CircleTransform
import com.example.socialnetwork.utils.PreferencesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

class CommentAdapter(
    private val context: Context,
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private lateinit var storageReference: StorageReference
    private var currentUser: User? = null
    private var currentToast: Toast? = null

    init {
        initializeFirebaseStorage()
        fetchUserData(object : UserCallback {
            override fun onUserFetched(user: User?) {
                currentUser = user
                notifyDataSetChanged()
            }
        })
    }
    private fun initializeFirebaseStorage() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        storageReference = FirebaseStorage.getInstance().reference
    }

    interface UserCallback {
        fun onUserFetched(user: User?)
    }
    interface ReplyButtonClickListener {
        fun onReplyButtonClick(comment: Comment)
    }

    interface EditButtonClickListener {
        fun onEditButtonClick(comment: Comment, updateButton: Button, commentContentEditText: EditText)
    }
    interface DeleteButtonClickListener {
        fun onDeleteButtonClick(comment: Comment)
    }
    interface ReportButtonClickListener {
        fun onReportButtonClick(comment: Comment)
    }

    interface LikeButtonClickListener {
        fun onLikeButtonClick(comment: Comment, view: View)
    }

    interface DislikeButtonClickListener {
        fun onDislikeButtonClick(comment: Comment, view: View)
    }

    interface HeartButtonClickListener {
        fun onHeartButtonClick(comment: Comment, view: View)
    }

    var replyButtonClickListener: ReplyButtonClickListener? = null
    var editButtonClickListener: EditButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null
    var reportButtonClickListener: ReportButtonClickListener? = null
    var likeButtonClickListener: LikeButtonClickListener? = null
    var dislikeButtonClickListener: DislikeButtonClickListener? = null
    var heartButtonClickListener: HeartButtonClickListener? = null

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImage)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val commentContentEditText: EditText = itemView.findViewById(R.id.commentContentEditText)
        val updateButton: Button = itemView.findViewById(R.id.updateButton)
        val moreOptionsButton: ImageButton = itemView.findViewById(R.id.moreOptionsButton)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val dislikeButton: ImageButton = itemView.findViewById(R.id.dislikeButton)
        val heartButton: ImageButton = itemView.findViewById(R.id.heartButton)
        val likeCountTextView: TextView = itemView.findViewById(R.id.likeCountTextView)
        val dislikeCountTextView: TextView = itemView.findViewById(R.id.dislikeCountTextView)
        val heartCountTextView: TextView = itemView.findViewById(R.id.heartCountTextView)
        val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_comment, parent, false)
        return CommentViewHolder(view)

    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        comment.user?.id?.let { loadProfileImage(it, holder.profileImageView) }
        holder.usernameTextView.text = comment.user?.profileName?.takeIf { it.isNotEmpty() } ?: comment.user?.username
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        holder.dateTextView.text = comment.timeStamp.format(formatter)
        holder.commentContentEditText.setText(comment.text)


        holder.moreOptionsButton.setOnClickListener { view ->
            showPopupMenu(view, comment, holder.updateButton, holder.commentContentEditText)
        }

        holder.usernameTextView.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("user", comment.user)
            context.startActivity(intent)
        }

        setupButtons(holder, comment)
        setupReactions(holder, comment)

        if (comment.replies.isNotEmpty()) {
            holder.repliesRecyclerView.layoutManager = LinearLayoutManager(context)
            val repliesAdapter = CommentAdapter(context, comment.replies)
            repliesAdapter.replyButtonClickListener = replyButtonClickListener
            repliesAdapter.editButtonClickListener = editButtonClickListener
            repliesAdapter.deleteButtonClickListener = deleteButtonClickListener
            repliesAdapter.reportButtonClickListener = reportButtonClickListener
            repliesAdapter.likeButtonClickListener = likeButtonClickListener
            repliesAdapter.dislikeButtonClickListener = dislikeButtonClickListener
            repliesAdapter.heartButtonClickListener = heartButtonClickListener
            holder.repliesRecyclerView.adapter = repliesAdapter
            holder.repliesRecyclerView.visibility = View.VISIBLE
        } else {
            holder.repliesRecyclerView.visibility = View.GONE
        }
    }

    private fun setupButtons(holder: CommentViewHolder, comment: Comment) {
        holder.likeButton.setOnClickListener { likeButtonClickListener?.onLikeButtonClick(comment, holder.itemView) }
        holder.dislikeButton.setOnClickListener { dislikeButtonClickListener?.onDislikeButtonClick(comment, holder.itemView) }
        holder.heartButton.setOnClickListener { heartButtonClickListener?.onHeartButtonClick(comment, holder.itemView) }
    }
    private fun setupReactions(holder: CommentViewHolder, comment: Comment) {
        holder.likeCountTextView.text = comment.reactions.count { it.type == EReactionType.LIKE }.toString()
        holder.dislikeCountTextView.text = comment.reactions.count { it.type == EReactionType.DISLIKE }.toString()
        holder.heartCountTextView.text = comment.reactions.count { it.type == EReactionType.HEART }.toString()

        updateReactionCounts(
            context,
            comment,
            holder.likeCountTextView,
            holder.dislikeCountTextView,
            holder.heartCountTextView
        )
        currentUser?.let {
            checkUserReaction(
                context,
                it,
                comment,
                holder.likeButton,
                holder.dislikeButton,
                holder.heartButton
            )
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

    companion object {
        fun updateReactionCounts(context: Context,
                                 comment: Comment,
                                 likeCountTextView: TextView,
                                 dislikeCountTextView: TextView,
                                 heartCountTextView: TextView) {
            val token = PreferencesManager.getToken(context) ?: return
            val reactionService = ClientUtils.getReactionService(token)
            val call = reactionService.countReactionsByComment(comment.id)
            call.enqueue(object : Callback<Map<EReactionType, Integer>> {
                override fun onResponse(
                    call: Call<Map<EReactionType, Integer>>,
                    response: Response<Map<EReactionType, Integer>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reactionCounts ->
                            likeCountTextView.text =
                                reactionCounts[EReactionType.LIKE]?.toString() ?: "0"
                            dislikeCountTextView.text =
                                reactionCounts[EReactionType.DISLIKE]?.toString() ?: "0"
                            heartCountTextView.text =
                                reactionCounts[EReactionType.HEART]?.toString() ?: "0"
                        }
                    }
                }

                override fun onFailure(call: Call<Map<EReactionType, Integer>>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        fun checkUserReaction(context: Context,
                              currentUser: User,
                              comment: Comment,
                              likeButton: ImageButton,
                              dislikeButton: ImageButton,
                              heartButton: ImageButton) {
            currentUser?.id?.let {
                val token = PreferencesManager.getToken(context) ?: return
                val reactionService = ClientUtils.getReactionService(token)
                reactionService.findReactionByCommentAndUser(comment.id, it)?.enqueue(object :
                    Callback<Reaction?> {
                    override fun onResponse(call: Call<Reaction?>, response: Response<Reaction?>) {
                        if (response.isSuccessful) {
                            val reaction = response.body()
                            if (reaction != null) {
                                val reactionTypeColorMap = mapOf(
                                    EReactionType.LIKE to Pair(likeButton, R.color.blue),
                                    EReactionType.DISLIKE to Pair(dislikeButton, R.color.blue),
                                    EReactionType.HEART to Pair(heartButton, R.color.red)
                                )

                                reactionTypeColorMap.forEach { (type, pair) ->
                                    val (button, colorResId) = pair
                                    button.post {
                                        val color = ContextCompat.getColor(context, colorResId)
                                        changeReactionColor(context, button, reaction.type == type, color)
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<Reaction?>, t: Throwable) {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        fun changeReactionColor(context: Context, button: ImageButton, reacted: Boolean, color: Int) {
            val finalColor = if (reacted) color else ContextCompat.getColor(context, R.color.grey)
            button.setColorFilter(finalColor)
        }
    }
    private fun fetchUserData(callback: UserCallback) {
        val token = PreferencesManager.getToken(context) ?: return
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                        callback.onUserFetched(user)
                    }
                } else {
                    callback.onUserFetched(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
                callback.onUserFetched(null)
            }
        })
    }

    override fun getItemCount(): Int = comments.size

    private fun showPopupMenu(view: View,
                              comment: Comment,
                              updateButton: Button,
                              commentContentEditText: EditText) {
        val popup = PopupMenu(context, view)

        val menu = popup.menu
        menu.add(0, R.id.reply, 0, context.getString(R.string.reply_to_comment))
        menu.add(0, R.id.edit, 1, context.getString(R.string.edit_comment))
        menu.add(0, R.id.delete, 2, context.getString(R.string.delete_comment))
        menu.add(0, R.id.report, 3, context.getString(R.string.report_comment))


        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.reply -> {
                    replyButtonClickListener?.onReplyButtonClick(comment)
                    true
                }
                R.id.edit -> {
                    editButtonClickListener?.onEditButtonClick(comment, updateButton, commentContentEditText)
                    true
                }
                R.id.delete -> {
                    deleteButtonClickListener?.onDeleteButtonClick(comment)
                    true
                }
                R.id.report -> {
                    reportButtonClickListener?.onReportButtonClick(comment)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}
