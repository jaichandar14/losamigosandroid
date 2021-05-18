package com.bpmlinks.vbank.ui.searchbranches.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.model.BranchDtosItem

class SearchBranchesAdapter(private val listener : View.OnClickListener) : RecyclerView.Adapter<SearchBranchesViewHolder>(){

    private var branches: List<BranchDtosItem>?= null

    fun setBranchesList(branches: List<BranchDtosItem>?){
        this.branches = branches
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBranchesViewHolder {
        return SearchBranchesViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_search_branches, parent, false))
    }

    override fun getItemCount(): Int =  branches?.size ?: 0

    override fun onBindViewHolder(holder: SearchBranchesViewHolder, position: Int) {
        holder.onBind(position,branches?.get(position),listener)
    }
}