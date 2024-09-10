package com.example.socialnetwork.adapters

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
import java.time.format.DateTimeFormatter

class GroupAdapter(context: Context, groups: ArrayList<Group>) :
    ArrayAdapter<Group>(context, R.layout.fragment_group, groups) {

    interface DeleteButtonClickListener {
        fun onDeleteButtonClick(groupId: Long?)
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

        group?.let { it ->
            nameTextView.text = it.name
            descriptionTextView.text = it.description
            val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
            dateTextView.text = it.creationDate.format(formatter)
            if (group.groupAdmin.isNotEmpty()) {
                val adminNames = group.groupAdmin.joinToString { it.user?.username ?: "" }
                adminTextView.text = "$adminNames"
            } else {
                adminTextView.text = "No admins"
            }
        }

        moreOptionsButton.setOnClickListener { view ->
            showPopupMenu(view, group?.id)
        }

        return view
    }

    private fun showPopupMenu(view: View, groupId: Long?) {
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
                    deleteButtonClickListener?.onDeleteButtonClick(groupId)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}
