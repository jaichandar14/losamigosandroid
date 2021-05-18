package com.vbank.vidyovideoview.business

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vbank.vidyovideoview.R
import com.vbank.vidyovideoview.model.DataItem
import kotlinx.android.synthetic.main.adapter_user_actions.view.*

class UserActionViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

     fun onBind(
         dataItem: DataItem?,
         listener: View.OnClickListener,
         position: Int
     ) {
        view.txtUserActions.text = dataItem?.title

        if(dataItem?.isSelected == true){
            view.imgSelected.setImageResource(R.drawable.selection_tickmark)
        }else{
            view.imgSelected.setImageResource(R.drawable.un_select)
        }

        view.lnrDataItem.setOnClickListener(listener)
        view.lnrDataItem.setTag(R.id.lnrDataItem,position)

    }
}