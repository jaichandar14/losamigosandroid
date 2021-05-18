package com.bpmlinks.vbank.ui.choosebanker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.model.BankerDtosItem


class ChooseBankerAdapter(private var listener : View.OnClickListener): RecyclerView.Adapter<ChooseBankerViewHolder>(){

    private var bankers: List<BankerDtosItem>?= null

    fun setBankerList(bankers: List<BankerDtosItem>?){
        this.bankers = bankers
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseBankerViewHolder {
        return ChooseBankerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_choose_banker, parent, false))
    }

    override fun getItemCount(): Int =  bankers?.size ?: 0

    override fun onBindViewHolder(holder: ChooseBankerViewHolder, position: Int) {
        holder.onBind(position,bankers?.get(position),listener)
    }
}