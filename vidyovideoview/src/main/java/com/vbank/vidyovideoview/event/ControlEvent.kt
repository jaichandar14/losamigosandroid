package com.vbank.vidyovideoview.event


class ControlEvent<T> @SafeVarargs constructor(
    call: Call?,
    vararg value: T
) :
    BusBase<T, ControlEvent.Call?>(call, value) {
    enum class Call : CallBase {
        CONNECT_DISCONNECT, MUTE_CAMERA, MUTE_MIC, MUTE_SPEAKER, CYCLE_CAMERA, DEBUG_OPTION, SEND_LOGS, PIP_CONTROL
    }

    override val call: Call?
        get() = super.call
}