package com.example.homeaccontroller
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperatureTextView)

        val clientId = MqttClient.generateClientId()
        mqttAndroidClient = MqttAndroidClient(this.applicationContext, "tcp://mqtt.eclipse.org:1883", clientId)

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    // Handle connection failure
                }
            })

            mqttAndroidClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    // Handle connection lost
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // Handle incoming MQTT messages
                    if (topic == "room/temperature") {
                        val temperatureMessage = String(message?.payload ?: ByteArray(0))
                        handleTemperatureMessage(temperatureMessage)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Handle message delivery completion
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToTopic() {
        val topic = "room/temperature"
        val qos = 1
        try {
            mqttAndroidClient.subscribe(topic, qos)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleTemperatureMessage(temperatureMessage: String) {
        // Update UI with temperature reading
        temperatureTextView.text = "Room Temperature: $temperatureMessage"
        // Perform additional logic based on temperature if needed
    }
}
