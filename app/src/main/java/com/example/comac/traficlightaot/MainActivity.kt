package com.example.comac.traficlightaot

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

class MainActivity : Activity(), TLfsm.TLfsmCb {
    lateinit var fsm : TLfsm
    lateinit var pedestrianTL : TrafficLight
    lateinit var carsTL : TrafficLight
    lateinit var gpioButton : Gpio
    lateinit var gpioSensor : Gpio

    lateinit var pedestrianColour : TextView
    lateinit var carsColour : TextView
    lateinit var logging : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pedestrianColour = findViewById(R.id.pedestrian_colour)
        carsColour = findViewById(R.id.car_colour)
        logging = findViewById(R.id.logging)

        pedestrianTL = TrafficLight("BCM3", "BCM4", "BCM2")
        carsTL = TrafficLight("BCM27", "BCM22", "BCM17")

        fsm = TLfsm(TLTransitions.values(), this)

        gpioButton = openButton("BCM14", TLEvents.BUTTON_PRESSED)
        gpioSensor = openButton("BCM15", TLEvents.PRESSURE_SENSOR_TRIGGERED)
    }

    override fun onDestroy() {
        super.onDestroy()
        gpioButton.close()
        gpioSensor.close()
        pedestrianTL.close()
        carsTL.close()
    }

    private fun openButton(pinName: String, event: TLEvents) : Gpio {
        val button = PeripheralManager.getInstance().openGpio(pinName)
        button.setDirection(Gpio.DIRECTION_IN)
        button.setEdgeTriggerType(Gpio.EDGE_RISING)
        button.registerGpioCallback {
            fsm.processEvent(event)
            true
        }
        return button
    }

    override fun transition(source: TLState, event: TLEvents, destination: TLState) {
        pedestrianTL.setColour(destination.pedestrianColour)
        carsTL.setColour(destination.carsColour)

        logging.text = "${source.name} --( ${event.name} )--> ${destination.name}\n${logging.text}"
        pedestrianColour.text = destination.pedestrianColour.name
        carsColour.text = destination.carsColour.name
    }
}
