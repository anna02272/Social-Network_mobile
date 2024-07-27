package com.example.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.ReportAdapter
import com.example.socialnetwork.model.Report

class CommentReportsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comment_reports, container, false)

        val commentReportsListView = view.findViewById<ListView>(R.id.commentReportsListView)

        val reports = ArrayList<Report>()
        reports.add(Report("User",  "12 Jan 2024", "Text", "BRAKES_RULES"))
        reports.add(Report("User",  "12 Jan 2024", "Text", "BRAKES_RULES"))
        reports.add(Report("User",  "12 Jan 2024", "Text", "BRAKES_RULES"))
        reports.add(Report("User",  "12 Jan 2024", "Text", "BRAKES_RULES"))
        reports.add(Report("User",  "12 Jan 2024", "Text", "BRAKES_RULES"))
        reports.add(Report("User",  "12 Jan 2024", "Text", "BRAKES_RULES"))


        val adapter = ReportAdapter(
            requireContext(),
            reports,
            getString(R.string.report_for_comment),
            getString(R.string.reason_for_reporting),
            getString(R.string.accept),
            getString(R.string.delete_request))

        commentReportsListView.adapter = adapter

        return view
    }

}