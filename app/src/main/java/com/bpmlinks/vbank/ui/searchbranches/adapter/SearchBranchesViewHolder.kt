package com.bpmlinks.vbank.ui.searchbranches.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.databinding.AdapterSearchBranchesBinding
import com.bpmlinks.vbank.model.BranchDtosItem

class SearchBranchesViewHolder(private var adapterSearchBranchesBinding: AdapterSearchBranchesBinding) :
    RecyclerView.ViewHolder(adapterSearchBranchesBinding.root) {

    fun onBind(position : Int, branchesItem : BranchDtosItem?,listener : View.OnClickListener){
        adapterSearchBranchesBinding.branchesItem = branchesItem
        adapterSearchBranchesBinding.btnCallBack.setOnClickListener(listener)
        adapterSearchBranchesBinding.btnConnectingNow.setOnClickListener(listener)
        adapterSearchBranchesBinding.btnCallBack.setTag(R.id.btn_call_back,branchesItem)
        adapterSearchBranchesBinding.btnConnectingNow.setTag(R.id.btn_connecting_now,branchesItem)
    }
}