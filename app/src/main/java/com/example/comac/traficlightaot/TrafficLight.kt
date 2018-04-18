package com.example.comac.traficlightaot

import android.os.Handler
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

class TrafficLight(pinR: String, pinG: String, pinB: String) {

    private val gpioR : Gpio
    private val gpioG : Gpio
    private val gpioB : Gpio

    private val handler = Handler()

    init {
        gpioR = openPin(pinR)
        gpioG = openPin(pinG)
        gpioB = openPin(pinB)
    }

    private fun openPin(pinName : String) : Gpio {
        val gpio = PeripheralManager.getInstance().openGpio(pinName)
        gpio.setActiveType(Gpio.ACTIVE_LOW)
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH)
        return gpio
    }

    fun setColour(colour: TLColourAnim, firstBlinkCycle : Boolean = true) {
        handler.removeCallbacksAndMessages(null)
        setColour(if (firstBlinkCycle) { colour.colour1 } else { colour.colour2 } )
        if (colour.interval > 0) {
            handler.postDelayed({ setColour(colour, !firstBlinkCycle)}, colour.interval)
        }
    }

    private fun setColour(colour: TLColour) {
        gpioR.value = colour.red
        gpioG.value = colour.green
        gpioB.value = colour.blue
    }

    fun close() {
        handler.removeCallbacksAndMessages(null)
        gpioR.close()
        gpioG.close()
        gpioB.close()
    }

}
