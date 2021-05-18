package com.bpmlinks.vbank.ui.purpose.adapter

import com.bpmlinks.vbank.databinding.LayoutChooseTypeBinding
import com.bpmlinks.vbank.model.SubServiceTypeDtosItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R

class SubServiceTypeAdapter: RecyclerView.Adapter<SubServiceTypeAdapter.SubServiceViewHolder>() {

    inner class SubServiceViewHolder(val view: LayoutChooseTypeBinding): RecyclerView.ViewHolder(view.root){
        fun onBind(service: SubServiceTypeDtosItem, listener: OnSubServiceClickListener?){
            view.executePendingBindings()
            view.tvSubType.text = service.subServiceTypeName
            view.layoutSubType.setOnClickListener{
                service.selected = true
                listener?.onSubServiceClicked(service)
                notifyDataSetChanged()
            }
            if (service.selected) {
                view.tvSubType.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.selection_tickmark,0,0,0)
            } else {
                view.tvSubType.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.un_select,0,0,0)
            }
        }
    }

    private val serviceList = ArrayList<SubServiceTypeDtosItem>()
    private var onClickListener: OnSubServiceClickListener ?= null

    fun refreshItems(services: List<SubServiceTypeDtosItem>){
        serviceList.clear()
        serviceList.addAll(services)
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnSubServiceClickListener){
        onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubServiceViewHolder {
        return SubServiceViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_choose_type, parent, false))
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onBindViewHolder(holder: SubServiceViewHolder, position: Int) {
        holder.onBind(serviceList[position], onClickListener)
    }

    interface OnSubServiceClickListener{
        fun onSubServiceClicked(service: SubServiceTypeDtosItem)
    }
}