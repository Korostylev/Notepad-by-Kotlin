package ru.korostylev.exemple.notepadkotlin

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "myDB", null, 1) {
    override fun onCreate(p0: SQLiteDatabase?) {
        // создаем таблицу с полями
        p0!!.execSQL("create table notes ("
                + "id integer primary key autoincrement,"
                + "title text,"
                + "content text" + ");")
        // создаем таблицу с полями
        p0!!.execSQL("create table cases ("
                + "id integer primary key autoincrement,"
                + "title text,"
                + "time text,"
                + "content text" + ");")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }
}