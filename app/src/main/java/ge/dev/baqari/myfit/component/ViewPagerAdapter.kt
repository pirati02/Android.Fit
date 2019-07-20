package ge.dev.baqari.myfit.component

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager, private val fragments: MutableList<Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
