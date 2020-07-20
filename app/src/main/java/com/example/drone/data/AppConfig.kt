package com.example.drone.data

class AppConfig {

    companion object {
        const val API_URL : String = "http://project.thierrykg.xyz/api/drone"
        const val MQTT_TCP : String = "tcp://192.168.43.105:1883" // Dorian
        //const val MQTT_TCP : String = "tcp://192.168.43.105:1883" // Ken
        //const val MQTT_TCP : String = "tcp://10.33.0.147:1883" // ESGI
        const val MQTT_TOPIC: String = "drone_iot/devices/data"
    }
}