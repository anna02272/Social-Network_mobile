package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.squareup.picasso.Picasso

class ImageEditPostAdapter(
    private val context: Context,
    private var imageUrls: List<String>
) : RecyclerView.Adapter<ImageEditPostAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_post_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Picasso.get().load(imageUrls[position]).into(holder.imageView)
        // holder.deleteButton.setOnClickListener { onDeleteClick(imageUrls[position]) }
    }

    override fun getItemCount(): Int = imageUrls.size
}
