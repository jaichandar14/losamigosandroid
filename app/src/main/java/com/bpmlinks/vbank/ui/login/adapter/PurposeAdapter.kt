package com.bpmlinks.vbank.ui.login.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.model.MasterServiceTypeDtosItem

class PurposeAdapter(
    private val serviceTypeList: List<MasterServiceTypeDtosItem>?,
    private val listener: View.OnClickListener
) : RecyclerView.Adapter<PurposeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurposeViewHolder {
        val view = PurposeViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_purpose, parent, false))
        val layoutParams: ViewGroup.LayoutParams = view.itemView.layoutParams
        layoutParams.width = parent.measuredWidth / 2
        view.itemView.layoutParams = layoutParams
        return view
    }

    override fun getItemCount(): Int = serviceTypeList?.size ?: 0

    override fun onBindViewHolder(holder: PurposeViewHolder, position: Int) {
        holder.onBind(serviceTypeList?.get(position), position, listener)
    }

}