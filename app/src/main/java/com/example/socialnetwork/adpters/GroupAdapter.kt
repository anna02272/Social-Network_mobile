package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.Group

class GroupAdapter(context: Context, groups: ArrayList<Group>) :
    ArrayAdapter<Group>(context, R.layout.fragment_group, groups) {

    interface DeleteButtonClickListener {
        fun onDeleteButtonClick()
    }

    var deleteButtonClickListener: GroupAdapter.DeleteButtonClickListener? = null
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
        val moreOptionsButton = view.findViewById<ImageButton>(R.id.moreOptionsButton)

        group?.let {
            nameTextView.text = it.name
            descriptionTextView.text = it.description
            dateTextView.text = it.creationDate
            adminTextView.text = it.groupAdmin
        }

        moreOptionsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        return view
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(context, view)

        val menu = popup.menu
        menu.add(0, R.id.edit, 1, context.getString(R.string.edit_group))
        menu.add(0, R.id.delete, 2, context.getString(R.string.suspend_group))

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    Toast.makeText(context, "Edit Group clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.delete -> {
                    deleteButtonClickListener?.onDeleteButtonClick()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}
