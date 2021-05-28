package com.bpmlinks.vbank.ui.vehicleinspection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.VehicleInspectionFragmentBinding
import com.bpmlinks.vbank.fcm.FirebaseNotification
import com.bpmlinks.vbank.fcm.receiver.NotificationReceiver
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.helper.viewmodel.LocalStorage
import com.bpmlinks.vbank.locationRecivier.GeoLocationReceiver
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.ServiceType
import com.bpmlinks.vbank.twilio.CallActivity
import com.bpmlinks.vbank.twilio.LocationViewModel
import com.bpmlinks.vbank.twilio.VideoActivity
import com.bpmlinks.vbank.ui.HomeActivity
import com.bpmlinks.vbank.ui.thankyou.ThankYouActivity
import com.bpmlinks.vbank.ui.videorecordpermission.VdRecordPermissionActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.helper.AppConstant
import com.vbank.vidyovideoview.helper.BundleKeys
import com.vbank.vidyovideoview.model.*
import com.vbank.vidyovideoview.webservices.ApiCall
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.content_video.*
import kotlinx.android.synthetic.main.preference_dialog_number_edittext.*
import kotlinx.android.synthetic.main.vehicle_inspection_fragment.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import javax.inject.Inject

class VehicleInspectionFragment : BaseFragment<VehicleInspectionFragmentBinding,VehicleInspectionViewModel>() {
    var TAG = VehicleInspectionFragment::class.java.name
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val navArgs by navArgs<VehicleInspectionFragmentArgs>()
    private var meetingParams : MeetingParams? = MeetingParams()
    lateinit var recever :BroadcastReceiver
    var notificationid=0
    var isGpsEnabled = false
    var checkGpsEnabled = false
    lateinit var locationManager:LocationManager
    lateinit var dialogBuilder : AlertDialog.Builder
    lateinit var alert: AlertDialog
    var LOCATION_PERMISSION_REQUEST_CODE = 1
    private var webView: WebView? = null

    private lateinit var locationViewModel: LocationViewModel

    override fun getViewModel()= ViewModelProvider(this,factory).get(VehicleInspectionViewModel::class.java)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
//        getViewModel()?.scheduledTime.value = navArgs.sheduledTime
      //  dateTimeApiCall()
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationPermission()
        init()
Log.d(TAG,"enter the call")
        recever  = object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG,"enter the call 1")
                if (intent != null) {
                    meetingParams = if (intent.hasExtra(BundleKeys.MeetingParams)) {
                        intent.getParcelableExtra(BundleKeys.MeetingParams) as MeetingParams
                    }else{
                        MeetingParams()
                    }

                    if(!meetingParams?.token.isNullOrEmpty())
                    {
                        Log.d("onresume","call in trigger")
                        iv_join_disable.visibility=View.GONE
                        btn_join_disable.visibility=View.GONE

                        iv_join.visibility=View.VISIBLE
                        btn_join.visibility=View.VISIBLE
                    }
                    else
                    {
                        iv_join_disable.visibility=View.VISIBLE
                        btn_join_disable.visibility=View.VISIBLE

                        iv_join.visibility=View.GONE
                        btn_join.visibility=View.GONE
                    }

                    if (intent.hasExtra(BundleKeys.callDecline))
                    {
                        var callend= intent.getStringExtra(BundleKeys.callDecline)
                        if(callend.equals("yes",true))
                        {
                            iv_join_disable.visibility=View.GONE
                            btn_join_disable.visibility=View.GONE
                            iv_join.visibility=View.GONE
                            btn_join.visibility=View.GONE
                            btn_exit.visibility=View.VISIBLE
                        }

                    }

                    if(intent.hasExtra(AppConstants.NOTIFICATION_ID))
                    {
                        notificationid= intent.getIntExtra(AppConstants.NOTIFICATION_ID,0)
                    }


                    if (intent?.action.toString().equals("DOCUSIGN_ACTION")){
                        Log.d(TAG, "webview onReceive: if loop ")
                        var url=   intent?.getStringExtra(BundleKeys.docusignurl)
                        if (!url.isNullOrEmpty()) {
                            webView?.visibility = View.VISIBLE
                            btn_join_disable.visibility = View.GONE
                            iv_join_disable.visibility = View.GONE

                            loadWebview(url)
                        } else {
                            //      thumbnailVideoView.visibility=View.GONE
                            webView?.visibility = View.GONE
                        }

                    }
                }

            }

        }


    }




    override fun onResume() {
        super.onResume()

        dateTimeApiCall()
           Log.d("onresume","call in resume")

           var  intentfilter =IntentFilter(getString(R.string.brodcost_recever))
           context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(recever,intentfilter) }

           var  callintentfilter =IntentFilter(getString(R.string.callend_brodcost_recever))
           context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(recever,callintentfilter) }

           var intentfilter1 = IntentFilter(getString(R.string.docusign_brodcost_recever))
           context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(recever, intentfilter1) }

           if (checkGpsEnabled){
               isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

           }
           if (isGpsEnabled) {
               if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                           == PackageManager.PERMISSION_GRANTED)) {

//                requestLocationUpdates1()
                   Log.d("msg", "onresume if loop")
               }

           }else{

               if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                           == PackageManager.PERMISSION_GRANTED)) {

                   if (alertMessageLocation()) {
                       Log.d(TAG, "http location inside alart block requestLocationUpdates one ")
                       requestLocationUpdates()
                   }
                   Log.d("msg", "onresume if loop")
               }



           }




    }

    override fun onPause() {
        super.onPause()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(recever) }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun init()
    {
        Log.d("onCreate","calll in create call")
        webView = view?.findViewById(R.id.webView_inspection)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.loadWithOverviewMode = true
        webView?.settings?.useWideViewPort = true
        webView?.settings?.domStorageEnabled = true


        btn_join.setOnClickListener {

            var intent=Intent(context, VdRecordPermissionActivity::class.java)
            intent.putExtra(BundleKeys.MeetingParams,meetingParams)
            intent.putExtra(AppConstants.NOTIFICATION_ID, notificationid)
            startActivity(intent)
            activity?.finish()

            iv_join_disable.visibility=View.GONE
            btn_join_disable.visibility=View.GONE
            iv_join.visibility=View.GONE
            btn_join.visibility=View.GONE
            btn_exit.visibility=View.VISIBLE



        }

        btn_exit.setOnClickListener {
            val intent = Intent(context, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(com.bpmlinks.vbank.helper.BundleKeys.MOVE_TO_USER_INPUT_SCREEN,true)
            startActivity(intent)
            activity?.finish()
        }

        logout.setOnClickListener{
            startActivity(Intent(context,HomeActivity::class.java))
            val sharedPreferences = activity?.getSharedPreferences("MyUser", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.clear()
            editor?.apply()
            activity?.finish()

        }
        lougout_icon.setOnClickListener{
            startActivity(Intent(context,HomeActivity::class.java))
            val sharedPreferences = activity?.getSharedPreferences("MyUser", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.clear()
            editor?.apply()
            activity?.finish()

        }
        notification_btn.setOnClickListener {
            notification()
        }



    }


    override fun getBindingVariable()=BR.vehicleInspectionVM

    override fun getContentView()=R.layout.vehicle_inspection_fragment

    override fun internetConnected() {

    }

    fun notification()
    {
        val notificationId = 1

        var   intent= Intent(context, VideoActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
        val meetingParams = MeetingParams()
        meetingParams.roomName="tesss"
        meetingParams.token="ascdbsbcdvbsndbvnsdvnb"
        meetingParams.docusignurl=""

        intent.putExtra(BundleKeys.MeetingParams, meetingParams)
        val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val fullScreenIntent = Intent(context, CallActivity::class.java)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        fullScreenIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
        fullScreenIntent.putExtra(BundleKeys.MeetingParams, meetingParams)
        val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val buttonIntent = Intent(context, NotificationReceiver::class.java)
        buttonIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
        buttonIntent.putExtra(AppConstants.NOTIFICATION_CUSTOMER_KEY, meetingParams.customerKeyNb)
        buttonIntent.putExtra(AppConstants.NOTIFICATION_CALL_KEY, meetingParams.callKeyNb)



        val dismissIntent =
                PendingIntent.getBroadcast(context, 0, buttonIntent, 0)

        val channelId = getString(R.string.app_name)



        val builder: NotificationCompat.Builder? =
                context?.let { NotificationCompat.Builder(it, channelId) }

        val notificationLayout = RemoteViews(context?.packageName, R.layout.custome_notification)

        notificationLayout.setOnClickPendingIntent(R.id.action_btn,pendingIntent)
        notificationLayout.setTextViewText(R.id.custome_notification_title,"title")

        if(!meetingParams.docusignurl.isNullOrEmpty()) {


            builder ?.setSmallIcon(R.mipmap.ic_launcher)
                    ?.setContentTitle("title")
                    ?.setContentText("bankerName")
                    ?.setAutoCancel(false)
                    ?.setOngoing(true)
                    ?.addAction(
                            0,
                            getString(R.string.btn_accept_action),
                            pendingIntent
                    )


        }
        else {
            builder?.setSmallIcon(R.mipmap.ic_launcher)
                    ?.setContentTitle("title")
                    ?.setContentText("bankerName")
                    ?.setFullScreenIntent(fullScreenPendingIntent, true)
                    ?.setCategory(Notification.CATEGORY_CALL)
                    ?.setAutoCancel(false)
                    ?.setOngoing(true)
                    ?.addAction(
                            R.drawable.ic_call_end_24dp,
                            getString(R.string.btn_reject_incoming_call),
                            dismissIntent
                    )
                    ?.addAction(
                            R.drawable.ic_call_24dp,
                            getString(R.string.btn_accept_incoming_call),
                            pendingIntent
                    )
        }


        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            )


            channel.lightColor = Color.GREEN
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder?.build()
        notification?.flags = Notification.FLAG_INSISTENT

        notificationManager.notify(notificationId, notification)

        if (meetingParams.docusignurl.isNullOrEmpty())
        {
            var brodcostintent=Intent(getString(R.string.brodcost_recever))
            brodcostintent.putExtra(BundleKeys.MeetingParams,meetingParams)
            brodcostintent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
            activity?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(brodcostintent) }
        }
        else
        {
            var brodcostintent=Intent(getString(R.string.docusign_brodcost_recever))
            brodcostintent.putExtra(BundleKeys.docusignurl,meetingParams.docusignurl)
            brodcostintent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
            activity?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(brodcostintent) }
        }


    }

    private fun showProgress() {
        progress_bar1.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar1.visibility = View.GONE
    }


    fun locationPermission(){
        if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED )) {

            Log.d(TAG, "http location granted block requestLocationUpdates ")
            requestLocationUpdates()
            Log.d("msg","if loop")
        }else{

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                denymessage()
                Log.d("msg","if rationale loop")
            }else{
                val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
                Log.d("msg","else black")
            }

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            var locationPermissionGranted = true

            for (grantResult in grantResults) {
                locationPermissionGranted = locationPermissionGranted and
                        (grantResult == PackageManager.PERMISSION_GRANTED)
            }

            if (locationPermissionGranted) {
                Log.d("msg","if black")
                //alertMessageLocation()

            } else {
                Log.d("msg","else black.............................")


                denymessage()

            }
        }
    }

    fun alertMessageLocation():Boolean{
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setMessage( Html.fromHtml("Allow\"LocationAccess\"to access your location "+"                         " +
                    "while you are using the app?" +"<br />"+
                    "<small>"+"&nbsp;&nbsp;&nbsp;&nbsp This app needs access to your location!"+"</small>"))
                    .setCancelable(false)
                    .setPositiveButton("Allow", DialogInterface.OnClickListener { dialog, id ->
                        alert.dismiss()
                        Log.d("gps yes", "gps is checkGPSEnable ")
                        checkGpsEnabled = true
                        requireActivity().startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                    })
                    .setNegativeButton("Don't Allow",DialogInterface.OnClickListener{dialog,id ->
                        alert.dismiss()
                        gpsPermisson()
                    })

            alert= dialogBuilder.create()
            alert.show()
        }
        return true
    }

    fun gpsPermisson(){
        val builder = android.app.AlertDialog.Builder(requireActivity())
        val alertLayout = layoutInflater.inflate(R.layout.reject_dialog_layout, null)
        val btnYes = alertLayout.findViewById<AppCompatButton>(R.id.btnYes)
        val btnNo = alertLayout.findViewById<AppCompatButton>(R.id.btnNo)
        val title = alertLayout.findViewById<AppCompatTextView>(R.id.reject_title)
        title.setText(Html.fromHtml("In order to proceed,This app requires access to your Location. click\n" +
                "    <b>OK</b>\n" +
                "    to exit or click\n" +
                "    <b>CANCEL</b>\n" +
                "     to continue"))
        builder.setView(alertLayout)
        val alertDialog: android.app.AlertDialog? = builder.create()
        alertDialog?.setCancelable(false)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        alertDialog?.show()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog?.window?.setLayout((width * 0.75).toInt(), FrameLayout.LayoutParams.WRAP_CONTENT)
        btnYes.setOnClickListener {
            alertDialog?.dismiss()
            startActivity(Intent(requireActivity(), ThankYouActivity::class.java))
            requireActivity().finish()
        }
        btnNo.setOnClickListener {
            alertDialog?.dismiss()
            alertMessageLocation()
        }
    }


    fun requestLocationUpdates() {

        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        Log.d(TAG, "http location requestLocationUpdates ")
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        Log.d(TAG, "http location requestLocationUpdates one ")
        locationViewModel.getLocationLiveDate().observe(
                requireActivity(),
                androidx.lifecycle.Observer {
                    it.latitude
                    it.longitutde
                    Log.d(TAG, "http location longitude:${it.longitutde} " + "latitude:${it.latitude}")

                    var mLongitude = it.longitutde
                    var mLatitude = it.latitude

                    var locationStatus = LocationLatLan()
                    meetingParams?.longitude = it.longitutde
                    meetingParams?.latitude = it.latitude

                    var getSharedPreferences = requireActivity().applicationContext.getSharedPreferences("MyUser",Context.MODE_PRIVATE)
                    var mailId = getSharedPreferences?.getString("MailId","").toString()
                    LocalStorage.email =mailId
                    locationStatus.gpsOn = true
                    locationStatus.customerInCall = false
                    locationStatus.emailId = LocalStorage.email
                    locationStatus.longitude = meetingParams?.longitude
                    locationStatus.latitude = meetingParams?.latitude
                    Log.d(TAG, "http location requestLocationUpdates: ${locationStatus.emailId}....${locationStatus.longitude}..${locationStatus.latitude}")


                    ApiCall.retrofitClient.geoLocation(locationStatus).enqueue(object :
                            retrofit2.Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(requireActivity(), t.localizedMessage, Toast.LENGTH_SHORT)
                                    .show()
                            Log.d(
                                    TAG,
                                    "http location longitude:${it.longitutde} " + "latitude:${it.latitude} Failure method"
                            )
                        }

                        override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                        ) {

                            Log.d(
                                    TAG,
                                    "http location longitude:${locationStatus.longitude} " + "latitude:${locationStatus.latitude}Success method" + " customerker:${meetingParams?.customerKeyNb}"
                            )
                            if (response.isSuccessful) {
                                Log.d(TAG, "http location success ${locationStatus.customerInCall}")
                            }
                        }

                    })

                })
        Log.d(TAG, "location requestLocationUpdates end ")
    }

    fun denymessage(){
        dialogBuilder = AlertDialog.Builder(requireActivity())
        dialogBuilder
                //.setTitle("" )
                .setMessage(Html.fromHtml("Its look like you have turned off the permissions required for this feature.It can be enable under Phone Settings>>Apps>>Los AmigosS>>Permission."))
                // .setView(myview)
                .setCancelable(false)
                .setPositiveButton("Go to \n Settings", DialogInterface.OnClickListener { dialog, id
                    ->
                    var intent =Intent()
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    var uri=Uri.fromParts("package",activity?.packageName,null)
                    intent.setData(uri)
                    context?.startActivity(intent)


                })

        alert= dialogBuilder.create()

        if( ::alert.isInitialized&&!alert.isShowing) {
            alert.show()
        }

    }

    private fun loadWebview(url: String) {
        var url1=""
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d("URL", url)

                if ((url.toString().contains(AppConstant.DOCUSIGN_BASE_URL ))||url.toString().contains(AppConstant.DOCUSIGN_BASE_URL1)) {

                    updateDocumentSignStatus()

                    webView?.visibility=View.GONE
                    btn_join_disable.visibility = View.VISIBLE
                    iv_join_disable.visibility = View.VISIBLE

                    if(url.toString().contains("ttl_expired"))
                    {


                        url1 = generateDocusignUrl().toString()
                        Log.d(TAG,"generate if  cal  URl ${url1}")
                        webView?.loadUrl(url1)

                    }

                } else {

                    view?.loadUrl(url)
                }

                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                removeNotification()
            }
        }

        webView?.loadUrl(url)

        Log.d("URL", url)
    }

    private fun updateDocumentSignStatus() {
        val docuSignStatusRequest = DocuSignStatusRequest()
        docuSignStatusRequest.callKeyNb = FirebaseNotification.callKeyNbForDocOffline
        Log.d(TAG, "offline updateDocumentSignStatus: ${docuSignStatusRequest.callKeyNb}")
        docuSignStatusRequest.action="COMPLETED"
        ApiCall.retrofitClient.updateDocuSignStatus(docuSignStatusRequest).enqueue(object :
                retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
            ) {
            }
        })
    }

    private fun generateDocusignUrl():String?{
        var url1=""
        var callkeyNB=meetingParams?.callKeyNb
        ApiCall.retrofitClient.getDocuSignUrl(callkeyNB).enqueue(object :
                retrofit2.Callback<Output> {
            override fun onResponse(call: Call<Output>, response: Response<Output>) {
                Log.d(TAG,"generate on response cal  URl  ")
                if (response.isSuccessful){
                    var status: Output? = response.body()
                    url1= status?.data?.statusMsg.toString()
                    Log.d("TAG", "expired url success method called ${response.code()}​​${url1}​​​​​​​​ ")
                }

            }
            override fun onFailure(call: Call<Output>, t: Throwable) {

            }


        })
        return url1
    }

    private fun removeNotification() {
        if (requireActivity().intent.hasExtra(AppConstant.NOTIFICATION_ID)) {
            val notificationId = requireActivity().intent.getIntExtra(AppConstant.NOTIFICATION_ID, 0)
            val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }

    fun dateTimeApiCall(){

        var getSharedPreferencesOne = requireActivity().applicationContext.getSharedPreferences("MyUser",Context.MODE_PRIVATE)
        var mailIdOne = getSharedPreferencesOne?.getString("MailId","")
        ApiCall.retrofitClient.geoDateTime(mailIdOne).enqueue(object :retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.isSuccessful) {
                    var status: ResponseBody? = response.body()
                     val adapter = Gson().getAdapter(Output::class.java)
                    val successResponse = adapter.fromJson(status?.string())
                    var time = successResponse.data.schdeuleTime
                    var dateunix = successResponse.data.scheduleDate
                    var success=successResponse.message

                    var unixSeconds = dateunix?.toLong()
                            ?.div(1000)
                    var convertDate = unixSeconds?.times(1000L)?.let { Date(it) }
                    var dateFormat = SimpleDateFormat("dd-MMM-yyyy")
                    dateFormat.timeZone = TimeZone.getDefault()
                    var dateFinal = dateFormat.format(convertDate)
//Local Date
                    var sdf = SimpleDateFormat("dd-MMM-yyyy")
                    var localDate = sdf.format(Date())

                    if (dateFinal == localDate || dateFinal.isNullOrEmpty()) {
                        getViewModel()?.scheduleDate.value = "Today"
                        if (!time.isNullOrEmpty()) {
                            edit_time.visibility = View.VISIBLE
                        } else {
                            edit_time.visibility = View.GONE
                        }
                    } else {
                        getViewModel()?.scheduleDate.value = dateFinal
                        edit_time.visibility = View.VISIBLE
                    }
                    getViewModel().scheduledTime.value = time
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }


        })
    }

}
