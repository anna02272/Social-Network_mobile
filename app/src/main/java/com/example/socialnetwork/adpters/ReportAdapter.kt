package com.example.socialnetwork.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.socialnetwork.R
import com.example.socialnetwork.model.Report

class ReportAdapter(
    context: Context,
    reports: ArrayList<Report>,
    private val text: String? = null,
    private val reasonText: String? = null,
    private val acceptButtonText: String? = null,
    private val deleteButtonText: String? = null) :
    ArrayAdapter<Report>(context, R.layout.fragment_report, reports) {

    interface AcceptButtonClickListener {
        fun onAcceptButtonClick()
    }
    interface DeleteButtonClickListener {
        fun onDeleteButtonClick()
    }

    var acceptButtonClickListener: ReportAdapter.AcceptButtonClickListener? = null
    var deleteButtonClickListener: ReportAdapter.DeleteButtonClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val report: Report? = getItem(position)

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

        report?.let {
            userTextView.text = it.getFromUser()
            dateTextView.text = it.getCreatedAt()

            if (it.getContent().isNullOrEmpty()) {
                reportContentTextView.visibility = View.GONE
            } else {
                reportContentTextView.text = it.getContent()
            }
            if (it.getReason().isNullOrEmpty()) {
                reasonContentTextView.visibility = View.GONE
            } else {
                reasonContentTextView.text = it.getReason()
            }
            if (text.isNullOrEmpty()) {
                reportTextView.visibility = View.GONE
            } else {
                reportTextView.text = text
            }
            if (reasonText.isNullOrEmpty()) {
                reasonTextView.visibility = View.GONE
            } else {
                reasonTextView.text = reasonText
            }
            if (acceptButtonText.isNullOrEmpty()) {
                acceptButton.visibility = View.GONE
            } else {
                acceptButton.text = acceptButtonText
            }
            if (deleteButtonText.isNullOrEmpty()) {
                deleteButton.visibility = View.GONE
            } else {
                deleteButton.text = deleteButtonText
            }
        }

        acceptButton.setOnClickListener {
            acceptButtonClickListener?.onAcceptButtonClick()
        }

        deleteButton.setOnClickListener {
            deleteButtonClickListener?.onDeleteButtonClick()
        }

        return view
    }
}
