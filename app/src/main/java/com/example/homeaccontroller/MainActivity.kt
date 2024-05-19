package com.example.homeaccontroller

import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperatureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)

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
        // Update UI with temperature reading
        temperatureTextView.text = "Room Temperature: $temperatureMessage°C"
    }

    private fun handleHumidityMessage(humidityMessage: String) {
        // Update UI with humidity reading
        humidityTextView.text = "Room Humidity: $humidityMessage%"
    }
}
