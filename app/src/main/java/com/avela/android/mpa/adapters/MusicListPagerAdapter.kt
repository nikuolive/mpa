package com.avela.android.mpa.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.avela.android.mpa.ui.browselibrary.BrowseLibraryFragment
import com.avela.android.mpa.ui.home.HomeFragment
import java.lang.IndexOutOfBoundsException

const val ALBUM_PAGE_INDEX = 0
const val PLAYLIST_PAGE_INDEX = 1

class MusicListPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        ALBUM_PAGE_INDEX to { HomeFragment() },
        PLAYLIST_PAGE_INDEX to { BrowseLibraryFragment() }
    )

    override fun getItemCount(): Int {
        return tabFragmentsCreators.size
    }

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

}