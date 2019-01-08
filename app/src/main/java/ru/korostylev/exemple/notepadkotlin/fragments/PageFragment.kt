package ru.korostylev.exemple.notepadkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import ru.korostylev.exemple.notepadkotlin.MainActivity
import ru.korostylev.exemple.notepadkotlin.R

class PageFragment: Fragment() {
    var back: ImageButton? = null
    var logotipe: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true // Отмена пересоздания при повороте
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.fragment_page, container, false)

        var numPage: Int
        if (arguments != null)
            numPage = arguments!!.getInt("numPage")
        else
            numPage = MainActivity.numPage

        logotipe = view.findViewById(R.id.page_logo)
        back = view.findViewById(R.id.page_back)
        back!!.setOnClickListener {
            if (MainActivity.myBackstack == 1)
                MainActivity.exit = true
            else if (MainActivity.myBackstack == 2 || MainActivity.myBackstack == 3) {
                val records = RecordsFragment()
                val args = Bundle()
                args.putInt("numPage", numPage)
                records.arguments = args

                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.page_container, records)
                transaction.commit()

                logotipe!!.visibility = View.VISIBLE
                back!!.visibility = View.GONE
                MainActivity.myBackstack = 1 // MAIN
            } else if (MainActivity.myBackstack == 4) {
                val fragment = RecordFragment()
                val args = Bundle()
                args.putInt("tabID", numPage)
                args.putInt("recordID", MainActivity.numRec)
                fragment.arguments = args

                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.page_container, fragment)
                transaction.commit()

                MainActivity.myBackstack = 2 // "Информация"
            }
        }

        val records = RecordsFragment()
        val args = Bundle()
        args.putInt("numPage", numPage)
        records.arguments = args

        val transaction = childFragmentManager.beginTransaction()
        if (childFragmentManager.findFragmentById(R.id.page_container) == null)
            transaction.add(R.id.page_container, records)
        else
            transaction.replace(R.id.page_container, records)
        transaction.commit()

        logotipe!!.visibility = View.VISIBLE
        back!!.visibility = View.GONE
        MainActivity.myBackstack = 1 // MAIN

        return view
    }
}