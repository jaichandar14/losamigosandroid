package com.vbank.vidyovideoview.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vbank.vidyovideoview.R
import com.vbank.vidyovideoview.model.DataItem

class UserActionAdapter(private var dataItem : List<DataItem>?,private var listener : View.OnClickListener)
    : RecyclerView.Adapter<UserActionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserActionViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_actions, parent, false)
        return UserActionViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserActionViewHolder, position: Int) {
        holder.onBind(dataItem?.get(position),listener,position)
    }

    override fun getItemCount(): Int {
        return dataItem?.size ?: 0
    }


}