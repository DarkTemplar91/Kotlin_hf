package hu.bme.aut.android.portfoliotracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.adapter.CollectionPagerAdapter
import hu.bme.aut.android.portfoliotracker.databinding.FragmentMainPageBinding


class MainPageFragment : Fragment() {
    private  lateinit var binding : FragmentMainPageBinding

    private lateinit var collectionPagerAdapter: CollectionPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout : TabLayout
    private var startUpFirst = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        collectionPagerAdapter = CollectionPagerAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = collectionPagerAdapter


        tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, _ ->
            tab.text = ""
            tab.setIcon(R.drawable.tab_indicator_default)
        }.attach()
        viewPager.registerOnPageChangeCallback(PageListener(tabLayout))

        if(startUpFirst) {
            tabLayout.getTabAt(1)?.select()
            tabLayout.getTabAt(1)?.setIcon(R.drawable.tab_indicator_selected)
            startUpFirst = false
        }

    }

    class PageListener(private val tabLayout: TabLayout) : ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {

            when( position )
            {
                0 -> {
                    tabLayout.getTabAt(0)?.setIcon(R.drawable.tab_indicator_selected)
                    tabLayout.getTabAt(1)?.setIcon(R.drawable.tab_indicator_default)
                    tabLayout.getTabAt(2)?.setIcon(R.drawable.tab_indicator_default)
                }
                1->{
                    tabLayout.getTabAt(0)?.setIcon(R.drawable.tab_indicator_default)
                    tabLayout.getTabAt(1)?.setIcon(R.drawable.tab_indicator_selected)
                    tabLayout.getTabAt(2)?.setIcon(R.drawable.tab_indicator_default)
                }
                2->{
                    tabLayout.getTabAt(0)?.setIcon(R.drawable.tab_indicator_default)
                    tabLayout.getTabAt(1)?.setIcon(R.drawable.tab_indicator_default)
                    tabLayout.getTabAt(2)?.setIcon(R.drawable.tab_indicator_selected)
                }
            }
            super.onPageSelected(position)
        }
    }

}