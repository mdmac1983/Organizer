package com.mdmac.organizer.ui

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mdmac.organizer.ui.contacts.ContactsFragment
import com.mdmac.organizer.ui.notes.NotesFragment
import com.mdmac.organizer.ui.passwords.PasswordsFragment
import com.mdmac.organizer.ui.planner.PlannerFragment

class OrganizerPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> PlannerFragment()
        1 -> ContactsFragment()
        2 -> PasswordsFragment()
        3 -> NotesFragment()
        else -> throw IllegalArgumentException("Invalid tab position: $position")
    }
}
