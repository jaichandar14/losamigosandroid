package com.bpmlinks.vbank.twilio

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import com.bpmlinks.vbank.dto.LocationDetails
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class LocationLiveData(context: Context): LiveData<LocationDetails>() {
private  var client= LocationServices.getFusedLocationProviderClient(context)

    //

    override fun onInactive() {
        super.onInactive()
        client.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()



                client.lastLocation.addOnSuccessListener {
            location: Location? ->  location.also {
                setLocationData(it)
        }
        }
        startLocationUpdates()
        Log.d("msg","startLocatonupdates 0")

    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        Log.d("msg", "startLocatonupdates")

        client.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }


    private var locationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            p0?:return
            Log.d("msg", "1")
            for(location in p0.locations)
            {
                Log.d("msg", "2")
                setLocationData(location)
                Log.d("msg", "3")
            }
        }
    }

    private fun setLocationData(location: Location?) {
        value= LocationDetails(location?.longitude.toString(),location?.latitude.toString())
        Log.d("msg","startLocatonupdates ${value}")

    }

    companion object{
    var ONE_MINUTE:Long=30000
    var locationRequest: LocationRequest =LocationRequest.create().apply {
        interval= ONE_MINUTE
        fastestInterval= (ONE_MINUTE/4)
        numUpdates=100
        priority=LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}
}

