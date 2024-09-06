package com.example.socialnetwork.adpters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.FriendRequest

class FriendRequestAdapter(
    context: Context,
    friendRequest: ArrayList<FriendRequest>,
    private val acceptButtonText: String? = null,
    private val deleteButtonText: String? = null) :
    ArrayAdapter<FriendRequest>(context, R.layout.fragment_report, friendRequest) {

    interface AcceptButtonClickListener {
        fun onAcceptButtonClick(friendRequestId: Long)
    }
    interface DeleteButtonClickListener {
        fun onDeleteButtonClick(friendRequestId: Long)
    }

    var acceptButtonClickListener: AcceptButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val friendRequest: FriendRequest? = getItem(position)

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

        friendRequest?.let {
            try {
                userTextView.text = it.fromUser.firstName + " " + it.fromUser.lastName
                dateTextView.text = it.created_at.toString()

                reportContentTextView.visibility = View.GONE
                reasonContentTextView.visibility = View.GONE
                reportTextView.visibility = View.GONE
                reasonTextView.visibility = View.GONE

                acceptButton.visibility =
                    if (acceptButtonText.isNullOrEmpty()) View.GONE else View.VISIBLE
                acceptButton.text = acceptButtonText

                deleteButton.visibility =
                    if (deleteButtonText.isNullOrEmpty()) View.GONE else View.VISIBLE
                deleteButton.text = deleteButtonText

            } catch (e: Exception) {
                Log.e("FriendRequestAdapter", "Error binding view: ${e.message}", e)
            }
        }
        acceptButton.setOnClickListener {
            friendRequest?.let {
                it.id?.let { it1 -> acceptButtonClickListener?.onAcceptButtonClick(it1) }
            }
        }

        deleteButton.setOnClickListener {
            friendRequest?.let {
                it.id?.let { it1 -> deleteButtonClickListener?.onDeleteButtonClick(it1) }
            }
        }

        return view
    }
}