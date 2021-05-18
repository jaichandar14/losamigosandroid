package com.bpmlinks.vbank.ui.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.databinding.LayoutServiceTypeBinding
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.model.MasterServiceTypeDtosItem

class MasterServiceAdapter: RecyclerView.Adapter<MasterServiceAdapter.MasterServiceViewHolder>() {

    inner class MasterServiceViewHolder(val view: LayoutServiceTypeBinding): RecyclerView.ViewHolder(view.root){
        fun onBind(service: MasterServiceTypeDtosItem, listener: OnServiceClickListener?){
            view.executePendingBindings()
            view.tvService.text = service.masterServiceTypeName
            if(service.masterServiceTypeName == AppConstants.BUSINESS_BANKING){
                view.imgServices.setImageResource(R.drawable.business_banking)
            }else if(service.masterServiceTypeName == AppConstants.PERSONAL_BANKING){
                view.imgServices.setImageResource(R.drawable.personal_banking)
            }else if(service.masterServiceTypeName == AppConstants.RETAIL_BANKING){
                view.imgServices.setImageResource(R.drawable.retail_banking)
            }
            view.layoutService.setOnClickListener{
                listener?.onServiceClicked(service)
            }
        }
    }

    public val serviceList = ArrayList<MasterServiceTypeDtosItem>()
    private var onClickListener: OnServiceClickListener ?= null

    fun refreshItems(services: List<MasterServiceTypeDtosItem>){
        serviceList.clear()
        serviceList.addAll(services)
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnServiceClickListener){
        onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasterServiceViewHolder {
        return MasterServiceViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_service_type, parent, false))
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onBindViewHolder(holder: MasterServiceViewHolder, position: Int) {
        holder.onBind(serviceList[position], onClickListener)
    }

    interface OnServiceClickListener{
        fun onServiceClicked(service: MasterServiceTypeDtosItem)
    }
}