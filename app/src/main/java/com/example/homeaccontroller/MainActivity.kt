package com.example.homeaccontroller

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.homeaccontroller.R
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : AppCompatActivity() {

    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var temperatureTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var coolingModeButton: Button
    private lateinit var heatingModeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperatureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        coolingModeButton = findViewById(R.id.coolingModeButton)
        heatingModeButton = findViewById(R.id.heatingModeButton)

        coolingModeButton.visibility = View.GONE
        heatingModeButton.visibility = View.GONE

        val clientId = MqttClient.generateClientId()
        mqttAndroidClient = MqttAndroidClient(this.applicationContext, "tcp://broker.hivemq.com:1883", clientId)

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Connected successfully")
                    subscribeToTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Connection failed: ${exception?.message}")
                }
            })

            mqttAndroidClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Connection lost: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val messageString = String(message?.payload ?: ByteArray(0))
                    Log.d("MQTT", "Message arrived on topic $topic: $messageString")
                    when (topic) {
                        "TemperatureAta" -> handleTemperatureMessage(messageString)
                        "HumidityAta" -> handleHumidityMessage(messageString)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "Delivery complete")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToTopics() {
        val topics = arrayOf("TemperatureAta", "HumidityAta")
        val qos = intArrayOf(1, 1)
        try {
            mqttAndroidClient.subscribe(topics, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Subscribed successfully to topics")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Subscription failed: ${exception?.message}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleTemperatureMessage(temperatureMessage: String) {
        temperatureTextView.text = "Room Temperature: $temperatureMessage°C"
        val temperature = temperatureMessage.toDoubleOrNull()
        temperature?.let {
            if (it > 30) {
                coolingModeButton.visibility = View.VISIBLE
                heatingModeButton.visibility = View.GONE
            } else if (it < 10) {
                heatingModeButton.visibility = View.VISIBLE
                coolingModeButton.visibility = View.GONE
            } else {
                coolingModeButton.visibility = View.GONE
                heatingModeButton.visibility = View.GONE
            }
        }
    }

    private fun handleHumidityMessage(humidityMessage: String) {
        humidityTextView.text = "Room Humidity: $humidityMessage%"
    }
}
