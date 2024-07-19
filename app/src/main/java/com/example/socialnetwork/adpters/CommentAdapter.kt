package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.Comment

class CommentAdapter(context: Context, comments: ArrayList<Comment>) :
    ArrayAdapter<Comment>(context, R.layout.fragment_comment, comments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val comment: Comment? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_comment, parent, false)
        }

        val profileImage = view!!.findViewById<ImageView>(R.id.profileImage)
        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)

        comment?.let {
            profileImage.setImageResource(it.getProfileImageResource())
            usernameTextView.text = it.getUsername()
            dateTextView.text = it.getDate()
            contentTextView.text = it.getContent()
        }

        return view
    }
}
