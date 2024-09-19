package com.example.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.model.entity.Group
import java.time.format.DateTimeFormatter

class GroupFragment : Fragment() {
    private lateinit var group: Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        @Suppress("DEPRECATION")
        arguments?.let {
            group = it.getParcelable("group")!!
        }

        bindGroupData(view)

        return view
    }

    private fun bindGroupData(view: View) {
        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val adminTextView = view.findViewById<TextView>(R.id.adminTextView)

        nameTextView.text = group.name
        descriptionTextView.text = group.description
        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        dateTextView.text = group.creationDate.format(formatter)

        if (group.groupAdmin.isNotEmpty()) {
            val adminNames = group.groupAdmin.joinToString { it.user?.username ?: "" }
            adminTextView.text = adminNames
        } else {
            adminTextView.text = "No admins"
        }
    }
    companion object {
        fun newInstance(group: Group): GroupFragment {
            val fragment = GroupFragment()
            val args = Bundle()
            args.putParcelable("group", group)
            fragment.arguments = args
            return fragment
        }
    }
}
