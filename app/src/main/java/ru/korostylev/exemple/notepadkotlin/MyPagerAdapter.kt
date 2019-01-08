package ru.korostylev.exemple.notepadkotlin

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MyPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return MainActivity.pages[position]!!
    }

    override fun getCount(): Int {
        return MainActivity.pages.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return MainActivity.NAME_TABS[position]
    }
}