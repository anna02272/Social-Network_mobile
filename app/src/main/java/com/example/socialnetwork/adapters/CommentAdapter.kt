package com.example.socialnetwork.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.Comment
import java.time.format.DateTimeFormatter

class CommentAdapter(
    private val context: Context,
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val moreOptionsButton: ImageButton = itemView.findViewById(R.id.moreOptionsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.usernameTextView.text = comment.user?.profileName?.takeIf { it.isNotEmpty() } ?: comment.user?.username
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        holder.dateTextView.text = comment.timeStamp.format(formatter)
        holder.contentTextView.text = comment.text

        holder.moreOptionsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    override fun getItemCount(): Int = comments.size

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
