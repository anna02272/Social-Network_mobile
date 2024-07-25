package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.Group

class GroupAdapter(context: Context, groups: ArrayList<Group>) :
    ArrayAdapter<Group>(context, R.layout.fragment_group, groups) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val group: Group? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_group, parent, false)
        }

        val nameTextView = view!!.findViewById<TextView>(R.id.nameTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val adminTextView = view.findViewById<TextView>(R.id.adminTextView)

        group?.let {
            nameTextView.text = it.getName()
            descriptionTextView.text = it.getDescription()
            dateTextView.text = it.getCreationDate()
            adminTextView.text = it.getGroupAdmin()
        }

        return view
    }
}
