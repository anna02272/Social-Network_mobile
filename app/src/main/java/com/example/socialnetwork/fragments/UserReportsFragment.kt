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

class UserReportsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_reports, container, false)

        fillUserReportsArray(view)

        return view
    }
    private fun fillUserReportsArray(view: View) {
        val userReportsListView = view.findViewById<ListView>(R.id.userReportsListView)

        val reports = ArrayList<Report>()
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))
        reports.add(Report("User",  "12 Jan 2024", "User", "COPYRIGHT_VIOLATION"))

        val adapter = ReportAdapter(
            requireContext(),
            reports,
            getString(R.string.report_for_user),
            getString(R.string.reason_for_reporting),
            getString(R.string.block_user),
            getString(R.string.delete_request))

        userReportsListView.adapter = adapter

    }
}