package com.example.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.ReportAdapter
import com.example.socialnetwork.model.Report

class BlockedUsersFragment : Fragment(),
    ReportAdapter.AcceptButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blocked_users, container, false)

        fillBlockedUsersArray(view)

        return view
    }
    private fun fillBlockedUsersArray(view: View) {
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

        adapter.acceptButtonClickListener = this

        blockedUsersListView.adapter = adapter
    }

    override fun onAcceptButtonClick() {
        showConfirmationDialog()
    }
    override fun onDialogPositiveClick() {
        Toast.makeText(requireContext(),  "User is unblocked", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {
        Toast.makeText(requireContext(), "Unblocking canceled", Toast.LENGTH_SHORT).show()
    }
    private fun showConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_unblock))
        dialog.listener = this
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }
}