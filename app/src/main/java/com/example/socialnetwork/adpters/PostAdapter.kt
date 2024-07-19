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
import android.widget.TextView
import com.example.socialnetwork.activities.CommentActivity
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

    var commentButtonClickListener: CommentButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val post: Post? = getItem(position)

        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.fragment_post, parent, false)
        }

        val profileImage = convertView!!.findViewById<ImageView>(R.id.profileImage)
        val usernameTextView = convertView.findViewById<TextView>(R.id.usernameTextView)
        val dateTextView = convertView.findViewById<TextView>(R.id.dateTextView)
        val contentTextView = convertView.findViewById<TextView>(R.id.contentTextView)
        val commentButton = convertView.findViewById<ImageButton>(R.id.commentButton)


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

        return convertView

    }
}
