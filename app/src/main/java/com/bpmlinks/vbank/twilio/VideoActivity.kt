package com.bpmlinks.vbank.twilio

//import com.vbank.vidyovideoview.model.CallRecordAPI

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.media.AudioManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bpmlinks.vbank.BuildConfig
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.locationRecivier.GeoLocationReceiver
import com.bpmlinks.vbank.model.ServiceType
import com.bpmlinks.vbank.twilio.screencapture.ScreenCapturerManager
import com.bpmlinks.vbank.ui.thankyou.ThankYouActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioDevice.*
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.*
import com.twilio.video.ktx.Video.connect
import com.twilio.video.ktx.createLocalAudioTrack
import com.twilio.video.ktx.createLocalVideoTrack
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.fullscreenintent.turnScreenOnAndKeyguardOff
import com.vbank.vidyovideoview.fullscreenintent.workmanager.DeclineCallWorker
import com.vbank.vidyovideoview.helper.AppConstant
import com.vbank.vidyovideoview.helper.BundleKeys
import com.vbank.vidyovideoview.model.DocuSignStatusRequest
import com.vbank.vidyovideoview.model.LocationLatLan
import com.vbank.vidyovideoview.model.Output
import com.vbank.vidyovideoview.model.UserActionItems
import com.vbank.vidyovideoview.webservices.ApiCall
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.android.synthetic.main.content_video.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

import tvi.webrtc.VideoSink
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates


public class VideoActivity : AppCompatActivity() {
    private val CAMERA_MIC_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_MEDIA_PROJECTION = 5
    private val TAG = "VideoActivity"
    private lateinit var webView: WebView
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private lateinit var locationViewModel: LocationViewModel
    lateinit var receverLocation: BroadcastReceiver
    lateinit var recever: BroadcastReceiver
    lateinit var layoucontainer: LinearLayout
    var screencaptureStarted = false
    var isGpsEnabled=true
var i=0
    /*
     * You must provide a Twilio Access Token to connect to the Video service
     */

    private val TWILIO_ACCESS_TOKEN = BuildConfig.TWILIO_ACCESS_TOKEN
    private val ACCESS_TOKEN_SERVER = BuildConfig.TWILIO_ACCESS_TOKEN_SERVER

    /*
     * Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */

    private lateinit var accessToken: String

    /*
     * A Room represents communication between a local participant and one or more participants.
     */

    private var room: Room? = null
    private var localParticipant: LocalParticipant? = null

    private var userDataParams: UserDataParams? = null
    private var meetingParams: MeetingParams? = null

    /*
     * AudioCodec and VideoCodec represent the preferred codec for encoding and decoding audio and
     * video.
     */

    private val audioCodec: AudioCodec
        get() {
            val audioCodecName = sharedPreferences.getString(
                    SettingsActivity.PREF_AUDIO_CODEC,
                    SettingsActivity.PREF_AUDIO_CODEC_DEFAULT
            )

            return when (audioCodecName) {
                IsacCodec.NAME -> IsacCodec()
                OpusCodec.NAME -> OpusCodec()
                PcmaCodec.NAME -> PcmaCodec()
                PcmuCodec.NAME -> PcmuCodec()
                G722Codec.NAME -> G722Codec()
                else -> OpusCodec()
            }
        }
    private val videoCodec: VideoCodec
        get() {
            val videoCodecName = sharedPreferences.getString(
                    SettingsActivity.PREF_VIDEO_CODEC,
                    SettingsActivity.PREF_VIDEO_CODEC_DEFAULT
            )

            return when (videoCodecName) {
                Vp8Codec.NAME -> {
                    val simulcast = sharedPreferences.getBoolean(
                            SettingsActivity.PREF_VP8_SIMULCAST,
                            SettingsActivity.PREF_VP8_SIMULCAST_DEFAULT
                    )
                    Vp8Codec(simulcast)
                }
                H264Codec.NAME -> H264Codec()
                Vp9Codec.NAME -> Vp9Codec()
                else -> Vp8Codec()
            }
        }

    private val enableAutomaticSubscription: Boolean
        get() {
            return sharedPreferences.getBoolean(
                    SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                    SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBCRIPTION_DEFAULT
            )
        }

    /*
     * Encoding parameters represent the sender side bandwidth constraints.
     */
    private val encodingParameters: EncodingParameters
        get() {
            val defaultMaxAudioBitrate = SettingsActivity.PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT
            val defaultMaxVideoBitrate = SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT
            val maxAudioBitrate = Integer.parseInt(
                    sharedPreferences.getString(
                            SettingsActivity.PREF_SENDER_MAX_AUDIO_BITRATE,
                            defaultMaxAudioBitrate
                    ) ?: defaultMaxAudioBitrate
            )
            val maxVideoBitrate = Integer.parseInt(
                    sharedPreferences.getString(
                            SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE,
                            defaultMaxVideoBitrate
                    ) ?: defaultMaxVideoBitrate
            )

            return EncodingParameters(maxAudioBitrate, maxVideoBitrate)
        }

    /*
     * Room events listener
     */
    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            localParticipant = room.localParticipant
            videoStatusTextView.text = "Connected to ${room.name}"
            title = room.name

            // Only one participant is supported
            room.remoteParticipants?.firstOrNull()?.let { addRemoteParticipant(it) }
        }

        override fun onReconnected(room: Room) {
            videoStatusTextView.text = "Connected to ${room.name}"
            reconnectingProgressBar.visibility = View.GONE;
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            videoStatusTextView.text = "Reconnecting to ${room.name}"
            reconnectingProgressBar.visibility = View.VISIBLE;
        }

        override fun onConnectFailure(room: Room, e: TwilioException) {
            videoStatusTextView.text = "Failed to connect"
            audioSwitch.deactivate()
            initializeUI()
        }

        override fun onDisconnected(room: Room, e: TwilioException?) {
            localParticipant = null
            videoStatusTextView.text = "Disconnected from ${room.name}"
            reconnectingProgressBar.visibility = View.GONE;
            this@VideoActivity.room = null
            // Only reinitialize the UI if disconnect was not called from onDestroy()
            if (!disconnectedFromOnDestroy) {
                audioSwitch.deactivate()
                initializeUI()
                moveLocalVideoToPrimaryView()
            }
        }

        override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
            addRemoteParticipant(participant)
        }

        override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
            removeRemoteParticipant(participant)
        }

        override fun onRecordingStarted(room: Room) {
            /*
             * Indicates when media shared to a Room is being recorded. Note that
             * recording is only available in our Group Rooms developer preview.
             */
            Log.d(TAG, "onRecordingStarted")
        }

        override fun onRecordingStopped(room: Room) {
            /*
             * Indicates when media shared to a Room is no longer being recorded. Note that
             * recording is only available in our Group Rooms developer preview.
             */
            Log.d(TAG, "onRecordingStopped")
        }
    }

    /*
     * RemoteParticipant events listener
     */
    private val participantListener = object : RemoteParticipant.Listener {
        override fun onAudioTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            Log.i(
                    TAG, "onAudioTrackPublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                    "enabled=${remoteAudioTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteAudioTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteAudioTrackPublication.trackName}]"
            )
            videoStatusTextView.text = "onAudioTrackAdded"
        }

        override fun onAudioTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            Log.i(
                    TAG, "onAudioTrackUnpublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                    "enabled=${remoteAudioTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteAudioTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteAudioTrackPublication.trackName}]"
            )
            videoStatusTextView.text = "onAudioTrackRemoved"
        }

        override fun onDataTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
        ) {
            Log.i(
                    TAG, "onDataTrackPublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                    "enabled=${remoteDataTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteDataTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteDataTrackPublication.trackName}]"
            )
            videoStatusTextView.text = "onDataTrackPublished"
        }

        override fun onDataTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
        ) {
            Log.i(
                    TAG, "onDataTrackUnpublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                    "enabled=${remoteDataTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteDataTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteDataTrackPublication.trackName}]"
            )
            videoStatusTextView.text = "onDataTrackUnpublished"
        }

        override fun onVideoTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            Log.i(
                    TAG, "onVideoTrackPublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteVideoTrackPublication: sid=${remoteVideoTrackPublication.trackSid}, " +
                    "enabled=${remoteVideoTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteVideoTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteVideoTrackPublication.trackName}]"
            )
            videoStatusTextView.text = "onVideoTrackPublished"
        }

        override fun onVideoTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            Log.i(
                    TAG, "onVideoTrackUnpublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteVideoTrackPublication: sid=${remoteVideoTrackPublication.trackSid}, " +
                    "enabled=${remoteVideoTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteVideoTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteVideoTrackPublication.trackName}]"
            )
            videoStatusTextView.text = "onVideoTrackUnpublished"
        }

        override fun onAudioTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
        ) {
            Log.i(
                    TAG, "onAudioTrackSubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrack: enabled=${remoteAudioTrack.isEnabled}, " +
                    "playbackEnabled=${remoteAudioTrack.isPlaybackEnabled}, " +
                    "name=${remoteAudioTrack.name}]"
            )
            videoStatusTextView.text = "onAudioTrackSubscribed"
        }

        override fun onAudioTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
        ) {
            Log.i(
                    TAG, "onAudioTrackUnsubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrack: enabled=${remoteAudioTrack.isEnabled}, " +
                    "playbackEnabled=${remoteAudioTrack.isPlaybackEnabled}, " +
                    "name=${remoteAudioTrack.name}]"
            )
            videoStatusTextView.text = "onAudioTrackUnsubscribed"
        }

        override fun onAudioTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                twilioException: TwilioException
        ) {
            Log.i(
                    TAG, "onAudioTrackSubscriptionFailed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                    "name=${remoteAudioTrackPublication.trackName}]" +
                    "[TwilioException: code=${twilioException.code}, " +
                    "message=${twilioException.message}]"
            )
            videoStatusTextView.text = "onAudioTrackSubscriptionFailed"
        }

        override fun onDataTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
        ) {
            Log.i(
                    TAG, "onDataTrackSubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrack: enabled=${remoteDataTrack.isEnabled}, " +
                    "name=${remoteDataTrack.name}]"
            )
            videoStatusTextView.text = "onDataTrackSubscribed"
        }

        override fun onDataTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
        ) {
            Log.i(
                    TAG, "onDataTrackUnsubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrack: enabled=${remoteDataTrack.isEnabled}, " +
                    "name=${remoteDataTrack.name}]"
            )
            videoStatusTextView.text = "onDataTrackUnsubscribed"
        }

        override fun onDataTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                twilioException: TwilioException
        ) {
            Log.i(
                    TAG, "onDataTrackSubscriptionFailed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                    "name=${remoteDataTrackPublication.trackName}]" +
                    "[TwilioException: code=${twilioException.code}, " +
                    "message=${twilioException.message}]"
            )
            videoStatusTextView.text = "onDataTrackSubscriptionFailed"
        }

        override fun onVideoTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack
        ) {
            Log.i(
                    TAG, "onVideoTrackSubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteVideoTrack: enabled=${remoteVideoTrack.isEnabled}, " +
                    "name=${remoteVideoTrack.name}]"
            )
            videoStatusTextView.text = "onVideoTrackSubscribed"
            addRemoteParticipantVideo(remoteVideoTrack)
        }

        override fun onVideoTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack
        ) {
            Log.i(
                    TAG, "onVideoTrackUnsubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteVideoTrack: enabled=${remoteVideoTrack.isEnabled}, " +
                    "name=${remoteVideoTrack.name}]"
            )
            videoStatusTextView.text = "onVideoTrackUnsubscribed"
            removeParticipantVideo(remoteVideoTrack)
        }

        override fun onVideoTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                twilioException: TwilioException
        ) {
            Log.i(
                    TAG, "onVideoTrackSubscriptionFailed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteVideoTrackPublication: sid=${remoteVideoTrackPublication.trackSid}, " +
                    "name=${remoteVideoTrackPublication.trackName}]" +
                    "[TwilioException: code=${twilioException.code}, " +
                    "message=${twilioException.message}]"
            )
            videoStatusTextView.text = "onVideoTrackSubscriptionFailed"
            Snackbar.make(
                    connectActionFab,
                    "Failed to subscribe to ${remoteParticipant.identity}",
                    Snackbar.LENGTH_LONG
            )
                    .show()
        }

        override fun onAudioTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
        }

        override fun onVideoTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
        }

        override fun onVideoTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
        }

        override fun onAudioTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
        }
    }

    private var localAudioTrack: LocalAudioTrack? = null
    private var localVideoTrack: LocalVideoTrack? = null
    private var alertDialog: AlertDialog? = null
    private val cameraCapturerCompat by lazy {
        CameraCapturerCompat(this, CameraCapturerCompat.Source.FRONT_CAMERA)
    }
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this@VideoActivity)
    }

    /*
     * Audio management
     */
    private val audioSwitch by lazy {
        AudioSwitch(
                applicationContext, preferredDeviceList = listOf(
                BluetoothHeadset::class.java,
                WiredHeadset::class.java, Speakerphone::class.java, Earpiece::class.java
        )
        )
    }
    private var savedVolumeControlStream by Delegates.notNull<Int>()
    private lateinit var audioDeviceMenuItem: MenuItem

    private var participantIdentity: String? = null
    private lateinit var localVideoView: VideoSink
    private var disconnectedFromOnDestroy = false
    private var isSpeakerPhoneEnabled = true

    private lateinit var screenCapturer: ScreenCapturer
    private lateinit var screenCapturerManager: ScreenCapturerManager

    lateinit var locationManager: LocationManager

    lateinit var receverGPS:BroadcastReceiver
    var gpsStatus:Boolean=true
    var gpsOn :Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        turnScreenOnAndKeyguardOff()
        webView = findViewById(R.id.webView)
        layoucontainer = findViewById(R.id.video_container)
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        removeNotification()



        /*
         * Set local video view to primary view
         */
        localVideoView = primaryVideoView

        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager = ScreenCapturerManager(this)
        }

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        savedVolumeControlStream = volumeControlStream
        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        /*
         * Set access token
         */
        setAccessToken()

        /*
         * Request permissions.
         */

      requestPermissionForCameraAndMicrophone()
//        requestPermissionForLocation()
        Log.d(TAG, "location onCreate: called")
       isGPSEnabled()
//        requestPermissionForLocation()
        /*
         * Set the initial state of the UI
         */
        initializeUI()

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


        /* if(meetingParams?.docusignurl?.length!! >0)
          {
              var layoutparams: CoordinatorLayout.LayoutParams =CoordinatorLayout.LayoutParams(300,300)
              layoutparams.gravity=Gravity.BOTTOM or Gravity.END
              layoutparams.bottomMargin=600
              layoutparams.rightMargin=20
              layoucontainer.layoutParams = layoutparams
              webView.visibility = View.VISIBLE
              thumbnailVideoView.visibility=View.GONE
              loadWebview(meetingParams?.docusignurl!!)
          }*/


        speaker.setOnClickListener {
            // Get AudioManager
            val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            //audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
            if (audioManager.isSpeakerphoneOn) {
                speaker.setImageDrawable(getDrawable(R.drawable.loud_speaker_off))
                audioManager.isSpeakerphoneOn = false
            } else {
                speaker.setImageDrawable(getDrawable(R.drawable.loudspeaker))
                audioManager.isSpeakerphoneOn = true
            }
        }


        share_screen_off_iv.setOnClickListener {

            share_screen_off_iv.visibility = View.GONE
            share_screen_iv.visibility = View.VISIBLE
            switchCameraActionFab.visibility = View.VISIBLE
            localVideoActionFab.visibility = View.VISIBLE

            stopScreenCapture()

        }

        share_screen_iv.setOnClickListener {

            share_screen_off_iv.visibility = View.VISIBLE
            share_screen_iv.visibility = View.GONE

            try {

                if (Build.VERSION.SDK_INT >= 29) {
                    screenCapturerManager.startForeground()
                }
                //if (!this::screenCapturer.isInitialized) {
                requestScreenCapturePermission()
                /*} else {
                    startScreenCapture()
                }*/
            } catch (ae: java.lang.Exception) {

            }


            /*try {
                if(screencaptureStarted)
                {
                    share_screen_iv.setImageDrawable(getDrawable(R.drawable.screen_sharing))
                    stopScreenCapture()
                    screencaptureStarted=false
                }
                else {
                    share_screen_iv.setImageDrawable(getDrawable(R.drawable.share_off_drawable))
                    if (Build.VERSION.SDK_INT >= 29) {
                        screenCapturerManager.startForeground()
                    }
                    if (!this::screenCapturer.isInitialized) {
                        requestScreenCapturePermission()
                    } else {
                        startScreenCapture()
                    }
                }
            }catch (ae:Exception)
            {
            }*/



        }
      //  generateDocusignUrl()
    }




    private fun requestPermissionForLocation() {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)) {

                    Log.d("gps on", "gps is onrequestPermissionForLocation ")
            requestLocationUpdates()
        } else {
            Log.d("gps off", "gps is onrequestPermissionForLocation ")
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
        }

    }


    companion object{
                    var customerkeynb:Int=0

}

     fun requestLocationUpdates() {
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
                    var getSharedPreferences = this.applicationContext.getSharedPreferences("MyUser",Context.MODE_PRIVATE)
                    var mailId = getSharedPreferences?.getString("MailId","")

                    var locationStatus = LocationLatLan()
                    locationStatus.emailId=mailId
                    locationStatus.customerKeyNb = meetingParams?.customerKeyNb
                    locationStatus.duringVideo = meetingParams?.duringVideo
                    locationStatus.longitude = meetingParams?.longitude
                    locationStatus.latitude = meetingParams?.latitude
                    locationStatus.gpsOn = meetingParams?.gpsOn
                    locationStatus.customerInCall=true

                      customerkeynb= meetingParams?.customerKeyNb!!
                    meetingParams?.gpsOn = gpsOn
                    meetingParams?.longitude = mLongitude
                    meetingParams?.latitude = mLatitude

                    ApiCall.retrofitClient.geoLocation(locationStatus).enqueue(object :
                            retrofit2.Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(this@VideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
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
                                    "http location longitude:${it.longitutde} " + "latitude:${it.latitude}Success method" +"email:${locationStatus.emailId} "+ " customerker:${meetingParams?.customerKeyNb}"
                            )
                            if (response.isSuccessful) {
                                Log.d(TAG, "http location success")

                                Log.d("customerincall","${locationStatus.customerInCall}")
                            }
                        }

                    })

                })
        Log.d(TAG, "location requestLocationUpdates end ")
    }



    fun isGPSEnabled() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        gpsStatus  =locationManager.isProviderEnabled(GPS_PROVIDER)
        if(gpsStatus) {
            Log.d("gps on", "location gps is isGPSEnabled ")
//            apicall(true)
//            openVideoPage()
//            isGpsOn =true
            requestLocationUpdates()
        }else{
            Log.d("gps off", "location gps is isGPSEnabled ")
//            isGpsOn = false
            //Toast.makeText(this, "Turn on the gps", Toast.LENGTH_SHORT).show()
           // checkGPSEnable()
        }

    }

    @SuppressLint("RestrictedApi")
    private fun checkGPSEnable() {

        var dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setMessage( Html.fromHtml("Allow\"LocationAccess\"to access your location "+"                         " +
                "while you are using the app?" +"<br />"+
                "<small>"+"&nbsp;&nbsp;&nbsp;&nbsp This app needs access to your location!"+"</small>"))
                .setCancelable(false)
                .setPositiveButton("Allow", DialogInterface.OnClickListener { dialog, id
                    ->
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    Log.d("gps yes", "gps is checkGPSEnable ")
                    requestLocationUpdates()


                })
                .setNegativeButton("Don't Allow",DialogInterface.OnClickListener{dialog,id
                        ->
                    dialogBuilder.setMessage(Html.fromHtml("<font color=#000000>In order to proceed,This app requires access to your location. click</font>\n" +
                            "    <b>OK</b>\n" +
                            "    <font color=#000000>to exit or click</font>\n" +
                            "     <b>CANCEL</b>\n" +
                            "     <font color=#000000>to continue and access to your location</font>"))
                            // .setView(myview)
                            .setCancelable(false)
            .setPositiveButton("ok", DialogInterface.OnClickListener { dialog, id
                ->
                startActivity(Intent(this,ThankYouActivity::class.java))


            })
            .setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog,id
                -> dialogBuilder.setMessage( Html.fromHtml("Allow\"LocationAccess\"to access your location "+"                         " +
                    "while you are using the app?" +"<br />"+
                    "<small>"+"&nbsp;&nbsp;&nbsp;&nbsp This app needs access to your location!"+"</small>"))
                    .setCancelable(false)
                    .setPositiveButton("Allow", DialogInterface.OnClickListener { dialog, id
                        ->
                        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        Log.d("gps yes", "gps is checkGPSEnable ")
                        requestLocationUpdates()


                    })
                    .setNegativeButton("Don't Allow",DialogInterface.OnClickListener{dialog,id
                        ->
                        dialogBuilder.setMessage(Html.fromHtml("<font color=#000000>In order to proceed,This app requires access to your location. click</font>\n" +
                                "    <b>OK</b>\n" +
                                "    <font color=#000000>to exit or click</font>\n" +
                                "     <b>CANCEL</b>\n" +
                                "     <font color=#000000>to continue and access to your location</font>"))
                                // .setView(myview)
                                .setCancelable(false)
                                .setPositiveButton("ok", DialogInterface.OnClickListener { dialog, id
                                    ->
                                    startActivity(Intent(this,ThankYouActivity::class.java))


                                })
                                .setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog,id
                                    ->startActivity(Intent(this,ThankYouActivity::class.java))

                                })
                        var alert = dialogBuilder.create()
                        alert.show()
                    })

                var alert = dialogBuilder.create()
                alert.show()
                })
                    var alert = dialogBuilder.create()
                    alert.show()
                })
              var alert = dialogBuilder.create()
              alert.show()

    }
//fun dontAllow(){
//    var dialogBuilder = AlertDialog.Builder(this)
//
//
//}


    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener {
        override fun onScreenCaptureError(errorDescription: String) {

            stopScreenCapture()
            Toast.makeText(
                    this@VideoActivity, R.string.screen_capture_error,
                    Toast.LENGTH_LONG
            ).show()
        }

        override fun onFirstFrameAvailable() {

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(
                        this, R.string.screen_capture_permission_not_granted,
                        Toast.LENGTH_LONG
                ).show()

                switchCameraActionFab.visibility=View.VISIBLE
                localVideoActionFab.visibility=View.VISIBLE
                return
            }
            switchCameraActionFab.visibility=View.GONE
            localVideoActionFab.visibility=View.GONE
            screenCapturer = ScreenCapturer(this, resultCode, data!!, screenCapturerListener)
            startScreenCapture()
        }
    }



    private fun requestScreenCapturePermission() {
        Log.d(TAG, "Requesting permission to capture screen")
        val mediaProjectionManager: MediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // This initiates a prompt dialog for the user to confirm screen projection.
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
        )
    }

    private fun loadWebview(url: String) {
        var url1=""
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                Log.d("URL", url)

                if (url.toString().contains(AppConstant.DOCUSIGN_BASE_URL)) {
                   thumbnailVideoView.visibility=View.GONE
                    updateDocumentSignStatus()

                    // novigateThanksPage()

                    var layoutparams: CoordinatorLayout.LayoutParams =CoordinatorLayout.LayoutParams(
                            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                            CoordinatorLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutparams.gravity=Gravity.CENTER
                    layoucontainer.layoutParams = layoutparams
                    webView.visibility=View.GONE

                    if(screencaptureStarted){
                        thumbnailVideoView.visibility=View.GONE
                    }


                    if(url.toString().contains("ttl_expired"))
                    {


                        url1 = generateDocusignUrl().toString()
                        Log.d(TAG,"generate if  cal  URl ${url1}")

                        thumbnailVideoView.visibility=View.VISIBLE
                        webView.loadUrl(url1)

                    }



                    /*                var layoutparams: CoordinatorLayout.LayoutParams =CoordinatorLayout.LayoutParams(300,300)
                                    layoutparams.gravity=Gravity.BOTTOM or Gravity.END
                                    layoutparams.bottomMargin=600
                                    layoutparams.rightMargin=20
                                    webView.visibility = View.GONE
                                    thumbnailVideoView.visibility=View.VISIBLE
                                    primaryVideoView.visibility=View.VISIBLE*/

                } else {

                    view?.loadUrl(url)
                }


                //view?.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                 //   thumbnailVideoView.visibility=View.GONE
                reconnectingProgressBar.visibility =View.VISIBLE

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
              //  thumbnailVideoView.visibility=View.GONE
                reconnectingProgressBar.visibility =View.GONE
                removeNotification()
            }
        }

        webView.loadUrl(url)

        Log.d("URL", url)
    }

    private fun novigateThanksPage() {

        var thanksActivty = Intent(applicationContext, ThankYouActivity::class.java)
        startActivity(thanksActivty)
        finish()
    }


    private fun removeNotification() {
        if (intent.hasExtra(AppConstant.NOTIFICATION_ID)) {
            val notificationId = intent.getIntExtra(AppConstant.NOTIFICATION_ID, 0)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }


    lateinit var dialogBuilder :AlertDialog.Builder

    lateinit var alert:AlertDialog

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {

        when(requestCode){

            CAMERA_MIC_PERMISSION_REQUEST_CODE -> {
                if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
                    var cameraAndMicPermissionGranted = true

                    for (grantResult in grantResults) {
                        cameraAndMicPermissionGranted = cameraAndMicPermissionGranted and
                                (grantResult == PackageManager.PERMISSION_GRANTED)
                    }

                    if (cameraAndMicPermissionGranted) {
                        createAudioAndVideoTracks()
                    }
                    else {
denymessage()

                    }
                }

            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.size > 0 &&
                        grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    isGPSEnabled()

                    }
                 else {
                    Toast.makeText(
                            this,
                            "Location permissions needed. Please allow in App Settings for additional functionality.",
                            Toast.LENGTH_LONG
                    ).show()
                        dialogBuilder = AlertDialog.Builder(this)
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
                                    this.startActivity(Intent(this,ThankYouActivity::class.java))

                                })
                                .setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog,id
                                    ->
                                   requestPermissionForLocation()
//
//
                                })
                        alert= dialogBuilder.create()

                        if( ::alert.isInitialized&&!alert.isShowing) {
                            alert.show()
                        }


                    }
            }
          }

    }

    override fun onResume() {
        super.onResume()
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
      if (!isGpsEnabled){
          startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
          requestLocationUpdates()
      }else{
          requestLocationUpdates()
      }


        /*
         * If the local video track was released when the app was put in the background, recreate.
         */
        localVideoTrack = if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            if(screencaptureStarted){
createLocalVideoTrack(this,true,screenCapturer)
            }else
            {
            createLocalVideoTrack(
                    this,
                    true,
                    cameraCapturerCompat
            )
        }
        } else {
            localVideoTrack
        }
        localVideoTrack?.addSink(localVideoView)
        /*
         * If connected to a Room then share the local video track.
         */
        localVideoTrack?.let { localParticipant?.publishTrack(it) }
        /*
         * Update encoding parameters if they have changed.
         */
        localParticipant?.setEncodingParameters(encodingParameters)
        /*
         * Update reconnectin2g UI
         */
        room?.let {
            reconnectingProgressBar.visibility = if (it.state != Room.State.RECONNECTING)
                View.GONE else
                View.VISIBLE
            videoStatusTextView.text = "Connected to ${it.name}"
        }
        try {
            meetingParams?.roomName?.let { connectToRoom(it) }
        }catch (exception: java.lang.Exception)
        {}


        recever = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    var url=   intent.getStringExtra(BundleKeys.docusignurl)
                    if (!url.isNullOrEmpty()) {
/*                        val layoutParams = layoucontainer.layoutParams
                        layoutParams.height = 500
                        layoutParams.width = 500
                        layoucontainer.layoutParams = layoutParams
                        layoucontainer.gravity=Gravity.TOP*/

                        var layoutparams: CoordinatorLayout.LayoutParams =CoordinatorLayout.LayoutParams(
                                300,
                                300
                        )
                        layoutparams.gravity=Gravity.BOTTOM or Gravity.END
                        layoutparams.bottomMargin=600
                        layoutparams.rightMargin=20
                        layoucontainer.layoutParams = layoutparams
                        webView.visibility = View.VISIBLE
                        thumbnailVideoView.visibility=View.GONE
                        Log.d(TAG, "webview if starting")
                        loadWebview(url)
                    } else {
                  //      thumbnailVideoView.visibility=View.GONE
                        webView.visibility = View.GONE
                    }


                }
            }
        }
        var intentfilter = IntentFilter(getString(R.string.docusign_brodcost_recever))
        LocalBroadcastManager.getInstance(this).registerReceiver(recever, intentfilter)


        receverGPS = GeoLocationReceiver()
        var intentfilterGPS = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

        registerReceiver(receverGPS, intentfilterGPS)


    }


    override fun onPause() {
        unregisterReceiver(receverGPS)

        /*
         * If this local video track is being shared in a Room, remove from local
         * participant before releasing the video track. Participants will be notified that
         * the track has been removed.
         */
        localVideoTrack?.let { localParticipant?.unpublishTrack(it) }

        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        localVideoTrack?.release()
        localVideoTrack = null
        super.onPause()
    }

    override fun onDestroy() {

        /*
         * Tear down audio management and restore previous volume stream
         */
        audioSwitch.stop()
        volumeControlStream = savedVolumeControlStream

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        room?.disconnect()
        disconnectedFromOnDestroy = true

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        localAudioTrack?.release()
        localVideoTrack?.release()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(recever)

        meetingParams?.token=""

        /* var brodcostintent=Intent(getString(R.string.brodcost_recever))
         brodcostintent.putExtra(BundleKeys.MeetingParams,meetingParams)
         LocalBroadcastManager.getInstance(this).sendBroadcast(brodcostintent)*/
        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager.unbindService()
        }
        super.onDestroy()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        audioDeviceMenuItem = menu.findItem(R.id.menu_audio_device)

        /*
         * selected audio device changes.
         */
        audioSwitch.start { audioDevices, audioDevice ->
            updateAudioDeviceIcon(audioDevice)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_audio_device -> showAudioDevices()
        }
        return true
    }

    private fun requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO
                )) {
            Toast.makeText(
                    this,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    CAMERA_MIC_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkPermissionForCameraAndMicrophone(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED
    }

    private fun createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = createLocalAudioTrack(this, true)

        // Share your camera
        localVideoTrack = createLocalVideoTrack(
                this,
                true,
                cameraCapturerCompat
        )

        meetingParams?.roomName?.let { connectToRoom(it) }
    }


    private fun startScreenCapture() {
        share_screen_iv.setImageDrawable(getDrawable(R.drawable.screen_sharing))
        screencaptureStarted=true
        localVideoTrack = LocalVideoTrack.create(this, true, screenCapturer)
        meetingParams?.roomName?.let { connectToRoom(it) }

        thumbnailVideoView.visibility=View.GONE


    }

    private fun stopScreenCapture()
    {
        screencaptureStarted=false
        localVideoTrack?.removeSink(localVideoView)

//        if (webView.visibility == View.VISIBLE && webView.settings.javaScriptEnabled && webView.settings.loadWithOverviewMode && webView.settings.useWideViewPort && webView.settings.domStorageEnabled){
//
//
//            Log.d(TAG, "webview isVisible if starting")
//            thumbnailVideoView.visibility = View.GONE
//        }else{
//            Log.d(TAG, "webview isVisible else starting")
//            thumbnailVideoView.visibility = View.VISIBLE
//        }
//        Log.d(TAG, "webview isVisible visible end")
       thumbnailVideoView.visibility=View.VISIBLE
        localVideoTrack?.let { localParticipant?.unpublishTrack(it) }
        localVideoTrack?.release()

        localVideoTrack = createLocalVideoTrack(this, true, cameraCapturerCompat)
        localVideoTrack?.addSink(localVideoView)
        meetingParams?.roomName?.let { connectToRoom(it) }


    }

    private fun setAccessToken() {
        if (!BuildConfig.USE_TOKEN_SERVER) {
            /*
             * OPTION 1 - Generate an access token from the getting started portal
             * https://www.twilio.com/console/video/dev-tools/testing-tools and add
             * the variable TWILIO_ACCESS_TOKEN setting it equal to the access token
             * string in your local.properties file.
             */
            this.accessToken = TWILIO_ACCESS_TOKEN
        } else {
            /*
             * OPTION 2 - Retrieve an access token from your own web app.
             * Add the variable ACCESS_TOKEN_SERVER assigning it to the url of your
             * token server and the variable USE_TOKEN_SERVER=true to your
             * local.properties file.
             */
            retrieveAccessTokenfromServer()
        }
    }

    private fun connectToRoom(roomName: String) {
        try {
            audioSwitch.activate();
        }
        catch (e: Exception)
        {
            Log.d("AUDIO_ERROR", e.toString())
        }


        //room = connect(this, accessToken, roomListener)

        var token =meetingParams?.token

        if(token?.length!! >0)
        {
            connect(this, token, roomListener) {
                roomName(roomName)
                /*
                     * Add local audio track to connect options to share with participants.
                     */
                audioTracks(listOf(localAudioTrack))
                /*
                     * Add local video track to connect options to share with participants.
                     */
                videoTracks(listOf(localVideoTrack))

                /*
                     * Set the preferred audio and video codec for media.
                     */
                preferAudioCodecs(listOf(audioCodec))
                preferVideoCodecs(listOf(videoCodec))

                /*
                     * Set the sender side encoding parameters.
                     */
                encodingParameters(encodingParameters)

                /*
                     * Toggles automatic track subscription. If set to false, the LocalParticipant will receive
                     * notifications of track publish events, but will not automatically subscribe to them. If
                     * set to true, the LocalParticipant will automatically subscribe to tracks as they are
                     * published. If unset, the default is true. Note: This feature is only available for Group
                     * Rooms. Toggling the flag in a P2P room does not modify subscription behavior.
                     */
                enableAutomaticSubscription(enableAutomaticSubscription)
            }
        }

        setDisconnectAction()
    }

    /*
     * The initial state when there is no active room.
     */
    private fun initializeUI() {
        connectActionFab.setImageDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.call_disconnect_enable
                )
        )
        connectActionFab.visibility=View.VISIBLE
        //connectActionFab.setOnClickListener(connectActionClickListener())

        connectActionFab.setOnClickListener(dismiss())

        if(screencaptureStarted) {
            switchCameraActionFab.visibility = View.GONE
            localVideoActionFab.visibility = View.GONE


        }else
        {
            switchCameraActionFab.visibility = View.VISIBLE
            localVideoActionFab.visibility = View.VISIBLE
        }
        switchCameraActionFab.setOnClickListener(switchCameraClickListener())

        localVideoActionFab.setOnClickListener(localVideoClickListener())
        muteActionFab.visibility=View.VISIBLE
        muteActionFab.setOnClickListener(muteClickListener())
    }

    /*
     * Show the current available audio devices.
     */
    private fun showAudioDevices() {
        val availableAudioDevices = audioSwitch.availableAudioDevices

        audioSwitch.selectedAudioDevice?.let { selectedDevice ->
            val selectedDeviceIndex = availableAudioDevices.indexOf(selectedDevice)
            val audioDeviceNames = ArrayList<String>()

            for (a in availableAudioDevices) {
                audioDeviceNames.add(a.name)
            }

            AlertDialog.Builder(this)
                    .setTitle(R.string.room_screen_select_device)
                    .setSingleChoiceItems(
                            audioDeviceNames.toTypedArray<CharSequence>(),
                            selectedDeviceIndex
                    ) { dialog, index ->
                        dialog.dismiss()
                        val selectedAudioDevice = availableAudioDevices[index]
                        updateAudioDeviceIcon(selectedAudioDevice)
                        audioSwitch.selectDevice(selectedAudioDevice)
                    }.create().show()
        }
    }

    /*
     * Update the menu icon based on the currently selected audio device.
     */
    private fun updateAudioDeviceIcon(selectedAudioDevice: AudioDevice?) {
        val audioDeviceMenuIcon = when(selectedAudioDevice) {
            is BluetoothHeadset -> R.drawable.ic_bluetooth_white_24dp
            is WiredHeadset -> R.drawable.ic_headset_mic_white_24dp
            is Speakerphone -> R.drawable.ic_volume_up_white_24dp
            else -> R.drawable.ic_phonelink_ring_white_24dp
        }

        audioDeviceMenuItem.setIcon(audioDeviceMenuIcon)
    }

    /*
     * The actions performed during disconnect.
     */
    private fun setDisconnectAction() {
        connectActionFab.setImageDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.call_disconnect_enable
                )
        )
        connectActionFab.visibility=View.VISIBLE

        connectActionFab.setOnClickListener(disconnectClickListener())
    }

    /*
     * Creates an connect UI dialog
     */
    private fun showConnectDialog() {
        val roomEditText = EditText(this)
        alertDialog = createConnectDialog(
                roomEditText,
                connectClickListener(roomEditText), cancelConnectDialogClickListener(), this
        )
        alertDialog!!.show()
    }

    /*
     * Called when participant joins the room
     */
    private fun addRemoteParticipant(remoteParticipant: RemoteParticipant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (thumbnailVideoView.visibility == View.VISIBLE) {
            Snackbar.make(
                    connectActionFab,
                    "Multiple participants are not currently support in this UI",
                    Snackbar.LENGTH_LONG
            )
                    .setAction("Action", null).show()
            return
        }
        participantIdentity = remoteParticipant.identity
        videoStatusTextView.text = "Participant $participantIdentity joined"

        /*
         * Add participant renderer
         */
        remoteParticipant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { addRemoteParticipantVideo(it) }
            }
        }

        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(participantListener)
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private fun addRemoteParticipantVideo(videoTrack: VideoTrack) {
        if(!screencaptureStarted)
        {
            moveLocalVideoToThumbnailView()
            primaryVideoView.mirror = false
            videoTrack.addSink(primaryVideoView)
        }
        else
        {
            primaryVideoView.mirror = false
            videoTrack.addSink(primaryVideoView)
        }

    }

    private fun moveLocalVideoToThumbnailView() {
        if (thumbnailVideoView.visibility == View.GONE) {
            thumbnailVideoView.visibility = View.VISIBLE
            with(localVideoTrack) {
                this?.removeSink(primaryVideoView)
                this?.addSink(thumbnailVideoView)
            }
            localVideoView = thumbnailVideoView
            thumbnailVideoView.mirror = cameraCapturerCompat.cameraSource ==
                    CameraCapturerCompat.Source.FRONT_CAMERA
        }
    }

    /*
     * Called when participant leaves the room
     */
    private fun removeRemoteParticipant(remoteParticipant: RemoteParticipant) {
        videoStatusTextView.text = "Participant $remoteParticipant.identity left."
        if (remoteParticipant.identity != participantIdentity) {
            return
        }

        /*
         * Remove participant renderer
         */
        remoteParticipant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { removeParticipantVideo(it) }
            }
        }
        moveLocalVideoToPrimaryView()

        novigateThanksPage()
    }

    private fun removeParticipantVideo(videoTrack: VideoTrack) {
        videoTrack.removeSink(primaryVideoView)
    }

    private fun moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.visibility == View.VISIBLE) {
            thumbnailVideoView.visibility = View.GONE
            with(localVideoTrack) {
                this?.removeSink(thumbnailVideoView)
                this?.addSink(primaryVideoView)
            }
            localVideoView = primaryVideoView
            primaryVideoView.mirror = cameraCapturerCompat.cameraSource ==
                    CameraCapturerCompat.Source.FRONT_CAMERA
        }
    }

    private fun connectClickListener(roomEditText: EditText): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, _ ->
            /*
             * Connect to room
             */
            connectToRoom(roomEditText.text.toString())
        }
    }

    private fun disconnectClickListener(): View.OnClickListener {
        return View.OnClickListener {
            /*
             * Disconnect from room
             */
            room?.disconnect()
            initializeUI()

            callEndedStatusApi(BundleKeys.callDisconnect)
        }
    }


    fun apiGeoLocationEndcall(){
        var getSharedPreferences = this.applicationContext.getSharedPreferences("MyUser",Context.MODE_PRIVATE)
        var mailId = getSharedPreferences?.getString("MailId","")

        var locationStatus = LocationLatLan()
        locationStatus.emailId=mailId
        locationStatus.customerKeyNb = meetingParams?.customerKeyNb
        locationStatus.duringVideo = meetingParams?.duringVideo
        locationStatus.longitude = meetingParams?.longitude
        locationStatus.latitude = meetingParams?.latitude
        locationStatus.gpsOn = meetingParams?.gpsOn
        locationStatus.customerInCall =false


        ApiCall.retrofitClient.geoLocation(locationStatus).enqueue(object :
                retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@VideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                        .show()

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

  fun dismiss(): View.OnClickListener {
        return View.OnClickListener {



            apiGeoLocationEndcall()

            localVideoTrack?.let { localParticipant?.unpublishTrack(it) }
            localVideoTrack?.release()
            localVideoTrack = null

            localAudioTrack?.let {localParticipant?.unpublishTrack(it)}
            localAudioTrack?.release()
            localAudioTrack=null

            audioSwitch.stop()
            volumeControlStream = savedVolumeControlStream
            room?.disconnect()
//*****
  //          localVideoTrack?.enable(true)


            disconnectedFromOnDestroy = true


            var brodcostintent=Intent(getString(R.string.callend_brodcost_recever))
            brodcostintent.putExtra(BundleKeys.callDecline, "yes")
            LocalBroadcastManager.getInstance(this).sendBroadcast(brodcostintent)

            callEndedStatusApi(BundleKeys.callDisconnect)
            novigateThanksPage()
        }
    }

    private fun connectActionClickListener(): View.OnClickListener {
        return View.OnClickListener { showConnectDialog() }
    }

    private fun cancelConnectDialogClickListener(): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, _ ->
            initializeUI()
            alertDialog!!.dismiss()
        }
    }


    private fun callEndedStatusApi(callStatus: String) {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<DeclineCallWorker>()
        val data = Data.Builder()
        data.putInt(BundleKeys.CallKeyNb, meetingParams?.callKeyNb ?: 0)
        data.putInt(BundleKeys.CustomerKeyNb, meetingParams?.customerKeyNb ?: 0)
        data.putString(BundleKeys.CallEndReason, callStatus)
        uploadWorkRequest.setInputData(data.build())
        WorkManager
                .getInstance(this@VideoActivity)
                .enqueue(uploadWorkRequest.build())
    }

    private fun switchCameraClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val cameraSource = cameraCapturerCompat.cameraSource
            cameraCapturerCompat.switchCamera()
            if (thumbnailVideoView.visibility == View.VISIBLE) {
                thumbnailVideoView.mirror = cameraSource == CameraCapturerCompat.Source.BACK_CAMERA
            } else {
                primaryVideoView.mirror = cameraSource == CameraCapturerCompat.Source.BACK_CAMERA
            }
        }
    }

    private fun localVideoClickListener(): View.OnClickListener {
        return View.OnClickListener {
            /*
             * Enable/disable the local video track
             */
            localVideoTrack?.let {
                val enable = !it.isEnabled
                it.enable(enable)
                val icon: Int
                if (enable) {
                    icon = R.drawable.video_on
                    switchCameraActionFab.visibility=View.VISIBLE
                } else {
                    icon = R.drawable.video_off
                    switchCameraActionFab.visibility=View.GONE
                }
                localVideoActionFab.setImageDrawable(
                        ContextCompat.getDrawable(this@VideoActivity, icon)
                )
            }
        }
    }

    private fun muteClickListener(): View.OnClickListener {
        return View.OnClickListener {
            /*
             * Enable/disable the local audio track. The results of this operation are
             * signaled to other Participants in the same Room. When an audio track is
             * disabled, the audio is muted.
             */
            localAudioTrack?.let {
                val enable = !it.isEnabled
                it.enable(enable)
                val icon = if (enable)
                    R.drawable.mic_on
                else
                    R.drawable.mic_off
                muteActionFab.setImageDrawable(
                        ContextCompat.getDrawable(
                                this@VideoActivity, icon
                        )
                )
            }
        }
    }



    private fun updateDocumentSignStatus() {
        val docuSignStatusRequest = DocuSignStatusRequest()
        docuSignStatusRequest.callKeyNb = meetingParams?.callKeyNb
        docuSignStatusRequest.action="COMPLETED"
        ApiCall.retrofitClient.updateDocuSignStatus(docuSignStatusRequest).enqueue(object :
                retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@VideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
            }

            override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
            ) {
            }
        })
    }

    private fun retrieveAccessTokenfromServer() {
        Ion.with(this)
                .load("$ACCESS_TOKEN_SERVER?identity=${UUID.randomUUID()}")
                .asString()
                .setCallback { e, token ->
                    if (e == null) {
                        this@VideoActivity.accessToken = token
                    } else {
                        Toast.makeText(
                                this@VideoActivity,
                                R.string.error_retrieving_access_token, Toast.LENGTH_LONG
                        )
                                .show()
                    }
                }
    }

    private fun createConnectDialog(
            participantEditText: EditText,
            callParticipantsClickListener: DialogInterface.OnClickListener,
            cancelClickListener: DialogInterface.OnClickListener,
            context: Context
    ): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(context).apply {
            setIcon(R.drawable.call_disconnect_enable)
            setTitle("Connect to a room")
            setPositiveButton("Connect", callParticipantsClickListener)
            setNegativeButton("Cancel", cancelClickListener)
            setCancelable(false)
        }

        setRoomNameFieldInDialog(participantEditText, alertDialogBuilder, context)

        return alertDialogBuilder.create()
    }

    @SuppressLint("RestrictedApi")
    private fun setRoomNameFieldInDialog(
            roomNameEditText: EditText,
            alertDialogBuilder: AlertDialog.Builder,
            context: Context
    ) {
        roomNameEditText.hint = "room name"
        val horizontalPadding = context.resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
        val verticalPadding = context.resources.getDimensionPixelOffset(R.dimen.activity_vertical_margin)
        alertDialogBuilder.setView(
                roomNameEditText,
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                0
        )
    }
    fun denymessage(){
        dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
                //.setTitle("" )
                .setMessage(Html.fromHtml("Its look like you have turned off the permissions required for this feature.It can be enable under Phone Settings>>Apps>>Los AmigosS>>Permission."))
                // .setView(myview)
                .setCancelable(false)
                .setPositiveButton("Go to \n Settings", DialogInterface.OnClickListener { dialog, id
                    ->
                    var intent =Intent()
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    var uri= Uri.fromParts("package",this.packageName,null)
                    intent.setData(uri)
                   this.startActivity(intent)


                })

        alert= dialogBuilder.create()

        if( ::alert.isInitialized&&!alert.isShowing) {
            alert.show()
        }

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
                    Log.d("TAG", "expired url success method called ${response.code()}​​${url1} ")
                }

            }
            override fun onFailure(call: Call<Output>, t: Throwable) {
                Toast.makeText(this@VideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
            }


        })
        return url1
    }

}




