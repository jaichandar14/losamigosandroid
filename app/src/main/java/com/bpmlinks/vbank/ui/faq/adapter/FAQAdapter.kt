package com.bpmlinks.vbank.ui.faq.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.model.FaqListItem

class FAQAdapter(private val faqList: ArrayList<FaqListItem>?,
                 private val listener : View.OnClickListener) : RecyclerView.Adapter<FAQViewHolder>(),
    Filterable {

    var mArrayList: ArrayList<FaqListItem>? = null
    var mFilteredList: ArrayList<FaqListItem>? = null

    private var mfilter: NewFilter

    override fun getFilter(): Filter {
        return mfilter
    }

    init {
        mfilter = NewFilter(this@FAQAdapter)
        this.mArrayList = faqList
        this.mFilteredList = faqList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
       return FAQViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_faq, parent, false))
    }

    override fun getItemCount(): Int = mFilteredList?.size ?: 0

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.onBind(mFilteredList?.get(position),listener,position)
    }

    inner class NewFilter(var mAdapter: FAQAdapter) : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val charString = charSequence.toString()

            if (charString.isEmpty()) {
                mFilteredList = mArrayList
            } else {
                val filteredList: ArrayList<FaqListItem> = ArrayList()
                mArrayList?.let {faqList ->
                    for (faqItem in faqList) {
                        if (faqItem.question.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(faqItem)
                        }
                    }
                }

                mFilteredList = filteredList
            }

            val filterResults = FilterResults()
            filterResults.values = mFilteredList
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            mFilteredList = filterResults.values as ArrayList<FaqListItem>
            this.mAdapter.notifyDataSetChanged()
        }
    }
}