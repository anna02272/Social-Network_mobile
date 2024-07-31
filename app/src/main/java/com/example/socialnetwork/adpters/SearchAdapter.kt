package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.User

class SearchAdapter(context: Context, users: ArrayList<User>) :
    ArrayAdapter<User>(context, R.layout.fragment_search, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val user: User? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_search, parent, false)
        }

        val firstNameTextView = view!!.findViewById<TextView>(R.id.firstNameTextView)
        val lastNameTextView = view!!.findViewById<TextView>(R.id.lastNameTextView)

        user?.let {
            firstNameTextView.text = it.firstName
            lastNameTextView.text = it.lastName
        }

        return view
    }
}
