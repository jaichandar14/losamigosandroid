package com.bpmlinks.vbank.ui.faq.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.databinding.AdapterFaqBinding
import com.bpmlinks.vbank.model.FaqListItem

class FAQViewHolder(private val adapterFaqBinding: AdapterFaqBinding) :
    RecyclerView.ViewHolder(adapterFaqBinding.root) {
    fun onBind(faqList: FaqListItem?, listener: View.OnClickListener, position: Int) {
        adapterFaqBinding.faqList = faqList
        adapterFaqBinding.imgExpand.setOnClickListener(listener)
        adapterFaqBinding.imgExpand.setTag(R.id.imgExpand,position)
    }
}