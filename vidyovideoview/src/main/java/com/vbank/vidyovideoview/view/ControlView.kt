package com.vbank.vidyovideoview.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.vbank.vidyovideoview.R
import com.vbank.vidyovideoview.event.ControlEvent
import com.vbank.vidyovideoview.event.IControlLink
import kotlinx.android.synthetic.main.call_toolbar.view.*


class ControlView : LinearLayout, View.OnClickListener {

    private var callback: IControlLink? = null

    private var connectView: ImageView? = null
    private var muteCamera: ImageView? = null
    private var muteMic: ImageView? = null
    private var muteSpeaker: ImageView? = null
    private var switchCamera: ImageView? = null
    private var pipMode: ImageView? = null

    //  private View controlMoreLayout;
    //  private ImageView debugOption;

    //  private View controlMoreLayout;
//  private ImageView debugOption;
    //  private var libraryVersion: TextView? = null
    // private var connectionState: TextView? = null

    private var internalState: State? = null

    constructor(context: Context) : this(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun registerListener(callback: IControlLink?) {
        this.callback = callback
    }

    fun unregisterListener() {
        callback = null
    }

    fun getState(): State? {
        return internalState
    }

    fun showVersion(version: String?) {
        //   if (libraryVersion == null) return
        //   libraryVersion?.text = context.getString(R.string.lib_version, version)
    }

    fun disable(state: Boolean) {
        pipMode?.setOnClickListener(if (state) null else this)
        connectView?.setOnClickListener(if (state) null else this)
        muteCamera?.setOnClickListener(if (state) null else this)
        muteMic?.setOnClickListener(if (state) null else this)
        muteSpeaker?.setOnClickListener(if (state) null else this)
        switchCamera?.setOnClickListener(if (state) null else this)
        //  debugOption.setOnClickListener(state ? null : this);
        //  findViewById(R.id.more_control).setOnClickListener(state ? null : this);
        // findViewById(R.id.more_send_logs).setOnClickListener(state ? null : this);
        alpha = if (state) 0.3f else 1f
    }

     fun pipModeOrFullScreen(isPipMode: Boolean) {
         val layoutPaddingBottom = resources.getDimensionPixelOffset(R.dimen.dp_24)
         val iconSize: Int
         val iconPaddingSize : Int
         val iconPaddingStart : Int

         val layoutParams = lnr_control.layoutParams as LayoutParams
        if (isPipMode) {
            iconSize = resources.getDimensionPixelOffset(R.dimen.dp_24)
            iconPaddingSize = resources.getDimensionPixelOffset(R.dimen.dp_4)
            iconPaddingStart = resources.getDimensionPixelOffset(R.dimen.dp_8)
            lnr_control.orientation = LinearLayout.VERTICAL
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            layoutParams.height = LayoutParams.MATCH_PARENT
            layoutParams.width = LayoutParams.WRAP_CONTENT
            lnr_control.setPadding(0, 0, 0, 0)
            pip_control?.visibility = View.GONE
        } else {
            iconSize = resources.getDimensionPixelOffset(R.dimen.dp_50)
            iconPaddingSize = resources.getDimensionPixelOffset(R.dimen.dp_16)
            iconPaddingStart = resources.getDimensionPixelOffset(R.dimen.dp_16)

            lnr_control.orientation = LinearLayout.HORIZONTAL
            layoutParams.gravity = Gravity.CENTER
            layoutParams.height = LayoutParams.WRAP_CONTENT
            layoutParams.width = LayoutParams.MATCH_PARENT
            pip_control?.visibility = View.VISIBLE
            lnr_control.setPadding(0, 0, 0, layoutPaddingBottom)
        }
        setMargins(camera_control,iconPaddingStart,iconPaddingSize,iconSize)
        setMargins(mic_control,iconPaddingStart,iconPaddingSize,iconSize)
        setMargins(call_control,iconPaddingStart,iconPaddingSize,iconSize)
        setMargins(pip_control,iconPaddingStart,iconPaddingSize,iconSize)
    }



    private fun setMargins(
        view: View,startPadding : Int,
    padding : Int,iconSize: Int) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(startPadding, padding, padding, padding)
            view.requestLayout()
        }
        view.layoutParams?.width = iconSize
        view.layoutParams?.height = iconSize
        view.requestLayout()
    }
    fun connectedCall(connected: Boolean) {
        internalState?.isConnected = connected
        invalidateState()
    }

    fun updateConnectionState(state: ConnectionState) {
        //connectionState?.text = state.name
    }

    private fun invalidateState() {
        connectView?.setImageResource(if (internalState?.isConnected == true) R.drawable.call_disconnect_enable else R.drawable.call_disconnect_disable)
        muteCamera?.setImageResource(if (internalState?.isMuteCamera == true) R.drawable.video_off else R.drawable.video_on)
        muteMic?.setImageResource(if (internalState?.isMuteMic == true) R.drawable.mic_off else R.drawable.mic_on)
        // muteSpeaker?.setImageResource(if (internalState?.isMuteSpeaker == true) R.drawable.speaker_off else R.drawable.speaker_on)
        // debugOption.setImageResource(internalState.isDebug() ? R.drawable.ic_debug_on : R.drawable.ic_debug_off);
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.call_toolbar, this, true)
        connectView = findViewById(R.id.call_control)
        muteCamera = findViewById(R.id.camera_control)
        muteMic = findViewById(R.id.mic_control)
        muteSpeaker = findViewById(R.id.speaker_control)
        switchCamera = findViewById(R.id.switch_control)
        pipMode = findViewById(R.id.pip_control)

        //     controlMoreLayout = findViewById(R.id.control_settings_layout);
//     debugOption = findViewById(R.id.more_debug);
//        libraryVersion = findViewById(R.id.library_version)
//        connectionState = findViewById(R.id.connection_state)
        internalState = State.defaultState()
        invalidateState()
        updateConnectionState(ConnectionState.DISCONNECTED)
        disable(false)
    }

    override fun onClick(v: View) {
        if (internalState == null) return
        var controlEvent: ControlEvent<Any>? = null
        when (v.id) {
            R.id.pip_control -> controlEvent = ControlEvent(ControlEvent.Call.PIP_CONTROL)
            R.id.call_control -> controlEvent =
                ControlEvent(
                    ControlEvent.Call.CONNECT_DISCONNECT,
                    !(internalState?.isConnected ?: false)
                )
            R.id.camera_control -> {
                val muteCamera: Boolean = !(internalState?.isMuteCamera ?: false)
                controlEvent = ControlEvent(ControlEvent.Call.MUTE_CAMERA, muteCamera)
                internalState?.isMuteCamera = muteCamera
                invalidateState()
            }
            R.id.mic_control -> {
                val muteMic: Boolean = !(internalState?.isMuteMic ?: false)
                controlEvent = ControlEvent(ControlEvent.Call.MUTE_MIC, muteMic)
                internalState?.isMuteMic = muteMic
                invalidateState()
            }
            R.id.speaker_control -> {
                val muteSpeaker: Boolean = !(internalState?.isMuteSpeaker ?: false)
                controlEvent = ControlEvent(ControlEvent.Call.MUTE_SPEAKER, muteSpeaker)
                internalState?.isMuteSpeaker = muteSpeaker
                invalidateState()
            }
            R.id.switch_control -> {
                controlEvent = ControlEvent(ControlEvent.Call.CYCLE_CAMERA)

            }
        }
        if (controlEvent != null && callback != null) {
            callback?.onControlEvent(controlEvent)
        }
    }

    class State private constructor(
        var isConnected: Boolean,
        var isMuteCamera: Boolean,
        var isMuteMic: Boolean,
        var isMuteSpeaker: Boolean
    ) {
        var isDebug = false

        companion object {
            fun defaultState(): State {
                return State(
                    isConnected = false,
                    isMuteCamera = false,
                    isMuteMic = false,
                    isMuteSpeaker = false
                )
            }
        }

    }

    enum class ConnectionState {
        CONNECTED, CONNECTING, DISCONNECTING, FAILED, DISCONNECTED
    }

}