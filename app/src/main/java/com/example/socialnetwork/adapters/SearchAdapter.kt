package com.example.socialnetwork.adapters

import android.content.Context
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

class SearchAdapter(context: Context, users: ArrayList<User>) :
    ArrayAdapter<User>(context, R.layout.fragment_search, users) {
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

    interface AddFriendButtonClickListener {
        fun onAddFriendButtonClick(user: User)
    }
    var addFriendButtonClickListener: AddFriendButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val user: User? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_search, parent, false)
        }

        val firstNameTextView = view!!.findViewById<TextView>(R.id.firstNameTextView)
        val lastNameTextView = view.findViewById<TextView>(R.id.lastNameTextView)
        val addFriendButton = view.findViewById<Button>(R.id.addFriendButton)
        val profileImageView = view.findViewById<ImageView>(R.id.searchProfileImage)

        user?.let {
            firstNameTextView.text = it.firstName
            lastNameTextView.text = it.lastName
            loadProfileImage(it, profileImageView)
        }

        addFriendButton.setOnClickListener {
            if (user != null) {
                addFriendButtonClickListener?.onAddFriendButtonClick(user)
            }
        }

        return view
    }
    private fun loadProfileImage(user: User, profileImageView: ImageView) {
        val ref = storageReference.child("profile_images/${user.id}")
        ref.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.smiley_circle)
        }
    }


}
