/*
package com.vbank.vidyovideoview

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vbank.vidyovideoview.broadcast.ConnectivityReceiver
import com.vbank.vidyovideoview.business.UserActionAdapter
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.event.ControlEvent
import com.vbank.vidyovideoview.event.IControlLink
import com.vbank.vidyovideoview.fullscreenintent.turnScreenOffAndKeyguardOn
import com.vbank.vidyovideoview.fullscreenintent.turnScreenOnAndKeyguardOff
import com.vbank.vidyovideoview.fullscreenintent.workmanager.DeclineCallWorker
import com.vbank.vidyovideoview.helper.AppConstant
import com.vbank.vidyovideoview.helper.BundleKeys
import com.vbank.vidyovideoview.helper.RSA_Algorithm
import com.vbank.vidyovideoview.model.*
import com.vbank.vidyovideoview.tiles.CustomTilesHelper
import com.vbank.vidyovideoview.tiles.RemoteHolder
import com.vbank.vidyovideoview.utils.AppUtils
import com.vbank.vidyovideoview.utils.Logger
import com.vbank.vidyovideoview.view.ControlView
import com.vbank.vidyovideoview.view.IVideoFrameListener
import com.vbank.vidyovideoview.webservices.ApiCall
import com.vidyo.VidyoClient.Connector.Connector
import com.vidyo.VidyoClient.Connector.Connector.*
import com.vidyo.VidyoClient.Connector.ConnectorPkg
import com.vidyo.VidyoClient.Device.*
import com.vidyo.VidyoClient.Device.Device.DeviceState
import com.vidyo.VidyoClient.Endpoint.Participant
import kotlinx.android.synthetic.main.activity_vidyo_video.*
import kotlinx.android.synthetic.main.connecting_layout.*
import kotlinx.android.synthetic.main.control_view_layout.*
import kotlinx.android.synthetic.main.layout_no_internet.*
import kotlinx.android.synthetic.main.layout_pin_entry.*
import kotlinx.android.synthetic.main.layout_user_action.*
import kotlinx.android.synthetic.main.layout_web_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

class VidyoVideoActivity : FragmentActivity(), IConnect,
    IRegisterLocalCameraEventListener,
    IRegisterRemoteCameraEventListener,
    IRegisterLocalSpeakerEventListener,
    IRegisterRemoteMicrophoneEventListener,
    IRegisterLocalMicrophoneEventListener,
    IRegisterResourceManagerEventListener,
    IRegisterRemoteWindowShareEventListener,
    IRegisterParticipantEventListener,
    IControlLink, View.OnClickListener, ConnectivityReceiver.ConnectivityReceiverListener,
    IVideoFrameListener {

    companion object {
        private const val PERMISSIONS_REQUEST_ALL = 1988
        private const val INTERVAL_TO_CLOSE: Long = 60000
        private const val INTERVAL_TO_DISCONNECT: Long = 60000
        private const val REFRESH_BANKER_NAME: Long = 3000
    }

    private var mPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )

    private var controlView: ControlView? = null
    private var progressBar: View? = null

    private var connector: Connector? = null

    private val isCameraDisabledForBackground = AtomicBoolean(false)
    private val isDisconnectAndQuit = AtomicBoolean(false)

    private var customTilesHelper: CustomTilesHelper? = null
    private var userDataParams: UserDataParams? = null
    private var meetingParams: MeetingParams? = null

    private var rotate: Animation? = null
    private val myReceiver: ConnectivityReceiver? = ConnectivityReceiver()
    private var isDisconnected: Boolean = false
    private var refreshBankerName: Boolean = false
    private var waitingRoom: Boolean = false
    private var isParticipantJoined: Boolean = false

    private val handler: Handler? = Handler()
    private val handlerBankerName: Handler? = Handler()
    private val handlerDisconnect: Handler? = Handler()
    private var lastSelectedItem: Int = -1
    private var dataItems: MutableList<DataItem>? = null
    private var userActionAdapter: UserActionAdapter? = null
    private var currentActions: UserActions = UserActions.NONE
    private var isPinGenerated: Boolean = false
    private var tokenResponse: TokenResponse? = null
    private var isUserActionClicked: Boolean = false

    enum class UserActions {
        NONE, LIST_OF_ACTIONS, ACTIONS
    }

    enum class ActionType {
        sigining, PIN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vidyo_video)
        removeNotification()
        turnScreenOnAndKeyguardOff()
        ConnectorPkg.initialize()
        ConnectorPkg.setApplicationUIContext(this@VidyoVideoActivity)
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
        checkPermissions()

        if(!meetingParams?.docusignurl.isNullOrEmpty())
        {
            initWebView()
           loadUrl(meetingParams?.docusignurl)

            layout_web_view.visibility = View.VISIBLE
            layout_pin_entry.visibility = View.GONE
            lnr_action_items.visibility = View.GONE
        }
    }

    private fun removeNotification() {
        if (intent.hasExtra(AppConstant.NOTIFICATION_ID)) {
            val notificationId = intent.getIntExtra(AppConstant.NOTIFICATION_ID, 0)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        if (connector != null) {
            val state = controlView?.getState()
            connector?.setMode(ConnectorMode.VIDYO_CONNECTORMODE_Foreground)
            connector?.setCameraPrivacy(state?.isMuteCamera ?: true)
            connector?.setMicrophonePrivacy(state?.isMuteMic ?: true)
            connector?.setSpeakerPrivacy(state?.isMuteSpeaker ?: true)
        }
        val localCamera = customTilesHelper?.getLastSelectedLocalCamera()
        if (connector != null && localCamera != null && isCameraDisabledForBackground.getAndSet(
                false
            )
        ) {
            connector?.selectLocalCamera(localCamera)
        }
    }

    override fun onResume() {
        super.onResume()
        if (refreshBankerName) {
            startTimerToGetBankerName()
        }
        setConnectivityListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BundleKeys.CONNECTING_CHANGE)
        registerReceiver(myReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(myReceiver)
        stopTimerToGetBankerName()
    }

    override fun onStop() {
        super.onStop()
        if (connector != null) {
            connector?.setMode(ConnectorMode.VIDYO_CONNECTORMODE_Background)
            connector?.setCameraPrivacy(true)
            connector?.setMicrophonePrivacy(true)
            connector?.setSpeakerPrivacy(true)
        }
        if (!isFinishing && connector != null && controlView?.getState()?.isMuteCamera != true &&
            !isCameraDisabledForBackground.getAndSet(true)
        ) {
            connector?.selectLocalCamera(null)
            customTilesHelper?.detachLocal()
        }
    }


    private fun checkPermissions() {
        val permissionsNeeded: MutableList<String> =
            ArrayList()
        for (permission in mPermissions) { // Check if the permission has already been granted.
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) permissionsNeeded.add(permission)
        }
        if (permissionsNeeded.size > 0) {
            // Request any permissions which have not been granted. The result will be called back in onRequestPermissionsResult.
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                PERMISSIONS_REQUEST_ALL
            )
        } else {
            generateToken()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Logger.w("onRequestPermissionsResult: number of requested permissions = " + permissions.size)
        // If the expected request code is received, begin rendering video.
        if (requestCode == PERMISSIONS_REQUEST_ALL) {
            for (i in permissions.indices) Logger.w("permission: " + permissions[i] + " " + grantResults[i])
            // Begin listening for video view size changes.
            generateToken()
        } else {
            Logger.w("ERROR! Unexpected permission requested. Video will not be rendered.")
        }
    }

    private fun init() {
        progressBar = findViewById(R.id.progress)
        progressBar?.visibility = View.GONE
        controlView = findViewById(R.id.control_view)
        controlView?.registerListener(this)

        btnClear.setOnClickListener(this)
        btnDone.setOnClickListener(this)
        txt_action_item_title.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        txt_header_debit_pin.setOnClickListener(this)
        btn_exit.setOnClickListener(this)
        */
/*
         * Connector instance created with NULL passed as video frame. Local & RemoteHolder camera will be assigned later.
         *//*


        connector = Connector(
            null,
            ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default,
            2, "*@VidyoClient info@vidyovideoview info warning",
            AppUtils.configLogFile(this), 0
        )
        Logger.i("Connector instance has been created.")
        controlView?.showVersion(connector?.version)
        val container = findViewById<RelativeLayout>(R.id.master_container)
        frame_video.Register(this)

        customTilesHelper = CustomTilesHelper(
            this,
            connector,
            container,
            container.measuredWidth,
            container.measuredHeight,
            frame_video
        )


        //container.setOnTouchListener(this)
        */
/*
         * Register all the  listeners required for custom implementation
         *//*

        connector?.registerLocalCameraEventListener(this)
        connector?.registerLocalSpeakerEventListener(this)
        connector?.registerLocalMicrophoneEventListener(this)
        connector?.registerRemoteCameraEventListener(this)
        connector?.registerRemoteMicrophoneEventListener(this)
        connector?.registerRemoteWindowShareEventListener(this)
        connector?.registerParticipantEventListener(this)
        connectMeeting()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (customTilesHelper != null) customTilesHelper?.requestInvalidate()
    }


    override fun onControlEvent(event: ControlEvent<Any>?) {
        if (connector == null) return
        when (event?.call) {
            ControlEvent.Call.CONNECT_DISCONNECT -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                controlView?.disable(true)
                val state = event.getValue() as Boolean
                controlView?.updateConnectionState(if (state) ControlView.ConnectionState.CONNECTING else ControlView.ConnectionState.DISCONNECTING)
                if (connector == null) {
                    Logger.e("Connector is null")
                    return
                }
                if (state) {
                    connector?.connect(
                        meetingParams?.host,
                        meetingParams?.token,
                        meetingParams?.displayName,
                        meetingParams?.resource,
                        this
                    )
                } else {
                    progressBar?.visibility = View.VISIBLE
                    val connectionState = connector?.state
                    if (connectionState == ConnectorState.VIDYO_CONNECTORSTATE_Connected) {
                        if (isParticipantJoined) {
                            moveToThankYouScreen()
                            callEndedStatusApi(BundleKeys.callCompleted)
                        } else {
                            if (meetingParams?.isFromNotification == true) {
                                if (waitingRoom) {
                                    callEndedStatusApi(BundleKeys.callWaiting)
                                } else {
                                    callEndedStatusApi(BundleKeys.callDisconnect)
                                }
                                moveToHomeScreen()
                            } else {
                                if (waitingRoom) {
                                    callEndedStatusApi(BundleKeys.callWaiting)
                                } else {
                                    callEndedStatusApi(BundleKeys.callDisconnect)
                                }
                            }
                        }
                    }
                    connector?.disconnect()
                }
            }
            ControlEvent.Call.MUTE_CAMERA -> {
                val cameraPrivacy = event.getValue() as Boolean
                connector?.setCameraPrivacy(cameraPrivacy)
                if (cameraPrivacy) {
                    connector?.selectLocalCamera(null)
                    customTilesHelper?.detachLocal()
                } else {
                    connector?.selectLocalCamera(customTilesHelper!!.getLastSelectedLocalCamera())
                }
            }
            ControlEvent.Call.MUTE_MIC -> connector?.setMicrophonePrivacy(event.getValue() as Boolean)
            ControlEvent.Call.MUTE_SPEAKER -> connector?.setSpeakerPrivacy(event.getValue() as Boolean)
            ControlEvent.Call.CYCLE_CAMERA -> connector?.cycleCamera()
            ControlEvent.Call.PIP_CONTROL -> {
                if (!isUserActionClicked) {
                    isUserActionClicked = true
                    controlView?.disable(true)
                    getUserActionItems()
                }
            }
            ControlEvent.Call.DEBUG_OPTION -> {
                val value = event.getValue() as Boolean
                if (value) {
                    connector?.enableDebug(7776, "")
                } else {
                    connector?.disableDebug()
                }
                Toast.makeText(
                    this@VidyoVideoActivity,
                    getString(R.string.debug_option) + value,
                    Toast.LENGTH_SHORT
                ).show()
            }
            ControlEvent.Call.SEND_LOGS -> AppUtils.sendLogs(this)
        }
    }

    override fun onSuccess() {
        if (!connector!!.registerResourceManagerEventListener(this)) {
            Logger.e("Failed to register resource manager event listener")
        } else {
            Logger.e("Resource manager event listener succeed.")
        }
        runOnUiThread {
            if (!isParticipantJoined) {
                waitingUI()
            }
            Toast.makeText(this@VidyoVideoActivity, R.string.connected, Toast.LENGTH_SHORT)
                .show()
            progressBar?.visibility = View.GONE
            controlView?.connectedCall(true)
            controlView?.updateConnectionState(ControlView.ConnectionState.CONNECTED)
            controlView?.disable(false)
        }
    }

    override fun onFailure(connectorFailReason: ConnectorFailReason) {
        if (connector != null) connector?.unregisterResourceManagerEventListener()
        runOnUiThread {
            Toast.makeText(this@VidyoVideoActivity, connectorFailReason.name, Toast.LENGTH_SHORT)
                .show()
            progressBar?.visibility = View.GONE
            controlView?.connectedCall(false)
            controlView?.updateConnectionState(ControlView.ConnectionState.FAILED)
            controlView?.disable(false)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            stopConnectingAnimation()
            val state = connector?.state
            callEndedStatusApi(connectorFailReason.name)
            if (state == ConnectorState.VIDYO_CONNECTORSTATE_Connected) {
                disConnectingCall()
            } else {
                finish()
            }
        }
    }

    override fun onDisconnected(connectorDisconnectReason: ConnectorDisconnectReason?) {
        if (connector != null) connector?.unregisterResourceManagerEventListener()
        runOnUiThread {
            if (connectorDisconnectReason == ConnectorDisconnectReason.VIDYO_CONNECTORDISCONNECTREASON_ConnectionLost) {
                isDisconnected = true
                isDisconnectAndQuit.set(false)
                internetDisconnectedUI()
            } else {
                isDisconnectAndQuit.set(true)
            }
            Toast.makeText(this@VidyoVideoActivity, R.string.disconnected, Toast.LENGTH_SHORT)
                .show()
            progressBar?.visibility = View.GONE
            controlView?.connectedCall(false)
            controlView?.updateConnectionState(ControlView.ConnectionState.DISCONNECTED)
            controlView?.disable(false)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            */
/* Wrap up the conference *//*

            if (isDisconnectAndQuit.get()) {
                finish()
            }
        }
    }

    override fun onBackPressed() {
        when (currentActions) {
            UserActions.NONE -> {
                backPressed()
            }
            UserActions.LIST_OF_ACTIONS -> {
//                clearWebHistory()
//                currentActions = UserActions.NONE
//                layout_user_action.visibility = View.GONE
            }
            UserActions.ACTIONS -> {
//                clearWebHistory()
//                currentActions = UserActions.NONE
//                layout_user_action.visibility = View.GONE
            }
        }
    }

    private fun backPressed() {
        if (connector == null) {
            Logger.e("Connector is null!")
            finish()
            return
        }
        val state = connector?.state
        if (state == ConnectorState.VIDYO_CONNECTORSTATE_Idle || state == ConnectorState.VIDYO_CONNECTORSTATE_Ready) {
            super.onBackPressed()
        } else { */
/* You are still connecting or connected *//*

            Toast.makeText(
                this,
                "You have to disconnect or await connection first",
                Toast.LENGTH_SHORT
            ).show()
            */
/* Start disconnection if connected. Quit afterward. *//*

            if (state == ConnectorState.VIDYO_CONNECTORSTATE_Connected && !isDisconnectAndQuit.get()) {
                isDisconnectAndQuit.set(true)
                onControlEvent(ControlEvent(ControlEvent.Call.CONNECT_DISCONNECT, false))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOffAndKeyguardOn()
        if (isDisconnected) {
            stopTimerToFinish()
        }
        stopTimerToDisconnect()
        stopTimerToGetBankerName()
        if (controlView != null) controlView?.unregisterListener()
        if (customTilesHelper != null) customTilesHelper?.shutDown()
        if (connector != null) {
            connector?.unregisterLocalCameraEventListener()
            connector?.unregisterLocalSpeakerEventListener()
            connector?.unregisterLocalMicrophoneEventListener()
            connector?.unregisterRemoteCameraEventListener()
            connector?.unregisterRemoteMicrophoneEventListener()
            connector?.unregisterParticipantEventListener()
            connector?.disable()
            connector = null
        }
        ConnectorPkg.uninitialize()
        ConnectorPkg.setApplicationUIContext(null)
        Logger.i("Connector instance has been released.")
    }

    override fun onLocalCameraAdded(localCamera: LocalCamera?) {
        Logger.i("Local camera added.")
    }

    override fun onLocalCameraSelected(localCamera: LocalCamera?) {
        Logger.i(VidyoVideoActivity::class.java, "Local camera selected")
        runOnUiThread { customTilesHelper?.attachLocal(localCamera) }
    }

    override fun onLocalCameraRemoved(localCamera: LocalCamera?) {
        Logger.i("Local camera removed.")
    }

    override fun onRemoteCameraAdded(
        remoteCamera: RemoteCamera?,
        participant: Participant?
    ) {
        Logger.i(VidyoVideoActivity::class.java, "RemoteHolder camera added")
        runOnUiThread {
            customTilesHelper?.attachRemote(
                RemoteHolder(
                    participant,
                    remoteCamera
                )
            )
        }
    }

    override fun onRemoteCameraRemoved(
        remoteCamera: RemoteCamera?,
        participant: Participant?
    ) {
        Logger.i(VidyoVideoActivity::class.java, "RemoteHolder camera removed")
        runOnUiThread {
            participant?.let {
                customTilesHelper?.detachRemote(participant, false)
            }
        }
    }

    override fun onRemoteWindowShareAdded(
        remoteWindowShare: RemoteWindowShare?,
        participant: Participant?
    ) {
        Logger.i(VidyoVideoActivity::class.java, "RemoteHolder share added")
    }

    override fun onRemoteWindowShareRemoved(
        remoteWindowShare: RemoteWindowShare?,
        participant: Participant?
    ) {
        Logger.i(VidyoVideoActivity::class.java, "RemoteHolder share removed")
    }

    override fun onLoudestParticipantChanged(
        participant: Participant,
        b: Boolean
    ) {
        Logger.i("Loudest participant arrived. Name: %s", participant.getName())
        runOnUiThread {
            if (customTilesHelper != null) customTilesHelper?.updateLoudest(participant)
        }
    }

    override fun onLocalCameraStateUpdated(
        localCamera: LocalCamera?,
        deviceState: DeviceState?
    ) {
    }

    override fun onRemoteWindowShareStateUpdated(
        remoteWindowShare: RemoteWindowShare?,
        participant: Participant?,
        deviceState: DeviceState?
    ) {
    }

    override fun onRemoteCameraStateUpdated(
        remoteCamera: RemoteCamera?,
        participant: Participant?,
        deviceState: DeviceState?
    ) {
    }

    override fun onLocalSpeakerAdded(localSpeaker: LocalSpeaker?) {}

    override fun onLocalSpeakerRemoved(localSpeaker: LocalSpeaker?) {}

    override fun onLocalSpeakerSelected(localSpeaker: LocalSpeaker?) {}

    override fun onLocalSpeakerStateUpdated(
        localSpeaker: LocalSpeaker?,
        deviceState: DeviceState?
    ) {
    }

    override fun onRemoteMicrophoneAdded(
        remoteMicrophone: RemoteMicrophone?,
        participant: Participant?
    ) {
    }

    override fun onRemoteMicrophoneRemoved(
        remoteMicrophone: RemoteMicrophone?,
        participant: Participant?
    ) {
    }

    override fun onRemoteMicrophoneStateUpdated(
        remoteMicrophone: RemoteMicrophone?,
        participant: Participant?,
        deviceState: DeviceState?
    ) {
    }

    override fun onLocalMicrophoneAdded(localMicrophone: LocalMicrophone?) {}

    override fun onLocalMicrophoneRemoved(localMicrophone: LocalMicrophone?) {}

    override fun onLocalMicrophoneSelected(localMicrophone: LocalMicrophone?) {}

    override fun onLocalMicrophoneStateUpdated(
        localMicrophone: LocalMicrophone?,
        deviceState: DeviceState?
    ) {
    }

    override fun onAvailableResourcesChanged(
        cpuEncode: Int,
        cpuDecode: Int,
        bandwidthSend: Int,
        bandwidthReceive: Int
    ) {
    }

    override fun onMaxRemoteSourcesChanged(i: Int) {}

    override fun onParticipantJoined(participant: Participant?) {
        runOnUiThread {
            meetingUI()
        }
        isParticipantJoined = true
    }

    override fun onParticipantLeft(participant: Participant?) {

    }

    override fun onDynamicParticipantChanged(arrayList: ArrayList<Participant?>?) {

    }

    private fun connectingUI() {
        img_end_connecting.setOnClickListener(this)
        startConnectingAnimation()
        frame_video.visibility = View.GONE
        lnrConnecting.visibility = View.VISIBLE
        layout_no_internet.visibility = View.GONE
        txt_connecting_status.text = getString(R.string.txt_connecting_banker_title)
    }

    private fun waitingUI() {
        waitingRoom = true
        startTimerToDisconnect()
        frame_video.visibility = View.GONE
        lnrConnecting.visibility = View.VISIBLE
        layout_no_internet.visibility = View.GONE
        if (meetingParams?.bankerName.isNullOrBlank()) {
            refreshBankerName = true
            setBankerName(getString(R.string.txt_lbl_banker))
            startTimerToGetBankerName()
        } else {
            refreshBankerName = false
            setBankerName(meetingParams?.bankerName)
        }
    }

    private fun meetingUI() {
        waitingRoom = false
        refreshBankerName = false
        stopTimerToDisconnect()
        stopTimerToGetBankerName()
        stopConnectingAnimation()
        if (frame_video.visibility != View.VISIBLE) {
            frame_video.visibility = View.VISIBLE
            lnrConnecting.visibility = View.GONE
            layout_no_internet.visibility = View.GONE
        }
    }

    private fun internetDisconnectedUI() {
        startConnectingAnimation()
        startTimerToFinish()
        layout_no_internet.visibility = View.VISIBLE
        frame_video.visibility = View.GONE
        lnrConnecting.visibility = View.GONE
    }

    private fun connectMeeting() {
        isDisconnected = false
        onControlEvent(ControlEvent(ControlEvent.Call.CONNECT_DISCONNECT, true))
    }

    private fun disConnectingCall() {
        onControlEvent(ControlEvent(ControlEvent.Call.CONNECT_DISCONNECT, false))
    }

    private fun generateToken() {
        progressBar = findViewById(R.id.progress)
        progressBar?.visibility = View.GONE
        connectingUI()
        if (meetingParams?.isFromNotification == true) {
            init()
        } else {
            val androidID = UUID.randomUUID().toString()
            val tokenParams = TokenParams(
                userDataParams?.displayName,
                androidID,
                userDataParams?.customerKeyNb,
                branchKeyNb = userDataParams?.branchKeyNb,
                bankerKeyNb = userDataParams?.brankerKeyNb
            )
            meetingParams?.bankerName = userDataParams?.bankerName
            meetingParams?.customerKeyNb = userDataParams?.customerKeyNb
            meetingParams?.displayName = userDataParams?.displayName

            ApiCall.retrofitClient.generateToken(tokenParams).enqueue(object :
                retrofit2.Callback<TokenResponse> {
                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@VidyoVideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    stopConnectingAnimation()
                }

                override fun onResponse(
                    call: Call<TokenResponse>,
                    response: Response<TokenResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            tokenResponse = response.body() as TokenResponse
                            meetingParams?.token = tokenResponse?.data?.vidyoToken
                            meetingParams?.resource = tokenResponse?.data?.roomId
                            meetingParams?.callKeyNb = tokenResponse?.data?.callKeyNb
                            init()
                        }
                    }
                }
            })
        }
    }

    private fun getBankerName(callKeyNb: Int?) {
        ApiCall.retrofitClient.getBankerName(callKeyNb).enqueue(object :
            retrofit2.Callback<BankerName> {
            override fun onFailure(call: Call<BankerName>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<BankerName>,
                response: Response<BankerName>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val bankerName = response.body() as BankerName
                        bankerName.data?.bankerName?.let {
                            if (!bankerName.data.bankerName.isBlank()) {
                                setBankerName(bankerName.data.bankerName)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun setBankerName(bankerName: String?) {
        val builder = StringBuilder()
        builder.append(bankerName).append(" ").append(getString(R.string.txt_waiting_banker_title))
        txt_connecting_status.text = builder.toString()
    }

    //start timer to get banker name
    private fun startTimerToGetBankerName() {
        handlerBankerName?.postDelayed(runnableBankerName, REFRESH_BANKER_NAME)
    }

    private fun stopTimerToGetBankerName() {
        handlerBankerName?.removeCallbacks(runnableBankerName)
    }

    private val runnableBankerName = object : Runnable {
        override fun run() {
            getBankerName(meetingParams?.callKeyNb)
            handlerBankerName?.postDelayed(this, REFRESH_BANKER_NAME)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_end_connecting -> {
                val state = connector?.state
                if (state == ConnectorState.VIDYO_CONNECTORSTATE_Connected) {
                    if (isParticipantJoined) {
                        moveToThankYouScreen()
                        callEndedStatusApi(BundleKeys.callCompleted)
                    } else {
                        if (meetingParams?.isFromNotification == true) {
                            moveToHomeScreen()
                        }
                        callEndedStatusApi(BundleKeys.callDisconnect)
                    }
                    disConnectingCall()
                } else {
                    callEndedStatusApi(BundleKeys.callDisconnect)
                    if (meetingParams?.isFromNotification == true) {
                        moveToHomeScreen()
                    }
                    finish()
                }
            }
            R.id.btnClear -> {
                clearPinField()
            }
            R.id.btnDone -> {
                if (isPinGenerated) {
                    closePinEntryLayout()
                } else {
                    if (edtPin.text?.toString().isNullOrBlank() || edtPinReEnter.text?.toString()
                            .isNullOrBlank()
                    ) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.error_enter_pin),
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                    if (edtPin.text?.toString() == edtPinReEnter.text?.toString()) {
                        progress_pin_entry?.visibility = View.VISIBLE
                        disablePinScreenButton()
                        GlobalScope.launch(Dispatchers.Main) {
                            val encrypt = async(Dispatchers.IO) {
                                val pin = edtPin.text.toString()
                                val rsaAlgorithm = RSA_Algorithm()
                                val encryptionPin = rsaAlgorithm.encodeRawData(pin)
                                encryptionPin
                            }
                            callPinChangeApi(encrypt.await())
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.error_pin_not_match),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            R.id.lnrDataItem -> {
                val position = v.getTag(R.id.lnrDataItem) as Int
                selectUserActionItem(position)
            }
            R.id.txt_action_item_title -> {
                onVideoFrameClicked()
            }
            R.id.btn_next -> {
                showUserActionItems(dataItems?.get(lastSelectedItem))
            }
            R.id.txt_header_debit_pin -> {
                closePinEntryLayout()
            }
            R.id.btn_exit -> {
                callEndedStatusApi(BundleKeys.callDisconnect)
                finish()
            }
        }
    }

    private fun disablePinScreenButton() {
        btnClear.isEnabled = false
        btnDone.isEnabled = false
    }

    private fun enablePinScreenButton() {
        btnClear.isEnabled = true
        btnDone.isEnabled = true
    }

    private fun selectUserActionItem(position: Int) {
        btn_next.isEnabled = true
        btn_next.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_splash))

        if (lastSelectedItem != position) {
            if (lastSelectedItem != -1) {
                dataItems?.get(lastSelectedItem)?.isSelected = false
                userActionAdapter?.notifyItemChanged(lastSelectedItem)
            }
            dataItems?.get(position)?.isSelected = true
            userActionAdapter?.notifyItemChanged(position)
            lastSelectedItem = position
        }
    }

    private fun showUserActionItems(dataItem: DataItem?) {
        dataItem?.type?.let {
            if (dataItem.type == ActionType.sigining.name) {
                currentActions = UserActions.ACTIONS
                initWebView()
                loadUrl(dataItem.url)
                layout_web_view.visibility = View.VISIBLE
                layout_pin_entry.visibility = View.GONE
                lnr_action_items.visibility = View.GONE
            } else if (dataItem.type == ActionType.PIN.name) {
                enablePinScreenButton()
                currentActions = UserActions.ACTIONS
                layout_pin_entry.visibility = View.VISIBLE
                lnr_pin_entry.visibility = View.VISIBLE
                lnr_pin_entry_success.visibility = View.GONE
                expiry_date.text = dataItem.expirydate
                card_number.text = dataItem.cardNumber
                layout_web_view.visibility = View.GONE
                lnr_action_items.visibility = View.GONE
                btnClear.visibility = View.VISIBLE
            }
        }
    }

    private fun startConnectingAnimation() {
        rotate = AnimationUtils.loadAnimation(this@VidyoVideoActivity, R.anim.rotate)
        img_connecting_circle.startAnimation(rotate)
    }

    private fun stopConnectingAnimation() {
        rotate?.cancel()
    }

    private fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected && isDisconnected && meetingParams?.token?.isNotEmpty() == true) {
            stopTimerToFinish()
            internetDisconnectedUI()
            connectMeeting()
        }
    }

    //If internet is not connected for a while close the screen
    private fun startTimerToFinish() {
        handler?.postDelayed(runnable, INTERVAL_TO_CLOSE)
    }

    private fun stopTimerToFinish() {
        handler?.removeCallbacks(runnable)
    }

    private val runnable = Runnable {
        if (isDisconnected) {
            Toast.makeText(
                applicationContext,
                getString(R.string.message_disconnection_call_network_lost),
                Toast.LENGTH_LONG
            ).show()
            callEndedStatusApi(BundleKeys.callDisconnect)
            finish()
        }
    }

    private fun getUserActionItems() {
        lastSelectedItem = -1
        btn_next.isEnabled = false
        btn_next.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_grey2))
        progressBar?.visibility = View.VISIBLE
        ApiCall.retrofitClient.getUserActionItems(meetingParams?.callKeyNb).enqueue(object :
            retrofit2.Callback<UserActionItems> {
            override fun onFailure(call: Call<UserActionItems>, t: Throwable) {
                controlView?.disable(false)
                isUserActionClicked = false
                Toast.makeText(this@VidyoVideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
                progressBar?.visibility = View.GONE
            }

            override fun onResponse(
                call: Call<UserActionItems>,
                response: Response<UserActionItems>
            ) {
                controlView?.disable(false)
                isUserActionClicked = false
                enterPIPMode()
                progressBar?.visibility = View.GONE
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val userActionItems = response.body() as UserActionItems
                        dataItems = userActionItems.data
                        currentActions = UserActions.LIST_OF_ACTIONS
                        layout_pin_entry.visibility = View.GONE
                        layout_web_view.visibility = View.GONE
                        lnr_action_items.visibility = View.VISIBLE
                        layout_user_action.visibility = View.VISIBLE
                        if (dataItems?.isNotEmpty() == true) {
                            recycler_view.visibility = View.VISIBLE
                            no_action_items_available.visibility = View.GONE
                            dataItems?.let { it ->
                                showUserActionList(it)
                            }
                        } else {
                            recycler_view.visibility = View.GONE
                            no_action_items_available.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun showUserActionList(userActionItems: List<DataItem>) {
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.recycler_view_border)
        drawable?.let {
            itemDecoration.setDrawable(it)
        }
        recycler_view.addItemDecoration(itemDecoration)
        userActionAdapter = UserActionAdapter(userActionItems, this)
        recycler_view.adapter = userActionAdapter
    }

    private fun successfullyPinChanged() {
        clearPinField()
        isPinGenerated = true
        btnClear.visibility = View.GONE
        lnr_pin_entry.visibility = View.GONE
        lnr_pin_entry_success.visibility = View.VISIBLE
    }


    private fun closePinEntryLayout() {
        clearPinField()
        isPinGenerated = false
        onVideoFrameClicked()
    }

    private fun clearPinField() {
        edtPin.text?.clear()
        edtPinReEnter.text?.clear()
    }


    private fun callPinChangeApi(encryptPin: String) {
        val cardNo = card_number.text.toString()
        val expireDate = expiry_date.text.toString()
        val pinChangeRequest = PinChangeRequest(
            callKeyNb = meetingParams?.callKeyNb,
            pinNumber = encryptPin,
            cardNumber = cardNo,
            expireDate = expireDate,
            customerKeyNb = meetingParams?.customerKeyNb
        )

        ApiCall.retrofitClient.submitPinChange(pinChangeRequest).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                enablePinScreenButton()
                progress_pin_entry?.visibility = View.GONE
                Toast.makeText(this@VidyoVideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                enablePinScreenButton()
                progress_pin_entry?.visibility = View.GONE
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        successfullyPinChanged()
                    }
                }
            }
        })
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        clearWebHistory()
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        */
/*webView.webViewClient = object : WebViewClient() {
          //  override
           *//*
*/
/* fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }*//*
*/
/*

            *//*
*/
/*override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (Uri.parse(request?.url.toString()) == Uri.parse(AppConstant.CallBackURL)) {
                    updateDocumentSignStatus()
                    onVideoFrameClicked()
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                web_progress_bar.visibility = View.GONE
            }*//*
*/
/*
        }*//*


        webView.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == MotionEvent.ACTION_UP && webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
                return false
            }
        })
    }

    private fun clearWebHistory() {
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.clearCache(true)
    }

    private fun loadUrl(pageUrl: String?) {
        web_progress_bar.visibility = View.VISIBLE
        webView.loadUrl(pageUrl)
    }

    private fun startTimerToDisconnect() {
        handlerDisconnect?.postDelayed(runnableDisconnect, INTERVAL_TO_DISCONNECT)
    }

    private fun stopTimerToDisconnect() {
        handlerDisconnect?.removeCallbacks(runnableDisconnect)
    }

    private val runnableDisconnect = Runnable {
        if (waitingRoom) {
            Toast.makeText(
                applicationContext,
                getString(R.string.message_banker_not_joining),
                Toast.LENGTH_LONG
            ).show()
            disConnectingCall()
        }
    }

    private fun moveToThankYouScreen() {
        try {
            val myKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!myKM.isDeviceLocked) {
                val intent = Intent(this, Class.forName(getString(R.string.path_to_thanks_screen)))
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveToHomeScreen() {
        try {
            val myKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!myKM.isDeviceLocked) {
                val intent = Intent(this, Class.forName(getString(R.string.path_to_home_screen)))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(AppConstant.MOVE_TO_USER_INPUT_SCREEN, true)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            .getInstance(this@VidyoVideoActivity)
            .enqueue(uploadWorkRequest.build())
    }

    private fun updateDocumentSignStatus() {
        val docuSignStatusRequest = DocuSignStatusRequest()
        docuSignStatusRequest.callKeyNb = meetingParams?.callKeyNb
        ApiCall.retrofitClient.updateDocuSignStatus(docuSignStatusRequest).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@VidyoVideoActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
            }
        })
    }

    override fun onVideoFrameClicked() {
        clearWebHistory()
        currentActions = UserActions.NONE
        layout_user_action.visibility = View.GONE
        controlView?.pipModeOrFullScreen(false)
        customTilesHelper?.isPIPModeEnabled(false)
    }

    private fun enterPIPMode() {
        controlView?.pipModeOrFullScreen(true)
        customTilesHelper?.isPIPModeEnabled(true)
    }

}

*/
