package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.FriendRequest

class FriendRequestAdapter(context: Context, friendRequests: ArrayList<FriendRequest>) :
    ArrayAdapter<FriendRequest>(context, R.layout.fragment_request, friendRequests) {

    interface DeleteButtonClickListener {
        fun onDeleteButtonClick()
    }

    var deleteButtonClickListener: FriendRequestAdapter.DeleteButtonClickListener? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val friendRequest: FriendRequest? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_request, parent, false)
        }

        val userTextView = view!!.findViewById<TextView>(R.id.userTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)

        friendRequest?.let {
            userTextView.text = it.getFromUser()
            dateTextView.text = it.getCreatedAt()
        }

        deleteButton.setOnClickListener {
            deleteButtonClickListener?.onDeleteButtonClick()
        }

        return view
    }
}
