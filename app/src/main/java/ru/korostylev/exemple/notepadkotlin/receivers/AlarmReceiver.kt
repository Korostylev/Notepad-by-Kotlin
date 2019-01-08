package ru.korostylev.exemple.notepadkotlin.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v4.app.NotificationCompat
import ru.korostylev.exemple.notepadkotlin.DBHelper
import ru.korostylev.exemple.notepadkotlin.MainActivity
import ru.korostylev.exemple.notepadkotlin.NotificationHelper
import ru.korostylev.exemple.notepadkotlin.R
import java.util.*

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        var check: Boolean = false

        val dbHelper = DBHelper(p0!!)
        val db: SQLiteDatabase = dbHelper.writableDatabase
        var c: Cursor = db.query("cases", null, null, null, null, null, null)

        val calendar = Calendar.getInstance()
        val min: Long = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        calendar.set(Calendar.HOUR_OF_DAY, p1!!.getIntExtra("hour", 6))
        calendar.set(Calendar.MINUTE, p1.getIntExtra("min", 0))
        calendar.set(Calendar.SECOND, 0)
        val max: Long = calendar.timeInMillis

        if (c.moveToFirst()) {
            val idColIndex = c.getColumnIndex("id")
            val nameColIndex = c.getColumnIndex("title")
            val timeColIndex = c.getColumnIndex("time")
            val contentColIndex = c.getColumnIndex("content")

            do {
                if (c.getLong(timeColIndex) in min..max) {
                    val newNotif = NotificationHelper()
                    newNotif.createEasyNotification(p0, c.getInt(idColIndex),
                            c.getString(nameColIndex), c.getString(contentColIndex), c.getLong(timeColIndex))

                    check = true
                }
            } while (c.moveToNext())
        }
        c.close()
        dbHelper.close()

        if (check) {
            val notificationIntent = Intent(p0, MainActivity::class.java)
            val contentIntent = PendingIntent.getActivity(p0,0,
                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val builder = NotificationCompat.Builder(p0)
            builder.setContentIntent(contentIntent)
                    // обязательные настройки
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(p0.getString(R.string.app_name))
                    .setContentText("Сегодня есть дела")// Текст уведомления
                    .setAutoCancel(true) // автоматически закрыть уведомление после нажатия

            val notificationManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(0, builder.build())
        }
    }
}