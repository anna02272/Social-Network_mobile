package com.example.socialnetwork.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialnetwork.R
import com.example.socialnetwork.activities.LoginActivity
import com.example.socialnetwork.adpters.ReportAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserReportsFragment : Fragment(),
    ReportAdapter.AcceptButtonClickListener,
    ReportAdapter.DeleteButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_reports, container, false)

        val token = PreferencesManager.getToken(requireContext())
        if (token == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return view
        }
        fetchReportsFromServer(token)

        return view
    }

    private fun fetchReportsFromServer(token: String) {
        val reportService = ClientUtils.getReportService(token)
        val call = reportService.getAllReportsForUsers()

        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(
                call: Call<List<Report>>,
                response: Response<List<Report>>
            ) {
                if (response.isSuccessful) {
                    val reports = response.body() ?: arrayListOf()
                    updateListView(reports)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    showToast("Failed to load reports")
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun handleTokenExpired() {
        PreferencesManager.clearToken(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateListView(reports: List<Report>) {
        val filteredReports = reports.filter { report ->
            !report.isDeleted
        }
        val listView: ListView = requireView().findViewById(R.id.userReportsListView)
        val adapter = ReportAdapter(
            requireContext(),
            ArrayList(filteredReports),
            getString(R.string.report_for_user),
            getString(R.string.reason_for_reporting),
            getString(R.string.block_user),
            getString(R.string.delete_request))

        adapter.deleteButtonClickListener = this
        adapter.acceptButtonClickListener = this
        listView.adapter = adapter
    }

    override fun onAcceptButtonClick() {
        showConfirmationDialog()
    }

    override fun onDeleteButtonClick() {
        showDeleteConfirmationDialog()
    }
    override fun onDialogPositiveClick() {
        showToast("User is blocked or request deleted")
    }

    override fun onDialogNegativeClick() {
        showToast("Delete canceled")
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
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}