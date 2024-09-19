package com.example.socialnetwork.adapters

import ImagePagerAdapter
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.socialnetwork.R
import com.example.socialnetwork.activities.GroupActivity
import com.example.socialnetwork.activities.UserProfileActivity
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EReactionType
import com.example.socialnetwork.model.entity.Post
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

class PostAdapter(private val mContext: Context, posts: List<Post>) :
    ArrayAdapter<Post?>(mContext, 0, posts as List<Post?>) {
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
        if (FirebaseApp.getApps(mContext).isEmpty()) {
            FirebaseApp.initializeApp(mContext)
        }
        storageReference = FirebaseStorage.getInstance().reference
    }

    interface UserCallback {
        fun onUserFetched(user: User?)
    }

    interface CommentButtonClickListener {
        fun onCommentButtonClick(post: Post)
    }

    interface ReportButtonClickListener {
        fun onReportButtonClick(post: Post)
    }

    interface DeleteButtonClickListener {
        fun onDeleteButtonClick(post: Post)
    }

    interface EditButtonClickListener {
        fun onEditButtonClick(post: Post)
    }

    interface LikeButtonClickListener {
        fun onLikeButtonClick(post: Post, view: View)
    }

    interface DislikeButtonClickListener {
        fun onDislikeButtonClick(post: Post, view: View)
    }

    interface HeartButtonClickListener {
        fun onHeartButtonClick(post: Post, view: View)
    }

    var commentButtonClickListener: CommentButtonClickListener? = null
    var reportButtonClickListener: ReportButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null
    var editButtonClickListener: EditButtonClickListener? = null
    var likeButtonClickListener: LikeButtonClickListener? = null
    var dislikeButtonClickListener: DislikeButtonClickListener? = null
    var heartButtonClickListener: HeartButtonClickListener? = null

    private lateinit var likeButton: ImageButton
    private lateinit var dislikeButton: ImageButton
    private lateinit var heartButton: ImageButton

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_post, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        dislikeButton = view.findViewById(R.id.dislikeButton)
        likeButton = view.findViewById(R.id.likeButton)
        heartButton = view.findViewById(R.id.heartButton)

        val post: Post? = getItem(position)
        post?.let {
            viewHolder.bind(it, view)
        }

        return view
    }
    inner class ViewHolder(view: View) {
        private val profileImageView: ImageView = view.findViewById(R.id.profileImage)
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val groupTextView: TextView = view.findViewById(R.id.groupTextView)
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        private val contentTextView: TextView = view.findViewById(R.id.contentTextView)
        private val commentButton: ImageButton = view.findViewById(R.id.commentButton)
        private val moreOptionsButton: ImageButton = view.findViewById(R.id.moreOptionsButton)
        private val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        private val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)
        private val dislikeCountTextView: TextView = view.findViewById(R.id.dislikeCountTextView)
        private val heartCountTextView: TextView = view.findViewById(R.id.heartCountTextView)

        fun bind(post: Post, view: View) {
            post.user?.id?.let { loadProfileImage(it) }
            usernameTextView.text =
                post.user?.profileName?.takeIf { it.isNotEmpty() } ?: post.user?.username
            val groupName = post.group?.name
            if (groupName.isNullOrEmpty()) {
                groupTextView.visibility = View.GONE
            } else {
                groupTextView.text = context.getString(R.string.group_name_label, groupName)
                groupTextView.visibility = View.VISIBLE
            }

            dateTextView.text =
                post.creationDate.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
            contentTextView.text = post.content

            usernameTextView.setOnClickListener {
                val intent = Intent(mContext, UserProfileActivity::class.java)
                intent.putExtra("user", post.user)
                mContext.startActivity(intent)
            }

            groupTextView.setOnClickListener {
                val intent = Intent(mContext, GroupActivity::class.java)
                intent.putExtra("group", post.group)
                mContext.startActivity(intent)
            }

            loadPostImages(post.id, viewPager)

            setupButtons(post, view)

            setupReactions(post)
        }

        private fun setupButtons(post: Post, view: View) {
            moreOptionsButton.setOnClickListener { showPopupMenu(it, post) }
            commentButton.setOnClickListener { commentButtonClickListener?.onCommentButtonClick(post) }
            likeButton.setOnClickListener { likeButtonClickListener?.onLikeButtonClick(post, view) }
            dislikeButton.setOnClickListener { dislikeButtonClickListener?.onDislikeButtonClick(post, view) }
            heartButton.setOnClickListener { heartButtonClickListener?.onHeartButtonClick(post, view) }
        }

        private fun setupReactions(post: Post){
            likeCountTextView.text =
                post.reactions.count { it.type == EReactionType.LIKE }.toString()
            dislikeCountTextView.text =
                post.reactions.count { it.type == EReactionType.DISLIKE }.toString()
            heartCountTextView.text =
                post.reactions.count { it.type == EReactionType.HEART }.toString()

            updateReactionCounts(context, post, likeCountTextView, dislikeCountTextView, heartCountTextView)
            currentUser?.let { checkUserReaction(context, it, post, likeButton, dislikeButton, heartButton) }
        }

        private fun loadProfileImage(userId: Long) {
            val ref = storageReference!!.child("profile_images/$userId")
            ref.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.smiley_circle)
            }
        }

    }
    companion object {
        fun updateReactionCounts(context: Context,
                                 post: Post,
                                 likeCountTextView: TextView,
                                 dislikeCountTextView: TextView,
                                 heartCountTextView: TextView) {
            val token = PreferencesManager.getToken(context) ?: return
            val reactionService = ClientUtils.getReactionService(token)
            val call = reactionService.countReactionsByPost(post.id)
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
                              post: Post,
                              likeButton: ImageButton,
                              dislikeButton: ImageButton,
                              heartButton: ImageButton) {
            currentUser?.id?.let {
                val token = PreferencesManager.getToken(context) ?: return
                val reactionService = ClientUtils.getReactionService(token)
                reactionService.findReactionByPostAndUser(post.id, it)?.enqueue(object : Callback<Reaction?> {
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

    private fun loadPostImages(postId: Long, viewPager: ViewPager2) {
        val imageFolderRef = storageReference.child("post_images/$postId/")
        imageFolderRef.listAll()
            .addOnSuccessListener { listResult ->
                val imageUrls = mutableListOf<String>()
                val imageItems = listResult.items.size

                if (imageItems == 0) {
                    viewPager.visibility = View.GONE
                } else {
                    listResult.items.forEachIndexed { index, item ->
                        item.downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(uri.toString())
                            if (index == imageItems - 1) {
                                val adapter = ImagePagerAdapter(mContext, imageUrls)
                                viewPager.adapter = adapter
                                viewPager.visibility = View.VISIBLE
                            }
                        }.addOnFailureListener {
                            showToast("Failed to load image: ${it.message}")
                        }
                    }
                }
            }
            .addOnFailureListener {
                showToast("Failed to list images: ${it.message}")
            }
    }

    private fun showPopupMenu(view: View, post: Post) {
        val popup = PopupMenu(mContext, view)

        val menu = popup.menu
        menu.add(0, R.id.edit, 0, mContext.getString(R.string.edit_post))
        menu.add(0, R.id.delete, 1, mContext.getString(R.string.delete_post))
        menu.add(0, R.id.report, 2, mContext.getString(R.string.report_post))

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    editButtonClickListener?.onEditButtonClick(post)
                    true
                }

                R.id.delete -> {
                    deleteButtonClickListener?.onDeleteButtonClick(post)
                    true
                }

                R.id.report -> {
                    reportButtonClickListener?.onReportButtonClick(post)
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