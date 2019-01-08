package ru.korostylev.exemple.notepadkotlin.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import ru.korostylev.exemple.notepadkotlin.MainActivity
import ru.korostylev.exemple.notepadkotlin.R

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val notificationIntent = Intent(p0!!, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(p0,0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val builder = NotificationCompat.Builder(p0)
        builder.setContentIntent(contentIntent)
                // обязательные настройки
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(p1!!.getStringExtra("title"))
                .setContentText(p1.getStringExtra("text"))// Текст уведомления
                .setAutoCancel(true) // автоматически закрыть уведомление после нажатия

        val notificationManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(p1.getIntExtra("id", 0), builder.build())
    }
}