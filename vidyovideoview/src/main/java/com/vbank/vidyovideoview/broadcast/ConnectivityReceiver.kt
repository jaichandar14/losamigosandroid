package com.vbank.vidyovideoview.broadcast

import CheckInternetConnection
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ConnectivityReceiver: BroadcastReceiver() {
    companion object {
        var connectivityReceiverListener : ConnectivityReceiverListener? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
           if (CheckInternetConnection.isInternetAvailable(context)){
               connectivityReceiverListener?.onNetworkConnectionChanged(true)
           }else{
               connectivityReceiverListener?.onNetworkConnectionChanged(false)
           }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }
}