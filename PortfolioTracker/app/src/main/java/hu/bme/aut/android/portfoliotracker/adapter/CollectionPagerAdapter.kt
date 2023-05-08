package hu.bme.aut.android.portfoliotracker.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import hu.bme.aut.android.portfoliotracker.fragments.FavoritesFragment
import hu.bme.aut.android.portfoliotracker.fragments.HistoryFragment
import hu.bme.aut.android.portfoliotracker.fragments.LandingPageFragment


class CollectionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        when( position )
        {
            0 -> return FavoritesFragment()
            1 -> return LandingPageFragment()
            2 -> return HistoryFragment()
        }

        return Fragment()
    }


}