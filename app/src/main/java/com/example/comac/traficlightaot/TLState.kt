package com.example.comac.traficlightaot

import android.os.Handler

enum class TLColour(val red: Boolean, val green: Boolean, val blue: Boolean) {
    GREEN(false, true, false),
    ORANGE(true, true, false),
    RED(true, false, false),
    BLACK(false, false, false)
}

enum class TLColourAnim(val colour1: TLColour, val interval: Long = 0, val colour2: TLColour = colour1) {
    GREEN(TLColour.GREEN),
    ORANGE(TLColour.ORANGE),
    RED(TLColour.RED),

    FLASH_GREEN(TLColour.GREEN, 500, TLColour.BLACK),
    FLASH_ORANGE(TLColour.ORANGE, 500, TLColour.BLACK),
    FLASH_RED(TLColour.RED, 500, TLColour.BLACK),

    BLACK(TLColour.BLACK)
}

enum class TLState(val pedestrianColour: TLColourAnim, val carsColour: TLColourAnim) {
    INIT(TLColourAnim.BLACK, TLColourAnim.BLACK),
    GR(TLColourAnim.GREEN, TLColourAnim.RED),
    fGR(TLColourAnim.FLASH_GREEN, TLColourAnim.RED),
    RRp2c(TLColourAnim.RED, TLColourAnim.RED),
    RGfixed(TLColourAnim.RED, TLColourAnim.GREEN),
    RGextra(TLColourAnim.RED, TLColourAnim.GREEN),
    RfO(TLColourAnim.RED, TLColourAnim.FLASH_ORANGE),
    RRc2p(TLColourAnim.RED, TLColourAnim.RED),
    fRRp2c(TLColourAnim.FLASH_RED, TLColourAnim.RED),
    fRG(TLColourAnim.FLASH_RED, TLColourAnim.GREEN),
    fRfO(TLColourAnim.FLASH_RED, TLColourAnim.FLASH_ORANGE),
    fRRc2p(TLColourAnim.FLASH_RED, TLColourAnim.RED),
}

enum class TLEvents {
    RESET,
    BUTTON_PRESSED,
    PRESSURE_SENSOR_TRIGGERED,
    TIMEOUT
}

enum class TLTransitions(val source: TLState, val event: TLEvents, val destination: TLState, val timeout: Long) {
    Init_Reset(TLState.INIT, TLEvents.RESET, TLState.RGextra, 0),
    GR_Tout(TLState.GR, TLEvents.TIMEOUT, TLState.fGR, 5000),
    fGR_Tout(TLState.fGR, TLEvents.TIMEOUT, TLState.RRp2c, 2000),
    RRp2c_Tout(TLState.RRp2c, TLEvents.TIMEOUT, TLState.RGfixed, 30000),
    RRp2c_Button(TLState.RRp2c, TLEvents.BUTTON_PRESSED, TLState.fRRp2c, -1),
    RGfixed_Tout(TLState.RGfixed, TLEvents.TIMEOUT, TLState.RGextra, 0),
    RGfixed_Button(TLState.RGfixed, TLEvents.BUTTON_PRESSED, TLState.fRG, -1),
    RGextra_Sensor(TLState.RGextra, TLEvents.PRESSURE_SENSOR_TRIGGERED, TLState.RfO, 5000),
    RGextra_Button(TLState.RGextra, TLEvents.BUTTON_PRESSED, TLState.fRfO, 5000),
    RfO_Tout(TLState.RfO, TLEvents.TIMEOUT, TLState.RRc2p, 2000),
    RRc2p_Tout(TLState.RRc2p, TLEvents.TIMEOUT, TLState.GR, 30000),
    fRR_Tout(TLState.fRRp2c, TLEvents.TIMEOUT, TLState.fRG, 30000),
    fRG_Tout(TLState.fRG, TLEvents.TIMEOUT, TLState.fRfO, 5000),
    fRfO_Tout(TLState.fRfO, TLEvents.TIMEOUT, TLState.fRRc2p, 2000),
    fRRc2p_Tout(TLState.fRRc2p, TLEvents.TIMEOUT, TLState.GR, 30000),
}

class TLfsm(private val transitions: Array<TLTransitions>, private val callbacks: TLfsmCb) {

    private var currentState = TLState.INIT
    private val handler = Handler()

    init {
        processEvent(TLEvents.RESET)
    }

    private fun enterState(transition : TLTransitions) {
        currentState = transition.destination
        if (transition.timeout > -1) {
            handler.removeCallbacksAndMessages(null)
        }
        if (transition.timeout > 0) {
            handler.postDelayed({ processEvent(TLEvents.TIMEOUT)}, transition.timeout)
        }
    }

    fun processEvent(event: TLEvents) {
        transitions.forEach {
            if (it.source.equals(currentState) && it.event.equals(event)) {
                callbacks.transition(currentState, event, it.destination)
                enterState(it)
                return
            }
        }
    }

    interface TLfsmCb {
        fun transition(source : TLState, event : TLEvents, destination : TLState)
    }
}