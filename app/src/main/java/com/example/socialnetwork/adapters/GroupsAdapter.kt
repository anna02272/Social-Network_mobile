package com.example.socialnetwork.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.example.socialnetwork.R
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EUserType
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.GroupRequest
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GroupsAdapter(context: Context, groups: ArrayList<Group>) :
    ArrayAdapter<Group>(context, R.layout.fragment_group, groups) {
    private var currentUser: User? = null
    private var currentToast: Toast? = null
    init {
        fetchUserData()
    }
    interface DeleteButtonClickListener {
        fun onDeleteButtonClick(groupId: Long?)
    }

    interface GroupRequestButtonClickListener {
        fun onGroupRequestButtonClick(group: Group)
    }

    interface OpenGroupButtonClickListener {
        fun onOpenGroupButtonClick(group: Group)
    }

    var deleteButtonClickListener: DeleteButtonClickListener? = null
    var groupRequestButtonClickListener: GroupRequestButtonClickListener? = null
    var openGroupButtonClickListener: OpenGroupButtonClickListener? = null

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
        val joinButton = view.findViewById<Button>(R.id.joinButton)

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

        joinButton.setOnClickListener { view ->
            group?.let {
                groupRequestButtonClickListener?.onGroupRequestButtonClick(it)
            }
        }

        nameTextView.setOnClickListener { view ->
            group?.let {
                openGroupButtonClickListener?.onOpenGroupButtonClick(it)
            }
        }

        return view
    }
    private fun fetchUserData() {
        val token = PreferencesManager.getToken(context) ?: return
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                    }
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast( "Error: ${t.message}")
            }
        })
    }

    private fun showPopupMenu(view: View, groupId: Long?) {
        val popup = PopupMenu(context, view)

        val menu = popup.menu
        menu.add(0, R.id.edit, 1, context.getString(R.string.edit_group))
        if (currentUser?.type == EUserType.ADMIN) {
            menu.add(0, R.id.delete, 2, context.getString(R.string.suspend_group))
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
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

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

}
