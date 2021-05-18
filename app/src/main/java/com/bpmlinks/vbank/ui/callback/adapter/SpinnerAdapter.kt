package com.bpmlinks.vbank.ui.callback.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatTextView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.model.AvailableTimingMasterDtosItem

class SpinnerAdapter(
    val context: Context,
    private var listItemsTxt: List<AvailableTimingMasterDtosItem>?
) : BaseAdapter() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.spinner_timeslot, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        vh.label.text = listItemsTxt?.get(position)?.timeSlot
        view.setPadding(0, view.paddingTop,0, view.paddingBottom)
        return view
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return listItemsTxt?.size ?: 0
    }

    private class ItemRowHolder(row: View?) {
        val label: AppCompatTextView = row?.findViewById(R.id.txt_time_slot) as AppCompatTextView
    }
}
