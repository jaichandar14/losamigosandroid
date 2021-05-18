
package com.bpmlinks.vbank.locationRecivier
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.bpmlinks.vbank.twilio.VideoActivity
import com.bpmlinks.vbank.ui.thankyou.ThankYouActivity
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.model.LocationLatLan
import com.vbank.vidyovideoview.webservices.ApiCall
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class GeoLocationReceiver(): BroadcastReceiver() {
    var isGpsEnabled: Boolean = true
    lateinit var dialogBuilder :AlertDialog.Builder

    lateinit var alert:AlertDialog
    var videoActivity:VideoActivity= VideoActivity()
    var gpsOn: Boolean? = true
    var locationStatus = LocationLatLan()
    var meetingParams: MeetingParams? = MeetingParams()
    var TAG = "GeoLocationReceiver"


    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "onReceive:received")
        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {

            var locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (!isGpsEnabled) {
                Log.d("TAG", "onReceive: if block ")
                if (!this::dialogBuilder.isInitialized) {
                    this.gpsOn= false


                    var result =VideoActivity.customerkeynb
                                             //  var result: String = intent.getStringExtra("keyvalue")
                    locationStatus.customerKeyNb = result
                    locationStatus.duringVideo = meetingParams?.duringVideo
                    locationStatus.longitude = meetingParams?.longitude
                    locationStatus.latitude = meetingParams?.latitude
                    locationStatus.gpsOn = meetingParams?.gpsOn


                    meetingParams?.gpsOn = gpsOn
                    meetingParams?.longitude = "0.0"
                    meetingParams?.latitude= "0.0"

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
                                    "http location longitude:${meetingParams?.longitude} " + "latitude:${meetingParams?.latitude}geo location receiver Success method"+"customrKeyNB: ${result} gpsOn:${meetingParams?.gpsOn}"
                            )
                            if (response.isSuccessful) {
                                Log.d(TAG, "http location Geolocation Receiver success")
                            }
                        }

                    })


                    dialogBuilder = AlertDialog.Builder(context)
                    dialogBuilder.setMessage( Html.fromHtml("Allow\"LocationAccess\"to access your location "+"                         " +
                            "while you are using the app?" +"<br />"+
                            "<small>"+"&nbsp;&nbsp;&nbsp;&nbsp This app needs access to your location!"+"</small>"))
                            .setCancelable(false)

                            .setPositiveButton("Allow", DialogInterface.OnClickListener { dialog, id
                                ->
                                Log.d("gps yes", "gps is checkGPSEnable ")
                                context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                            })
                            .setNegativeButton("Don't Allow",DialogInterface.OnClickListener{dialog,id
                        ->

                        dialogBuilder
                                //.setTitle("" )
                                .setMessage(Html.fromHtml("<font color=#000000>In order to proceed,This app requires access to your location. click</font>\n" +
                                        "    <b>OK</b>\n" +
                                        "    <font color=#000000>to exit or click</font>\n" +
                                        "     <b>CANCEL</b>\n" +
                                        "     <font color=#000000>to continue and access to your location</font>"))
                                // .setView(myview)
                                .setCancelable(false)
                                .setPositiveButton("ok", DialogInterface.OnClickListener { dialog, id
                                    ->
                                 context.startActivity(Intent(context,ThankYouActivity::class.java))
                                    apiGeoLocation()
                                })
                                .setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog,id
                                       ->     dialogBuilder.setMessage( Html.fromHtml("Allow\"LocationAccess\"to access your location "+"                         " +
                                        "while you are using the app?" +"<br />"+
                                        "<small>"+"&nbsp;&nbsp;&nbsp;&nbsp This app needs access to your location!"+"</small>"))
                                        .setCancelable(false)

                                        .setPositiveButton("Allow", DialogInterface.OnClickListener { dialog, id
                                            ->
                                            Log.d("gps yes", "gps is checkGPSEnable ")
                                            context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                                        })
                                        .setNegativeButton("Don't Allow",DialogInterface.OnClickListener{dialog,id
                                            -> dialogBuilder
                                                //.setTitle("" )
                                                .setMessage(Html.fromHtml("<font color=#000000>In order to proceed,This app requires access to your location. click</font>\n" +
                                                        "    <b>OK</b>\n" +
                                                        "    <font color=#000000>to exit or click</font>\n" +
                                                        "     <b>CANCEL</b>\n" +
                                                        "     <font color=#000000>to continue and access to your location</font>"))
                                                // .setView(myview)
                                                .setCancelable(false)
                                                .setPositiveButton("ok", DialogInterface.OnClickListener { dialog, id
                                                    ->
                                                    context.startActivity(Intent(context,ThankYouActivity::class.java))
                                                    apiGeoLocation()
                                                })
                                                .setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog,id
                                                    ->
                                                    videoActivity.dismiss()

                                                })

                                            var alert= dialogBuilder.create()

                                            // alert.setMessage("This app needs access to your location!")
                                            alert.show()
                                                })

                                    var alert= dialogBuilder.create()

                                    // alert.setMessage("This app needs access to your location!")
                                    alert.show()
                                })


                        var alert= dialogBuilder.create()

                       // alert.setMessage("This app needs access to your location!")
                        alert.show()
                    })
                    alert = dialogBuilder.create()
                }
                if( ::alert.isInitialized&&!alert.isShowing) {
                    alert.show()
                }


            }else{
                if(::alert.isInitialized&& alert.isShowing) {
                    alert.dismiss()
                    meetingParams?.gpsOn = true
                    var videoActivity:VideoActivity= VideoActivity()
                    videoActivity.requestLocationUpdates()
                }
                Log.d("TAG", "onReceive: else block ")
            }
        }
    }

    fun apiGeoLocation(){



        var locationStatus = LocationLatLan()

        locationStatus.customerKeyNb = meetingParams?.customerKeyNb
        locationStatus.duringVideo = meetingParams?.duringVideo
        locationStatus.longitude ="0.0"
        locationStatus.latitude ="0.0"
        locationStatus.gpsOn = meetingParams?.gpsOn
        locationStatus.customerInCall =false


        ApiCall.retrofitClient.geoLocation(locationStatus).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {


            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                Log.d(
                    TAG,
                    " dismiss onresponse ${meetingParams?.customerKeyNb }"+" ${locationStatus.customerInCall}}"
                )
                if (response.isSuccessful) {
                    Log.d(TAG, " dismiss http location success")
                    Log.d("customerincall dismiss","${locationStatus.customerInCall}")
                }
            }

        })
    }

}



