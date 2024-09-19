package com.example.socialnetwork.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.socialnetwork.fragments.BlockedMembersFragment
import com.example.socialnetwork.fragments.MembersFragment

class SectionsPagerGroupAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MembersFragment()
            1 -> BlockedMembersFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}