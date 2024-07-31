package com.example.socialnetwork.adpters

import com.example.socialnetwork.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.socialnetwork.activities.CommentActivity
import com.example.socialnetwork.model.EReportReason
import com.example.socialnetwork.model.Post

class PostAdapter(private val mContext: Context, posts: ArrayList<Post>) :
    ArrayAdapter<Post?>(mContext, 0, posts as List<Post?>) {
    private val mPosts: ArrayList<Post>

    init {
        mPosts = posts
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

        post?.let {
            profileImage.setImageResource(it.getProfileImageResource())
            usernameTextView.text = it.getUsername()
            dateTextView.text = it.getDate()
            contentTextView.text = it.getContent()
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

}
