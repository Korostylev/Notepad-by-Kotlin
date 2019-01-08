package ru.korostylev.exemple.notepadkotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ru.korostylev.exemple.notepadkotlin.R
import ru.korostylev.exemple.notepadkotlin.items.CaseItem

class CasesAdapter(val context: Context,val data: ArrayList<CaseItem>): BaseAdapter() {

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        //Получение объекта inflater из контекста
        val inflater = LayoutInflater.from(context)

        var view: View
        //Если someView (View из ListView) вдруг оказался равен
        //null тогда мы загружаем его с помошью inflater
        if (p1 == null) {
            view = inflater.inflate(R.layout.item_case, p2, false)
        } else {
            view = p1
        }
        //Обявляем наши текствьюшки и связываем их с разметкой
        val title: TextView = view.findViewById(R.id.item_case_title)
        val time: TextView = view.findViewById(R.id.item_case_time)
        val date: TextView = view.findViewById(R.id.item_case_date)

        //Устанавливаем в каждую текствьюшку соответствующий текст
        title.text = data[p0].title
        time.text = data[p0].time
        date.text = data[p0].date

        return view
    }
}