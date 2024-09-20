package com.example.socialnetwork.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.CircleTransform
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class MemberAdapter(
    context: Context,
    members: ArrayList<User>,
    private val acceptButtonText: String? = null
) : ArrayAdapter<User>(context, R.layout.fragment_report, members) {
    private lateinit var storageReference: StorageReference

    init {
        initializeFirebaseStorage()
    }

    private fun initializeFirebaseStorage() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        storageReference = FirebaseStorage.getInstance().reference
    }

    interface AcceptButtonClickListener {
        fun onAcceptButtonClick(userId: Long)
    }

    var acceptButtonClickListener: AcceptButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val member: User? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_report, parent, false)
        }

        val userTextView = view!!.findViewById<TextView>(R.id.userTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val reportContentTextView = view.findViewById<TextView>(R.id.reportContentTextView)
        val reasonContentTextView = view.findViewById<TextView>(R.id.reasonContentTextView)
        val reportTextView = view.findViewById<TextView>(R.id.reportTextView)
        val reasonTextView = view.findViewById<TextView>(R.id.reasonTextView)
        val acceptButton = view.findViewById<Button>(R.id.acceptButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
        val profileImageView = view.findViewById<ImageView>(R.id.reportProfileImage)

        member?.let {
            try {
                userTextView.text = it.username
                it.id?.let { it1 -> loadProfileImage(it1, profileImageView) }

                acceptButton.visibility =
                    if (acceptButtonText.isNullOrEmpty()) View.GONE else View.VISIBLE
                acceptButton.text = acceptButtonText

                dateTextView.visibility = View.GONE
                reportContentTextView.visibility = View.GONE
                reasonContentTextView.visibility = View.GONE
                reportTextView.visibility = View.GONE
                reasonTextView.visibility = View.GONE
                deleteButton.visibility = View.GONE

            } catch (e: Exception) {
                Log.e("MemberAdapter", "Error binding view: ${e.message}", e)
            }
        }
        acceptButton.setOnClickListener {
            member?.let {
                it.id?.let { it1 -> acceptButtonClickListener?.onAcceptButtonClick(it1) }
            }
        }

        return view
    }

    private fun loadProfileImage(userId: Long, profileImageView: ImageView) {
        val ref = storageReference.child("profile_images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.smiley_circle)
        }
    }
}
