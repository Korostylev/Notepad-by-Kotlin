package ru.korostylev.exemple.notepadkotlin.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.korostylev.exemple.notepadkotlin.NotificationHelper

class AlarmBootReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val settingsNotif = NotificationHelper()
        settingsNotif.createEverydayNotification(p0!!, 8, 0)
    }
}