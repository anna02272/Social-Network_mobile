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
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.Comment

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
        val moreOptionsButton = view.findViewById<ImageButton>(R.id.moreOptionsButton)

        comment?.let {
            profileImage.setImageResource(it.profileImageResource)
            usernameTextView.text = it.username
            dateTextView.text = it.date
            contentTextView.text = it.content
        }

        moreOptionsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }


        return view
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(context, view)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.reply -> {
                    Toast.makeText(context, "Reply to Comment clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.edit -> {
                    Toast.makeText(context, "Edit Comment clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.delete -> {
                    Toast.makeText(context, "Delete Comment clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.report -> {
                    Toast.makeText(context, "Report Comment clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

}
