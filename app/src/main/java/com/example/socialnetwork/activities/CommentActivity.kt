package com.example.socialnetwork.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.CommentAdapter
import com.example.socialnetwork.model.Comment
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
        comments.add(Comment(R.drawable.smiley_circle, "User1", "12 Jan 2024", "This is an example Comment content. It can be any text that a user might comment on social network app."))
        comments.add(Comment(R.drawable.smiley_circle, "User2", "13 Jan 2024", "Comment content 2"))
        comments.add(Comment(R.drawable.smiley_circle, "User3", "12 Jan 2024", "Comment content 3"))
        comments.add(Comment(R.drawable.smiley_circle, "User4", "13 Jan 2024", "Comment content 4"))
        comments.add(Comment(R.drawable.smiley_circle, "User1", "12 Jan 2024", "Comment content 5"))
        comments.add(Comment(R.drawable.smiley_circle, "User2", "13 Jan 2024", "Comment content 6"))

        val adapter = CommentAdapter(requireContext(), comments)

        commentsListView.adapter = adapter
    }

}