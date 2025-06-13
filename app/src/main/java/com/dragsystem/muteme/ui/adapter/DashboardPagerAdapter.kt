package com.dragsystem.muteme.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dragsystem.muteme.ui.fragment.HistoricoChamadasFragment
import com.dragsystem.muteme.ui.fragment.HistoricoSMSFragment


class DashboardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if (position == 0) HistoricoChamadasFragment() else HistoricoSMSFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }
}
