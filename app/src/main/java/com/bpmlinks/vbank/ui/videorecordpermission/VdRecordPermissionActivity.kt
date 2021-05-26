package com.bpmlinks.vbank.ui.videorecordpermission

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bpmlinks.vbank.R
import com.vbank.vidyovideoview.model.CallRecordAPI
import com.bpmlinks.vbank.twilio.VideoActivity
import com.bpmlinks.vbank.ui.thankyou.ThankYouActivity
import com.google.android.gms.location.*
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.fullscreenintent.workmanager.DeclineCallWorker
import com.vbank.vidyovideoview.helper.AppConstant
import com.vbank.vidyovideoview.helper.BundleKeys
import com.vbank.vidyovideoview.webservices.ApiCall
import kotlinx.android.synthetic.main.activty_vd_record_permission.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
class VdRecordPermissionActivity : AppCompatActivity() {
    private var meetingParams : MeetingParams? = null
    private var userDataParams: UserDataParams? = null
    var isGpsEnabled=true
    var cameraAndMicPermissionGranted=true
    lateinit var locationManager:LocationManager
    var PERMISSION_ID = 44
    var  LOCATION_PERMISSION_REQUEST_CODE=1
    lateinit var alert:AlertDialog
    lateinit var dialogBuilder :AlertDialog.Builder
    private val CAMERA_MIC_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_vd_record_permission)
        removeNotification()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            userDataParams = if (intent.hasExtra(BundleKeys.UserDataParams)) {
                intent.getParcelableExtra(BundleKeys.UserDataParams) as UserDataParams
            } else {
                UserDataParams()
            }
            meetingParams = if (intent.hasExtra(BundleKeys.MeetingParams)) {
                intent.getParcelableExtra(BundleKeys.MeetingParams) as MeetingParams
            } else {
                MeetingParams()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        accept_btn.setOnClickListener {

//            if(isGpsEnabled) {
//
//                apicall(true)
//                openVideoPage()
//            }
//            else
//            {
//                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED)) {

                apicall(true)
                openVideoPage()

                Log.d("msg","1")
            }else{
                val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
                Log.d("msg","1")
            }

            //}
        }
        regect_btn.setOnClickListener {
            showRejcetDilog()
        }
    }
    private fun showRejcetDilog() {
        val builder = android.app.AlertDialog.Builder(this)
        val alertLayout = layoutInflater.inflate(R.layout.reject_dialog_layout, null)
        val btnYes = alertLayout.findViewById<AppCompatButton>(R.id.btnYes)
        val btnNo = alertLayout.findViewById<AppCompatButton>(R.id.btnNo)
        val title = alertLayout.findViewById<AppCompatTextView>(R.id.reject_title)
        title.setText(Html.fromHtml("<font color=#000000>you will not be able to join the inspection call by clicking reject. click</font>\n" +
                "    <font color=#81D226>OK</font>\n" +
                "    <font color=#000000>to confirm or click</font>\n" +
                "     <font color=#FE5151>CANCEL</font>\n" +
                "     <font color=#000000>to continue</font>"))
        builder.setView(alertLayout)
        val alertDialog: android.app.AlertDialog? = builder.create()
        alertDialog?.setCancelable(true)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        alertDialog?.show()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog?.window?.setLayout((width * 0.75).toInt(), FrameLayout.LayoutParams.WRAP_CONTENT)
        btnYes.setOnClickListener {
            alertDialog?.dismiss()
            apicall(false)
            declineCall()
            startActivity(Intent(this, ThankYouActivity::class.java))
            finish()
            var brodcostintent=Intent(getString(R.string.callend_brodcost_recever))
            brodcostintent.putExtra(BundleKeys.callDecline,"yes")
            LocalBroadcastManager.getInstance(this).sendBroadcast(brodcostintent)
        }
        btnNo.setOnClickListener {
            alertDialog?.dismiss()
        }
    }
    private fun openVideoPage() {
        var intent=Intent(this,VideoActivity::class.java)
        Log.d("msg","openvideopage")
        intent.putExtra(BundleKeys.MeetingParams,meetingParams)
        startActivity(intent)
        finish()
    }
    private fun removeNotification() {
        if (intent.hasExtra(AppConstant.NOTIFICATION_ID)) {
            val notificationId = intent.getIntExtra(AppConstant.NOTIFICATION_ID, 0)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }
    private fun apicall(reson:Boolean)
    {
        val caRecord = CallRecordAPI(
                meetingParams?.callKeyNb,
                meetingParams?.customerKeyNb,
                0,
                reson
        )
        ApiCall.retrofitClient.callRecordApi(caRecord).enqueue(object :
                retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
            override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                    }
                }
            }
        })
    }

    private fun declineCall() {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<DeclineCallWorker>()
        val data = Data.Builder()
        data.putInt(BundleKeys.CallKeyNb, meetingParams?.callKeyNb ?: 0)
        data.putInt(BundleKeys.CustomerKeyNb,meetingParams?.customerKeyNb ?: 0)
        data.putString(BundleKeys.CallEndReason, BundleKeys.callDecline)
        uploadWorkRequest.setInputData(data.build())
        WorkManager
                .getInstance(this@VdRecordPermissionActivity)
                .enqueue(uploadWorkRequest.build())
    }

    override fun onResume() {
        super.onResume()
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {


        when(requestCode){

            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (requestCode ==  LOCATION_PERMISSION_REQUEST_CODE && grantResults.size > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                    cameraAndMicPermissionGranted = true
                    isGpsEnabled=true

                    Log.d("msg","2")
                    if (cameraAndMicPermissionGranted ) {
                        if (isGpsEnabled){
                            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED &&ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                                            == PackageManager.PERMISSION_GRANTED)){
                                apicall(true)
                                openVideoPage()
                            }else{


                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED ){
                                    var locationMessage="Location"

                                    denyAlertMessage(locationMessage)
                                }
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED ){
                                    var mCameraMessage="Camera"
                                    denyAlertMessage(mCameraMessage)
                                }
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                                        != PackageManager.PERMISSION_GRANTED ){
                                    var mRecordAudioMessage="Audio"
                                    denyAlertMessage(mRecordAudioMessage)
                                }
                            }

                        }
                        else {


//                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                                != PackageManager.PERMISSION_GRANTED ){
//                                    var locationMessage="Location"
//
//                            denyAlertMessage(locationMessage)
//                        }
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED ){
                                var mCameraMessage="Camera"
                                denyAlertMessage(mCameraMessage)
                            }
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                                    != PackageManager.PERMISSION_GRANTED ){
                                var mRecordAudioMessage="Audio"
                                denyAlertMessage(mRecordAudioMessage)
                            }

                            Toast.makeText(
                                    this,
                                    R.string.permissions_needed,
                                    Toast.LENGTH_LONG
                            ).show()

                        }


                    } }
            }
        }}

    fun denyAlertMessage(mMessage:String){
        val builder = android.app.AlertDialog.Builder(this)
        val alertLayout = layoutInflater.inflate(R.layout.reject_dialog_layout, null)
        val btnYes = alertLayout.findViewById<AppCompatButton>(R.id.btnYes)
        val btnNo = alertLayout.findViewById<AppCompatButton>(R.id.btnNo)
        val title = alertLayout.findViewById<AppCompatTextView>(R.id.reject_title)
        title.setText(Html.fromHtml("In order to proceed,This app requires access to your ${mMessage}. click\n" +
                "    <b>OK</b>\n" +
                "    to exit or click\n" +
                "    <b>CANCEL</b>\n" +
                "     to continue"))
        builder.setView(alertLayout)
        val alertDialog: android.app.AlertDialog? = builder.create()
        alertDialog?.setCancelable(true)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        alertDialog?.show()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog?.window?.setLayout((width * 0.75).toInt(), FrameLayout.LayoutParams.WRAP_CONTENT)
        btnYes.setOnClickListener {
            alertDialog?.dismiss()
            apicall(false)
            declineCall()
            startActivity(Intent(this, ThankYouActivity::class.java))
            finish()
            var brodcostintent=Intent(getString(R.string.callend_brodcost_recever))
            brodcostintent.putExtra(BundleKeys.callDecline,"yes")
            LocalBroadcastManager.getInstance(this).sendBroadcast(brodcostintent)
        }
        btnNo.setOnClickListener {
            alertDialog?.dismiss()
        }



    }
}
