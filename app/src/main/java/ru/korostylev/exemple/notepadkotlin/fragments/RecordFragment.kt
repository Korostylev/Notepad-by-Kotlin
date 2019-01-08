package ru.korostylev.exemple.notepadkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ru.korostylev.exemple.notepadkotlin.DBHelper
import ru.korostylev.exemple.notepadkotlin.MainActivity
import ru.korostylev.exemple.notepadkotlin.NotificationHelper
import ru.korostylev.exemple.notepadkotlin.R
import java.util.*

class RecordFragment: Fragment() {

    private var dbHelper: DBHelper? = null
    private var textTitle: TextView? = null
    private var textContent:TextView? = null
    private var textDateTime:TextView? = null
    private val calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.fragment_record, container, false)

        dbHelper = DBHelper(context!!)

        val tabID = arguments!!.getInt("tabID", MainActivity.numPage)
        val recordID = arguments!!.getInt("recordID", 0)

        val textTitle: TextView = view.findViewById(R.id.textView_title)
        val textContent: TextView = view.findViewById(R.id.textView_content)
        val textDateTime: TextView = view.findViewById(R.id.textView_dateTime)


        // -------------------
        val db = dbHelper!!.writableDatabase

        var tableName: String = ""
        if (tabID == MainActivity.TAG_TABS[0]) {
            tableName = "notes"
            textDateTime.visibility = View.GONE
        } else
            tableName = "cases"

        val c = db.query(tableName, null, "id = ?", arrayOf(Integer.toString(recordID)), null, null, null)

        if (c.moveToFirst()) {
            val nameColIndex = c.getColumnIndex("title")
            val contentColIndex = c.getColumnIndex("content")

            textTitle.text = c.getString(nameColIndex)
            textContent.text = c.getString(contentColIndex)

            if (tabID != MainActivity.TAG_TABS[0]) {
                val timeColIndex = c.getColumnIndex("time")
                calendar.timeInMillis = c.getLong(timeColIndex)

                val outDateTime = (checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "." + checkValue(calendar.get(Calendar.MONTH) + 1) + "."
                        + calendar.get(Calendar.YEAR) + " в "
                        + checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE)))
                textDateTime.text = outDateTime
            }
        }

        c.close()
        dbHelper!!.close()

//        readRecordByDB(tabID, recordID)

        val editBtn: Button = view.findViewById(R.id.button_edit_record)
        editBtn.text = "Изменить"
        editBtn.setOnClickListener {
            val fragment = RecordCreaterFragment()
            val args = Bundle()
            args.putInt("tabID", tabID)
            args.putInt("recordID", recordID)
            fragment.arguments = args

            val transaction = parentFragment!!.childFragmentManager.beginTransaction()
            transaction.replace(R.id.page_container, fragment)
            transaction.commit()

            MainActivity.myBackstack = 4 // "Редактирование"
            MainActivity.numRec = recordID
        }

        val deleteBtn: Button = view.findViewById(R.id.button_delete_record)
        deleteBtn.text = "Удалить"
        deleteBtn.setOnClickListener {
            deleteRecordByID(tabID, recordID)

            val records = RecordsFragment()
            val args = Bundle()
            args.putInt("numPage", tabID)
            records.arguments = args
            val transaction = parentFragment!!.childFragmentManager.beginTransaction()
            transaction.replace(R.id.page_container, records)
            transaction.commit()

            MainActivity.pages[tabID]!!.logotipe!!.visibility = View.VISIBLE
            MainActivity.pages[tabID]!!.back!!.visibility = View.GONE
            MainActivity.myBackstack = 1 // MAIN
        }

        return view
    }

    private fun readRecordByDB(tabID: Int, id: Int) {
        val db = dbHelper!!.writableDatabase

        val tableName: String
        if (tabID == MainActivity.TAG_TABS[0]) {
            tableName = "notes"
            textDateTime!!.visibility = View.GONE
        } else
            tableName = "cases"

        val c = db.query(tableName, null,
                "id = ?", arrayOf(Integer.toString(id)), null, null, null)

        if (c.moveToFirst()) {
            val nameColIndex = c.getColumnIndex("title")
            val contentColIndex = c.getColumnIndex("content")

            textTitle!!.text = c.getString(nameColIndex)
            textContent!!.text = c.getString(contentColIndex)

            if (tabID != MainActivity.TAG_TABS[0]) {
                val timeColIndex = c.getColumnIndex("time")
                calendar.timeInMillis = c.getLong(timeColIndex)

                val outDateTime = (checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "." + checkValue(calendar.get(Calendar.MONTH) + 1) + "."
                        + calendar.get(Calendar.YEAR) + " в "
                        + checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE)))
                textDateTime!!.text = outDateTime
            }
        }

        c.close()
        dbHelper!!.close()
    }

    private fun deleteRecordByID(tabID: Int, id: Int) {
        val db = dbHelper!!.writableDatabase

        val tableName: String
        if (tabID == MainActivity.TAG_TABS[0])
            tableName = "notes"
        else {
            tableName = "cases"
            val notification = NotificationHelper()
            notification.cancelNotification(context!!, id)
        }

        db.delete(tableName, "id = ?", arrayOf(Integer.toString(id)))

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