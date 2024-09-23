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
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.utils.CircleTransform
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.time.format.DateTimeFormatter

class ReportAdapter(
    context: Context,
    reports: ArrayList<Report>,
    private val text: String? = null,
    private val reasonText: String? = null,
    private val acceptButtonText: String? = null,
    private val deleteButtonText: String? = null) :
    ArrayAdapter<Report>(context, R.layout.fragment_report, reports) {
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
        fun onAcceptButtonClick(report: Report)
    }
    interface DeleteButtonClickListener {
        fun onDeleteButtonClick(reportId: Long)
    }

    var acceptButtonClickListener: AcceptButtonClickListener? = null
    var deleteButtonClickListener: DeleteButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val report: Report? = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_report, parent, false)
        }

        val profileImageView: ImageView = view!!.findViewById(R.id.reportProfileImage)
        val userTextView = view.findViewById<TextView>(R.id.userTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val reportContentTextView = view.findViewById<TextView>(R.id.reportContentTextView)
        val reasonContentTextView = view.findViewById<TextView>(R.id.reasonContentTextView)
        val reportTextView = view.findViewById<TextView>(R.id.reportTextView)
        val reasonTextView = view.findViewById<TextView>(R.id.reasonTextView)
        val acceptButton = view.findViewById<Button>(R.id.acceptButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)


        report?.let {
            try {
                it.user.id?.let { it1 -> loadProfileImage(it1, profileImageView) }

                userTextView.text = it.user.username
                dateTextView.text = it.timestamp.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))

                val postContent = it.post?.content
                val commentText = it.comment?.text
                val reportedUser = it.reportedUser?.username

                when {
                    !postContent.isNullOrEmpty() -> {
                        reportContentTextView.text = postContent
                        reportContentTextView.visibility = View.VISIBLE
                    }
                    !commentText.isNullOrEmpty() -> {
                        reportContentTextView.text = commentText
                        reportContentTextView.visibility = View.VISIBLE
                    }
                    !reportedUser.isNullOrEmpty() -> {
                        reportContentTextView.text = reportedUser
                        reportContentTextView.visibility = View.VISIBLE
                    }
                    else -> {
                        reportContentTextView.text = ""
                        reportContentTextView.visibility = View.GONE
                    }
                }

                reasonContentTextView.visibility =
                    if (it.reason.name.isNullOrEmpty()) View.GONE else View.VISIBLE
                reasonContentTextView.text = it.reason.name

                reportTextView.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
                reportTextView.text = text

                reasonTextView.visibility =
                    if (reasonText.isNullOrEmpty()) View.GONE else View.VISIBLE
                reasonTextView.text = reasonText

                acceptButton.visibility =
                    if (acceptButtonText.isNullOrEmpty()) View.GONE else View.VISIBLE
                acceptButton.text = acceptButtonText

                deleteButton.visibility =
                    if (deleteButtonText.isNullOrEmpty()) View.GONE else View.VISIBLE
                deleteButton.text = deleteButtonText
            } catch (e: Exception) {
                Log.e("ReportAdapter", "Error binding view: ${e.message}", e)
            }
        }
        acceptButton.setOnClickListener {
            report?.let {
                it.let { acceptButtonClickListener?.onAcceptButtonClick(it) }
            }
        }

        deleteButton.setOnClickListener {
            report?.let {
                it.id?.let { it1 -> deleteButtonClickListener?.onDeleteButtonClick(it1) }
            }
        }

        return view
    }

    private fun loadProfileImage(userId: Long, profileImageView: ImageView) {
        val ref = storageReference!!.child("profile_images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.smiley_circle)
        }
    }
}