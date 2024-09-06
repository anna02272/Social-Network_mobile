package com.example.socialnetwork.adpters

import ImagePagerAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.Post
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.format.DateTimeFormatter

class PostAdapter(private val mContext: Context, posts: ArrayList<Post>) :
    ArrayAdapter<Post?>(mContext, 0, posts as List<Post?>) {
    private val mPosts: ArrayList<Post> = ArrayList(posts)
    private lateinit var storageReference: StorageReference

    init {
        initializeFirebaseStorage()
    }

    private fun initializeFirebaseStorage() {
        if (FirebaseApp.getApps(mContext).isEmpty()) {
            FirebaseApp.initializeApp(mContext)
        }
        storageReference = FirebaseStorage.getInstance().reference
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

    var commentButtonClickListener: CommentButtonClickListener? = null
    var reportButtonClickListener: ReportButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null
    var editButtonClickListener: EditButtonClickListener? = null

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

        fun bind(post: Post) {
            // Load profile image
            usernameTextView.text = post.user?.profileName?.takeIf { it.isNotEmpty() } ?: post.user?.username
            dateTextView.text = post.creationDate.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
            contentTextView.text = post.content

            loadPostImages(post.id, viewPager)

            commentButton.setOnClickListener {
                commentButtonClickListener?.onCommentButtonClick(post)
            }

            moreOptionsButton.setOnClickListener {
                showPopupMenu(it, post)
            }
        }
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
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
