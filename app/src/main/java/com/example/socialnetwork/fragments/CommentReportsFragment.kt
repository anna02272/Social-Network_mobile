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
import com.example.socialnetwork.adapters.ReportAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EUserType
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentReportsFragment : Fragment(),
    ReportAdapter.DeleteButtonClickListener,
    ReportAdapter.AcceptButtonClickListener {
    private var currentUser: User? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comment_reports, container, false)

        val token = PreferencesManager.getToken(requireContext())
        if (token == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return view
        }
        fetchUserData()
        fetchReportsFromServer(token)

        return view
    }

    private fun fetchReportsFromServer(token: String) {
        val reportService = ClientUtils.getReportService(token)
        val call = reportService.getAllReportsForComments()

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
            !report.isDeleted &&
                    (currentUser?.type == EUserType.ADMIN ||
                            report.comment?.post?.group?.groupAdmin?.any { it.user?.username == currentUser?.username } == true)
        }

        val listView: ListView = requireView().findViewById(R.id.commentReportsListView)
        val adapter = ReportAdapter(
            requireContext(),
            ArrayList(filteredReports),
            getString(R.string.report_for_comment),
            getString(R.string.reason_for_reporting),
            getString(R.string.accept),
            getString(R.string.delete_request))

        adapter.deleteButtonClickListener = this
        adapter.acceptButtonClickListener = this
        listView.adapter = adapter
    }

    override fun onDeleteButtonClick(reportId: Long) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val reportService = ClientUtils.getReportService(token)

        reportService.declineReport(reportId).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    showToast("Report is deleted")
                    fetchReportsFromServer(token)
                } else {
                    showToast("Failed to decline report")
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    override fun onAcceptButtonClick(report: Report) {
        val token = PreferencesManager.getToken(requireContext()) ?: return
        val reportService = ClientUtils.getReportService(token)

        report.id?.let {
            reportService.approveReport(it).enqueue(object : Callback<Report> {
                override fun onResponse(call: Call<Report>, response: Response<Report>) {
                    if (response.isSuccessful) {
                        showToast("Report approved")
                        fetchReportsFromServer(token)
                    } else {
                        showToast("Failed to approve report")
                    }
                }

                override fun onFailure(call: Call<Report>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        }
    }

    private fun fetchUserData() {
        val token = PreferencesManager.getToken(requireContext()) ?: return
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
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}