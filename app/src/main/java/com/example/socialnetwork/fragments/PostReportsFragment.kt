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

class PostReportsFragment : Fragment(),
    ReportAdapter.DeleteButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_reports, container, false)

        fillPostReportsArray(view)

        return view
    }
    private fun fillPostReportsArray(view: View) {
        val postReportsListView = view.findViewById<ListView>(R.id.postReportsListView)

        val reports = ArrayList<Report>()


        val adapter = ReportAdapter(
            requireContext(),
            reports,
            getString(R.string.report_for_post),
            getString(R.string.reason_for_reporting),
            getString(R.string.accept),
            getString(R.string.delete_request))

        adapter.deleteButtonClickListener = this

        postReportsListView.adapter = adapter
    }

    override fun onDeleteButtonClick() {
        showConfirmationDialog()
    }
    override fun onDialogPositiveClick() {
        Toast.makeText(requireContext(),  "Request is deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {
        Toast.makeText(requireContext(), "Delete canceled", Toast.LENGTH_SHORT).show()
    }
    private fun showConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_delete_post_report))
        dialog.listener = this
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }
}