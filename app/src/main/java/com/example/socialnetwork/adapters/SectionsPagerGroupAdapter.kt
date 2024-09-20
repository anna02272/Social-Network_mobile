package com.example.socialnetwork.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.socialnetwork.fragments.BlockedMembersFragment
import com.example.socialnetwork.fragments.MembersFragment
import com.example.socialnetwork.model.entity.Group

class SectionsPagerGroupAdapter(fa: FragmentActivity, private val group: Group) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MembersFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("group", group)
                }
            }
            1 -> BlockedMembersFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("group", group)
                }
            }
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}