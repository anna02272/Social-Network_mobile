package com.example.socialnetwork.adapters

import ImagePagerAdapter
import android.content.Context
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
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EReactionType
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.model.entity.Reaction
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

class PostAdapter(private val mContext: Context, posts: ArrayList<Post>) :
    ArrayAdapter<Post?>(mContext, 0, posts as List<Post?>) {
    private val mPosts: ArrayList<Post> = ArrayList(posts)
    private lateinit var storageReference: StorageReference
    private var currentUser: User? = null
    private var currentToast: Toast? = null

    init {
        initializeFirebaseStorage()
        fetchUserData(object : UserCallback {
            override fun onUserFetched(user: User?) {
                currentUser = user
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
        fun onLikeButtonClick(post: Post)
    }

    interface DislikeButtonClickListener {
        fun onDislikeButtonClick(post: Post)
    }

    interface HeartButtonClickListener {
        fun onHeartButtonClick(post: Post)
    }

    var commentButtonClickListener: CommentButtonClickListener? = null
    var reportButtonClickListener: ReportButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null
    var editButtonClickListener: EditButtonClickListener? = null
    var likeButtonClickListener: LikeButtonClickListener? = null
    var dislikeButtonClickListener: DislikeButtonClickListener? = null
    var heartButtonClickListener: HeartButtonClickListener? = null

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

        val post: Post? = getItem(position)
        post?.let {
            viewHolder.bind(it)
        }


        fetchUserData(object : UserCallback {
            override fun onUserFetched(user: User?) {
                currentUser = user
                post?.let {
                    viewHolder.checkUserReaction(it)
                }
            }
        })

        return view
    }

    inner class ViewHolder(view: View) {
        private val profileImage: ImageView = view.findViewById(R.id.profileImage)
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        private val contentTextView: TextView = view.findViewById(R.id.contentTextView)
        private val commentButton: ImageButton = view.findViewById(R.id.commentButton)
        private val moreOptionsButton: ImageButton = view.findViewById(R.id.moreOptionsButton)
        private val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        val likeButton: ImageButton = view.findViewById(R.id.likeButton)
        val dislikeButton: ImageButton = view.findViewById(R.id.dislikeButton)
        val heartButton: ImageButton = view.findViewById(R.id.heartButton)
        private val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)
        private val dislikeCountTextView: TextView = view.findViewById(R.id.dislikeCountTextView)
        private val heartCountTextView: TextView = view.findViewById(R.id.heartCountTextView)

        fun bind(post: Post) {
            // Load profile image
            usernameTextView.text = post.user?.profileName?.takeIf { it.isNotEmpty() } ?: post.user?.username
            dateTextView.text = post.creationDate.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
            contentTextView.text = post.content

            loadPostImages(post.id, viewPager)

            setupButtons(post)

            likeCountTextView.text = post.reactions.count { it.type == EReactionType.LIKE }.toString()
            dislikeCountTextView.text = post.reactions.count { it.type == EReactionType.DISLIKE }.toString()
            heartCountTextView.text = post.reactions.count { it.type == EReactionType.HEART }.toString()

            updateReactionCounts(post)
            checkUserReaction(post)
        }
        private fun updateReactionCounts(post: Post) {
            val token = PreferencesManager.getToken(context) ?: return
            val reactionService = ClientUtils.getReactionService(token)
            val call = reactionService.countReactionsByPost(post.id)
            call.enqueue(object : Callback<Map<EReactionType, Integer>> {
                override fun onResponse(call: Call<Map<EReactionType, Integer>>, response: Response<Map<EReactionType, Integer>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { reactionCounts ->
                        likeCountTextView.text = reactionCounts[EReactionType.LIKE]?.toString() ?: "0"
                        dislikeCountTextView.text = reactionCounts[EReactionType.DISLIKE]?.toString() ?: "0"
                        heartCountTextView.text = reactionCounts[EReactionType.HEART]?.toString() ?: "0"
                        }
                    }
                }
                override fun onFailure(call: Call<Map<EReactionType, Integer>>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        }
        fun checkUserReaction(post: Post) {
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
                                        changeReactionColor(button, reaction.type == type, color)
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<Reaction?>, t: Throwable) {
                        showToast("Error: ${t.message}")
                    }
                })
            }
        }

        private fun changeReactionColor(button: ImageButton, reacted: Boolean, color: Int) {
            val finalColor = if (reacted) color else ContextCompat.getColor(context, R.color.grey)
            button.setColorFilter(finalColor)
        }

        private fun setupButtons(post: Post) {
            moreOptionsButton.setOnClickListener { showPopupMenu(it, post) }
            commentButton.setOnClickListener { commentButtonClickListener?.onCommentButtonClick(post) }
            likeButton.setOnClickListener { likeButtonClickListener?.onLikeButtonClick(post) }
            dislikeButton.setOnClickListener { dislikeButtonClickListener?.onDislikeButtonClick(post) }
            heartButton.setOnClickListener { heartButtonClickListener?.onHeartButtonClick(post) }
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
                        Log.d("PostAdapter", "User data received: $user")
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

