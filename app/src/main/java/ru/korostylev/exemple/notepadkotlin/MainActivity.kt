package ru.korostylev.exemple.notepadkotlin

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import ru.korostylev.exemple.notepadkotlin.fragments.PageFragment

class MainActivity : AppCompatActivity() {

    companion object {
        var numPage: Int = 1
        var exit = false
        var myBackstack: Int = 1
        var numRec = 0
        var pages = arrayOfNulls<PageFragment>(3)

        val TAG_TABS = intArrayOf(0, 1, 2)
        val NAME_TABS = arrayOf("Заметки", "Дела", "Старое")

        // настройки
        private val SETTING_NOTIFICATION_CHECK = "checknotification"
    }

    private var pager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (pages[0] == null) {  // При повороте не пересоздает
            for (i in pages.indices) {
                pages[i] = PageFragment()
                val args = Bundle()
                args.putInt("numPage", i)
                pages[i]!!.arguments = args
            }
        }

        pager = findViewById(R.id.pager)
        pagerAdapter = MyPagerAdapter(supportFragmentManager)
        pager!!.adapter = pagerAdapter
        pager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                while (myBackstack > 1)
                    pages[numPage]!!.back!!.performClick()

                numPage = position
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        pager!!.currentItem = numPage

        val preferences = getPreferences(Context.MODE_PRIVATE)
        if (preferences.getBoolean(SETTING_NOTIFICATION_CHECK, true)) {
            val notification = NotificationHelper()
            notification.createEverydayNotification(this, 8, 0)

            val editor = preferences.edit()
            editor.putBoolean(SETTING_NOTIFICATION_CHECK, false)
            editor.apply()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()

        pages[numPage]!!.back!!.performClick()

        if (exit)
            super.onBackPressed()

        exit = false
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Скрыть клавиатуру
        if (ev!!.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                v.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        return super.dispatchTouchEvent(ev)
    }
}
