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
import com.example.socialnetwork.model.entity.Report

class UserReportsFragment : Fragment(),
    ReportAdapter.AcceptButtonClickListener,
    ReportAdapter.DeleteButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener {
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

        val adapter = ReportAdapter(
            requireContext(),
            reports,
            getString(R.string.report_for_user),
            getString(R.string.reason_for_reporting),
            getString(R.string.block_user),
            getString(R.string.delete_request))

        adapter.acceptButtonClickListener = this
        adapter.deleteButtonClickListener = this

        userReportsListView.adapter = adapter

    }

    override fun onAcceptButtonClick() {
        showConfirmationDialog()
    }

    override fun onDeleteButtonClick() {
        showDeleteConfirmationDialog()
    }
    override fun onDialogPositiveClick() {
        Toast.makeText(requireContext(),  "User is blocked or request deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {
        Toast.makeText(requireContext(), "Delete canceled", Toast.LENGTH_SHORT).show()
    }
    private fun showConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_block))
        dialog.listener = this
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_delete_user_report))
        dialog.listener = this
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }
}