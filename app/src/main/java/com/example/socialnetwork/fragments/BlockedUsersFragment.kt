package com.example.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.FriendRequestAdapter
import com.example.socialnetwork.adpters.ReportAdapter
import com.example.socialnetwork.model.FriendRequest
import com.example.socialnetwork.model.Report

class BlockedUsersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blocked_users, container, false)

        val blockedUsersListView = view.findViewById<ListView>(R.id.blockedUsersListView)

        val reports = ArrayList<Report>()
        reports.add(Report("User",  "12 Jan 2024"))
        reports.add(Report("User",  "12 Jan 2024"))
        reports.add(Report("User",  "12 Jan 2024"))
        reports.add(Report("User",  "12 Jan 2024"))
        reports.add(Report("User",  "12 Jan 2024"))


        val adapter = ReportAdapter(
            requireContext(),
            reports,
            null,
            null,
            getString(R.string.unblock_user),
            null)

        blockedUsersListView.adapter = adapter

        return view
    }

}