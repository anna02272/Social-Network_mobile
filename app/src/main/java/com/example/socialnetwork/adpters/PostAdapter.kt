package com.example.socialnetwork.adpters

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
import androidx.viewpager.widget.ViewPager
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.Post
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.format.DateTimeFormatter

class PostAdapter(private val mContext: Context, posts: ArrayList<Post>) :
    ArrayAdapter<Post?>(mContext, 0, posts as List<Post?>) {
    private val mPosts: ArrayList<Post>
    private lateinit var storageReference: StorageReference

    init {
        mPosts = ArrayList(posts)
        initializeFirebaseStorage()
    }

    private fun initializeFirebaseStorage() {
        FirebaseApp.initializeApp(context)
        val firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
    }
    interface CommentButtonClickListener {
        fun onCommentButtonClick(post: Post)
    }

    interface ReportButtonClickListener {
        fun onReportButtonClick(post: Post)
    }

    interface DeleteButtonClickListener {
        fun onDeleteButtonClick()
    }

    var commentButtonClickListener: CommentButtonClickListener? = null
    var reportButtonClickListener: ReportButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val post: Post? = getItem(position)

        if (view == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.fragment_post, parent, false)
        }

        val profileImage = view!!.findViewById<ImageView>(R.id.profileImage)
        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val commentButton = view.findViewById<ImageButton>(R.id.commentButton)
        val moreOptionsButton = view.findViewById<ImageButton>(R.id.moreOptionsButton)
        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)

        post?.let { it ->
//            profileImage.setImageResource(it.user.image)
            usernameTextView.text = it.user?.profileName?.takeIf { it.isNotEmpty() } ?: it.user?.username
            val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
            dateTextView.text = it.creationDate.format(formatter)
            contentTextView.text = it.content

            viewPager.adapter = null
            loadPostImages(it.id, viewPager)
        }

        commentButton.setOnClickListener {
            post?.let {
                commentButtonClickListener?.onCommentButtonClick(it)
            }
        }

        moreOptionsButton.setOnClickListener { view ->
            post?.let {
                showPopupMenu(view, it)
            }
        }

        return view
    }
    private fun loadPostImages(postId: Long, viewPager: ViewPager) {
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
                                viewPager.adapter = ImagePagerAdapter(mContext, imageUrls)
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
                    Toast.makeText(mContext, "Edit Post clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.delete -> {
                    deleteButtonClickListener?.onDeleteButtonClick()
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
