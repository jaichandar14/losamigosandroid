package com.bpmlinks.vbank.ui.choosebanker.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.databinding.AdapterChooseBankerBinding
import com.bpmlinks.vbank.model.BankerDtosItem

class ChooseBankerViewHolder(private val adapterChooseBankerBinding: AdapterChooseBankerBinding) 
    : RecyclerView.ViewHolder(adapterChooseBankerBinding.root) {
    fun onBind(
        position: Int,
        bankerInfoDtosItem: BankerDtosItem?,
        listener: View.OnClickListener
    ) {
        adapterChooseBankerBinding.bankerInfo = bankerInfoDtosItem
        adapterChooseBankerBinding.btnCallBack.setOnClickListener(listener)
        adapterChooseBankerBinding.btnConnectingNow.setOnClickListener(listener)
        adapterChooseBankerBinding.btnCallBack.setTag(R.id.btn_call_back,bankerInfoDtosItem)
        adapterChooseBankerBinding.btnConnectingNow.setTag(R.id.btn_connecting_now,bankerInfoDtosItem)
    }
}