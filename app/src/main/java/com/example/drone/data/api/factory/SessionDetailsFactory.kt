package com.example.drone.data.api.factory

import com.example.drone.data.api.model.SessionDetails
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailsFactory {

    companion object {
        fun mapFromJSON(jsonObject: JSONObject) : SessionDetails {
            return SessionDetails(
                jsonObject.getInt("id"),
                jsonObject.getDouble("speed"),
                jsonObject.getInt("distance"),
                parseDateString(jsonObject.getJSONObject("createdAt").getString("date"))
            )
        }
        fun mapFromJSONArray(jsonArray: JSONArray) : MutableList<SessionDetails> {
            val detailsArray: MutableList<SessionDetails> = mutableListOf()
            for (x in 0 until jsonArray.length()) {
                val details = mapFromJSON(jsonArray.getJSONObject(x))
                detailsArray.add(x, details)
            }
            return detailsArray
        }
        private fun parseDateString(dateString: String) : String {
            val parts = dateString.split(" ")
            return parts.last().replace(".000000","")
        }

        fun mapFromPayload(payload: JSONObject): SessionDetails {
            return SessionDetails(
                null,
                payload.getDouble("speed"),
                payload.getInt("distance"),
                getFormattedDateNow()
            )
        }
        fun getFormattedDateNow() : String
        {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("HH:mm:ss")

            return dateFormat.format(calendar.time)
        }
    }
}