package com.example.socialnetwork.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.CommentAdapter
import com.example.socialnetwork.model.entity.Comment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentActivity : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillCommentArray(view)
    }
    private fun fillCommentArray(view: View) {
        val commentsListView: ListView = view.findViewById(R.id.commentsListView)

        val comments = ArrayList<Comment>()

        val adapter = CommentAdapter(requireContext(), comments)

        commentsListView.adapter = adapter
    }

}