package com.bpmlinks.vbank.helper

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.twilio.LocationViewModel
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.model.LocationLatLan
import com.vbank.vidyovideoview.webservices.ApiCall
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class GeoLocationApi :AppCompatActivity() {
    var locationStatus = LocationLatLan()
    var meetingParams:MeetingParams= MeetingParams()
    var TAG="GeoLocationApi"
   lateinit var locationViewModel: LocationViewModel
    var gpsOn :Boolean = false
fun geoApi() {
    Log.d(TAG, "http location requestLocationUpdates ")
    locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
    Log.d(TAG, "http location requestLocationUpdates one ")
    locationViewModel.getLocationLiveDate().observe(
            this,
            androidx.lifecycle.Observer {
                it.latitude
                it.longitutde
                Log.d(TAG, "http location longitude:${it.longitutde} " + "latitude:${it.latitude}")
                var mLongitude = it.longitutde
                var mLatitude = it.latitude

                if (it.longitutde != null) {
                    Log.d(TAG, "location requestLocationUpdates: true")
                    gpsOn = true
                }

                meetingParams?.longitude=mLongitude
                meetingParams?.latitude=mLatitude
    locationStatus.emailId=meetingParams?.emailId
    locationStatus.customerKeyNb = meetingParams?.customerKeyNb
    locationStatus.duringVideo = meetingParams?.duringVideo
    locationStatus.longitude = meetingParams?.longitude
    locationStatus.latitude = meetingParams?.latitude
    locationStatus.gpsOn = true
    locationStatus.customerInCall =meetingParams.customerInCall



    ApiCall.retrofitClient.geoLocation(locationStatus).enqueue(object :
            retrofit2.Callback<ResponseBody> {
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d(
                    TAG,
                    "http location longitude:${meetingParams?.longitude} " + "latitude:${meetingParams?.latitude} Failure method"
            )
        }

        override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
        ) {

            Log.d(
                    TAG,
                    "http location longitude:${meetingParams?.longitude} " +
                            "latitude:${meetingParams?.latitude}geo location receiver Success method" + "customrKeyNB:  gpsOn:${meetingParams?.gpsOn}"
            )
            if (response.isSuccessful) {
                Log.d(TAG, "http location Geolocation Receiver success")
            }
        }

    })   })
}


}