package com.example.drone.data.api.factory

import com.android.volley.VolleyError
import org.json.JSONObject
import java.nio.charset.Charset

class VolleyFactory {

    companion object {
        fun getMessage(error: VolleyError) : String {
            val responseBody = String(error.networkResponse.data, Charset.forName("utf-8"))
            val responseJSON = JSONObject(responseBody)
            return responseJSON.getString("message")
        }
    }
}