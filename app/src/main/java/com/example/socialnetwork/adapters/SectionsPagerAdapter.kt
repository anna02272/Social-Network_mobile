package com.example.socialnetwork.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.socialnetwork.fragments.BlockedUsersFragment
import com.example.socialnetwork.fragments.CommentReportsFragment
import com.example.socialnetwork.fragments.FriendRequestsFragment
import com.example.socialnetwork.fragments.PostReportsFragment
import com.example.socialnetwork.fragments.UserReportsFragment

class SectionsPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 5
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FriendRequestsFragment()
            1 -> PostReportsFragment()
            2 -> CommentReportsFragment()
            3 -> UserReportsFragment()
            4 -> BlockedUsersFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}