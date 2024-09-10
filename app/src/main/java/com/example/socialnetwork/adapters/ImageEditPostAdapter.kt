package com.example.socialnetwork.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.utils.PreferencesManager
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageEditPostAdapter(
    private val context: Context,
    private var imageDetails: MutableList<Pair<String, String>>,
    private val postId: Long
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
        val (imageId, imageUrl) = imageDetails[position]
        Picasso.get().load(imageUrl).into(holder.imageView)
        holder.deleteButton.setOnClickListener {
            deleteImage(imageId, position)
        }
    }

    override fun getItemCount(): Int = imageDetails.size

    private fun deleteImage(imageId: String, position: Int) {
        val storageRef = FirebaseStorage.getInstance().getReference("post_images/$postId/$imageId")
        storageRef.delete().addOnSuccessListener {
            deleteImageFromBackend(imageId, position)
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to delete image: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteImageFromBackend(imageId: String, position: Int) {
        val token = PreferencesManager.getToken(context) ?: return
        val postService = ClientUtils.getPostService(token)

        val call = postService.deleteImage(imageId.toLong())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    imageDetails.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, imageDetails.size)
                    Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete image: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

