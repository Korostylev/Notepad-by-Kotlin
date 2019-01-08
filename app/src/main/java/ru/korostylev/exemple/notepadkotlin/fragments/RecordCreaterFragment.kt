package ru.korostylev.exemple.notepadkotlin.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import ru.korostylev.exemple.notepadkotlin.DBHelper
import ru.korostylev.exemple.notepadkotlin.MainActivity
import ru.korostylev.exemple.notepadkotlin.R
import ru.korostylev.exemple.notepadkotlin.receivers.NotificationReceiver
import java.util.*


class RecordCreaterFragment: Fragment() {

    private var dbHelper: DBHelper? = null
    private var calendar = Calendar.getInstance()
    private var editTitle: EditText? = null
    private var editContent: EditText? = null
    private var createRecord: Button? = null
    private var editDate: Button? = null
    private var editTime: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_record_creater, container, false)

        dbHelper = DBHelper(context!!)

        val tabID = arguments!!.getInt("tabID", MainActivity.numPage)
        val recordID = arguments!!.getInt("recordID", 0)

        val editTitle: EditText = view.findViewById(R.id.edit_title)
        val editContent: EditText = view.findViewById(R.id.edit_content)

        val editDate: Button = view.findViewById(R.id.button_edit_date)
        val editTime: Button = view.findViewById(R.id.button_edit_time)
        val createRecord: Button = view.findViewById(R.id.button_create_record)

        val d: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { datePicker, i, i1, i2 ->
            calendar.set(Calendar.YEAR, i)
            calendar.set(Calendar.MONTH, i1)
            calendar.set(Calendar.DAY_OF_MONTH, i2)

            editDate.text = checkValue(i2) + "." + checkValue(i1 + 1) + "." + i
        }

        val t: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, i, i1 ->
            calendar.set(Calendar.HOUR_OF_DAY, i)
            calendar.set(Calendar.MINUTE, i1)

            editTime.text = checkValue(i) + ":" + checkValue(i1)
        }

        if (tabID == MainActivity.TAG_TABS[0]) {
            editDate.visibility = View.GONE
            editTime.visibility = View.GONE
            createRecord.text = "Довавить заметку"
        } else {
            editDate.text = "Выбрать день"
            editDate.setOnClickListener {
                DatePickerDialog(context, d, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
            editTime.text = "Выбрать время"
            editTime.setOnClickListener {
                TimePickerDialog(context, t, calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), true).show()
            }
            createRecord.text = "Довавить запись"
        }

        if (recordID != 0) {
            createRecord.text = "Сохранить изменения"
            // ------------------

            val db = dbHelper!!.writableDatabase

            val tableName: String
            if (tabID == MainActivity.TAG_TABS[0])
                tableName = "notes"
            else
                tableName = "cases"

            val c = db.query(tableName, null,
                    "id = ?", arrayOf(Integer.toString(recordID)), null, null, null)

            if (c.moveToFirst()) {
                val nameColIndex = c.getColumnIndex("title")
                val contentColIndex = c.getColumnIndex("content")

                editTitle.setText(c.getString(nameColIndex))
                editContent.setText(c.getString(contentColIndex))

                if (tabID != MainActivity.TAG_TABS[0]) {
                    val timeColIndex = c.getColumnIndex("time")
                    calendar.timeInMillis = c.getLong(timeColIndex)

                    editDate.text = (checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "."
                            + checkValue(calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR))
                    editTime.text = checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE))
                }
            }

            c.close()
            dbHelper!!.close()

//            readRecordByDB(tabID, recordID)
        }

        createRecord.setOnClickListener {
            createRecordDB(tabID, recordID, editTitle.text.toString(), editContent.text.toString())

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

//    private var d: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { datePicker, i, i1, i2 ->
//        calendar.set(Calendar.YEAR, i)
//        calendar.set(Calendar.MONTH, i1)
//        calendar.set(Calendar.DAY_OF_MONTH, i2)
//
//        editDate.text = checkValue(i2) + "." + checkValue(i1 + 1) + "." + i
//    }
//
//    private var t: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, i, i1 ->
//        calendar.set(Calendar.HOUR_OF_DAY, i)
//        calendar.set(Calendar.MINUTE, i1)
//
//        editTime!!.text = checkValue(i) + ":" + checkValue(i1)
//    }

    private fun createRecordDB(tabID: Int, recordID: Int, title: String, content: String) {
        val cv = ContentValues()
        val db = dbHelper!!.writableDatabase

        cv.put("title", title)
        cv.put("content", content)

        val tableName: String
        if (tabID == MainActivity.TAG_TABS[0]) {
            tableName = "notes"
        } else {
            cv.put("time", calendar.timeInMillis)
            tableName = "cases"
        }

        var notifID = 0

        if (recordID == 0)
            notifID = db.insert(tableName, null, cv).toInt()
        else {
            db.update(tableName, cv, "id = ?", arrayOf(recordID.toString()))
            notifID = recordID
        }

        if (tabID == MainActivity.TAG_TABS[1])
            createNotification(notifID, title, content, calendar.timeInMillis)

        db.close()
        dbHelper!!.close()
    }

    private fun readRecordByDB(tabID: Int, id: Int) {
        val db = dbHelper!!.writableDatabase

        val tableName: String
        if (tabID == MainActivity.TAG_TABS[0])
            tableName = "notes"
        else
            tableName = "cases"

        val c = db.query(tableName, null,
                "id = ?", arrayOf(Integer.toString(id)), null, null, null)

        if (c.moveToFirst()) {
            val nameColIndex = c.getColumnIndex("title")
            val contentColIndex = c.getColumnIndex("content")

            editTitle!!.setText(c.getString(nameColIndex))
            editContent!!.setText(c.getString(contentColIndex))

            if (tabID != MainActivity.TAG_TABS[0]) {
                val timeColIndex = c.getColumnIndex("time")
                calendar.timeInMillis = c.getLong(timeColIndex)

                editDate!!.text = (checkValue(calendar.get(Calendar.DAY_OF_MONTH)) + "."
                        + checkValue(calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR))
                editTime!!.text = checkValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkValue(calendar.get(Calendar.MINUTE))
            }
        } else {
            editTitle!!.setText("Отсутствует")
            editContent!!.setText("Отсутствует")
        }

        c.close()
        dbHelper!!.close()
    }

    private fun createNotification(id: Int, title: String, text: String, time: Long) {
        val intent = Intent(activity, NotificationReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("id", id)
        val pending = PendingIntent.getBroadcast(activity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val am: AlarmManager = activity!!.getSystemService(FragmentActivity.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, time, 0, pending)
    }

    private fun checkValue(value: Int): String {
        return if (value < 10)
            "0$value"
        else
            value.toString()
    }
}