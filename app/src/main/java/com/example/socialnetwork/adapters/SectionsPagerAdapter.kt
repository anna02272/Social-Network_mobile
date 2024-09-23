package com.example.socialnetwork.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.socialnetwork.fragments.BlockedUsersFragment
import com.example.socialnetwork.fragments.CommentReportsFragment
import com.example.socialnetwork.fragments.FriendRequestsFragment
import com.example.socialnetwork.fragments.PostReportsFragment
import com.example.socialnetwork.fragments.UserReportsFragment
import com.example.socialnetwork.model.entity.EUserType

class SectionsPagerAdapter(
    fa: FragmentActivity,
    private val userType: EUserType?) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return if (userType == EUserType.ADMIN) 5 else 3
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FriendRequestsFragment()
            1 -> PostReportsFragment()
            2 -> CommentReportsFragment()
            3 -> if (userType == EUserType.ADMIN) UserReportsFragment()
            else throw IllegalStateException("Unexpected position $position")
            4 -> if (userType == EUserType.ADMIN) BlockedUsersFragment()
            else throw IllegalStateException("Unexpected position $position")
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}