package ru.korostylev.exemple.notepadkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import ru.korostylev.exemple.notepadkotlin.DBHelper
import ru.korostylev.exemple.notepadkotlin.MainActivity
import ru.korostylev.exemple.notepadkotlin.R
import ru.korostylev.exemple.notepadkotlin.adapters.CasesAdapter
import ru.korostylev.exemple.notepadkotlin.items.CaseItem
import java.util.*

class RecordsFragment: Fragment() {

    private var dbHelper: DBHelper? = null
    private var idRecords: ArrayList<Int> = ArrayList()
    private var arrayList: ArrayList<String> = ArrayList()
    private var arrayCases: ArrayList<CaseItem> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_records, container, false)

        dbHelper = DBHelper(context!!)

        val tabID = arguments!!.getInt("numPage", MainActivity.numPage)
        var records: ListView = view.findViewById(R.id.list_records)

        readFromDB(tabID)

        if (tabID == MainActivity.TAG_TABS[0]) {
            val adapter = ArrayAdapter<String>(context, R.layout.item_note, arrayList)
            records.adapter = adapter
        } else {
            records.adapter = CasesAdapter(context!!, arrayCases)
        }

        records.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val fragment = RecordFragment()
            val args = Bundle()
            args.putInt("tabID", tabID)
            args.putInt("recordID", idRecords[i])
            fragment.arguments = args

            val transaction = parentFragment!!.childFragmentManager.beginTransaction()
            transaction.replace(R.id.page_container, fragment)
            transaction.commit()

            MainActivity.pages[tabID]!!.logotipe!!.visibility = View.GONE
            MainActivity.pages[tabID]!!.back!!.visibility = View.VISIBLE
            MainActivity.myBackstack = 2 // "Информация"
        }

        val toRecordCreate: Button = view.findViewById(R.id.button_by_records)
        if (tabID == MainActivity.TAG_TABS[2]) {
            toRecordCreate.text = "Удалить старое"
            toRecordCreate.setOnClickListener {
                deleteOldRecords()

                val records = RecordsFragment()
                val args = Bundle()
                args.putInt("numPage", tabID)
                records.arguments = args
                val transaction = parentFragment!!.childFragmentManager.beginTransaction()
                transaction.replace(R.id.page_container, records)
                transaction.commit()

                MainActivity.myBackstack = 1 // MAIN
            }
        } else {
            toRecordCreate.setOnClickListener {
                val fragment = RecordCreaterFragment()
                val args = Bundle()
                args.putInt("tabID", tabID)
                fragment.arguments = args

                val transaction = parentFragment!!.childFragmentManager.beginTransaction()
                transaction.replace(R.id.page_container, fragment)
                transaction.commit()

                MainActivity.pages[tabID]!!.logotipe!!.visibility = View.GONE
                MainActivity.pages[tabID]!!.back!!.visibility = View.VISIBLE
                MainActivity.myBackstack = 3 // "Создание"
            }
            if (tabID == MainActivity.TAG_TABS[0])
                toRecordCreate.text = "Добавить заметку"
            if (tabID == MainActivity.TAG_TABS[1])
                toRecordCreate.text = "Довавить запись"
        }

        return view
    }

    private fun readFromDB (tabID: Int) {
        val db = dbHelper!!.writableDatabase

        val tableName: String
        if (tabID == MainActivity.TAG_TABS[0])
            tableName = "notes"
        else
            tableName = "cases"

        val c = db.query(tableName, null, null, null, null, null, null)

        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            val idColIndex = c.getColumnIndex("id")
            val nameColIndex = c.getColumnIndex("title")

            if (tabID == MainActivity.TAG_TABS[0]) {
                do {
                    arrayList.add(0, c.getString(nameColIndex))
                    idRecords.add(0, c.getInt(idColIndex))
                } while (c.moveToNext())
            } else {
                val timeColIndex = c.getColumnIndex("time")
                var times = ArrayList<Long>()
                var calendar = Calendar.getInstance()
                times.add(0.toLong())
                var time: Long
                do {
                    time = c.getLong(timeColIndex)

                    if (tabID == MainActivity.TAG_TABS[1] && Calendar.getInstance().timeInMillis < time
                            || tabID == MainActivity.TAG_TABS[2] && Calendar.getInstance().timeInMillis > time) {
                        for (i in times.indices) {
                            if (time < times[i] || times[i] == 0.toLong()) {
                                calendar.timeInMillis = time

                                arrayCases.add(i, CaseItem(c.getString(nameColIndex),
                                        checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "."
                                                + checkValue(calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR),
                                        checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE))))
                                idRecords.add(i, c.getInt(idColIndex))
                                times.add(i, time)
                                break
                            }
                        }
                    }
                } while (c.moveToNext())
            }
        }

        c.close()
        dbHelper!!.close()
    }

    private fun deleteOldRecords() {
        dbHelper = DBHelper(context!!)

        val db = dbHelper!!.writableDatabase

        for (i in idRecords.indices)
            db.delete("cases", "id = ?", arrayOf(Integer.toString(idRecords[i])))

        db.close()
        dbHelper!!.close()
    }

    private fun checkValue(value: Int): String {
        return if (value < 10)
            "0$value"
        else
            value.toString()
    }
}