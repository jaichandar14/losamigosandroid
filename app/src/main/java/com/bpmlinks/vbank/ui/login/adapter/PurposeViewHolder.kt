package com.bpmlinks.vbank.ui.login.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.databinding.AdapterPurposeBinding
import com.bpmlinks.vbank.model.MasterServiceTypeDtosItem

class PurposeViewHolder(private val adapterPurposeBinding: AdapterPurposeBinding) :
    RecyclerView.ViewHolder(adapterPurposeBinding.root) {

    fun onBind(serviceTypeList: MasterServiceTypeDtosItem?, position: Int, listener: View.OnClickListener) {
       adapterPurposeBinding.serviceTypeList = serviceTypeList
    }
}