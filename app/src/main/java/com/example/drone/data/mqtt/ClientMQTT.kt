package com.example.drone.data.mqtt

import android.widget.Toast
import android.content.Context
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.android.service.MqttAndroidClient
import com.example.drone.data.AppConfig.Companion.MQTT_TCP
import com.example.drone.data.AppConfig.Companion.MQTT_TOPIC
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions


class ClientMQTT(context: Context) {

    var mqttAndroidClient: MqttAndroidClient? = null

    companion object {
        val dataTopic = MQTT_TOPIC
    }

    init {
        //val serverUri = "tcp://test.mosquitto.org:1883"
        val serverUri = MQTT_TCP
        val clientId = "5b232e9712aa4ae1b200fa3f5535f42a"
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)

        // <ApplicationID>/devices/<DeviceID>/up
        val mqttConnectOptions = MqttConnectOptions()

        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.isAutomaticReconnect = true
        mqttAndroidClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Toast.makeText(context, "MQTT Client connecté!", Toast.LENGTH_LONG).show()
                val disconnectedBufferOptions =
                    DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
                mqttAndroidClient!!.subscribe(
                    dataTopic,
                    0,
                    null,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Toast.makeText(context, "MQTT Subscription connecté!", Toast.LENGTH_LONG).show()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Toast.makeText(context, "MQTT Subscription échoué!", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(context, "MQTT Client échoué!", Toast.LENGTH_LONG).show()
            }
        })
    }
}