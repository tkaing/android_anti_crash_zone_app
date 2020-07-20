package com.example.drone.data.api.factory

import com.example.drone.data.api.model.Session
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class SessionFactory {

    companion object {
        fun mapFromJSON(jsonObject: JSONObject) : Session {
            return Session(
                jsonObject.getInt("id"),
                parseDate(jsonObject.getJSONObject("createdAt").getString("date")),
                parseTime(jsonObject.getJSONObject("createdAt").getString("date"))
            )
        }
        fun mapFromJSONArray(jsonArray: JSONArray) : MutableList<Session> {
            val sessionArray: MutableList<Session> = mutableListOf()
            for (x in 0 until jsonArray.length()) {
                val session = mapFromJSON(jsonArray.getJSONObject(x))
                sessionArray.add(x, session)
            }
            return sessionArray
        }
        private fun parseTime(dateString: String) : String
        {
            val parts = dateString.split(" ")
            return parts.last().replace(".000000","")
        }
        private fun parseDate(dateString: String) : String
        {
            val parts = dateString.split(" ")
            val dateString = parts.first()

            var formatter = SimpleDateFormat("yyyy-MM-dd")
            val date : Date = formatter.parse(dateString)

            formatter = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
            return formatter.format(date)
        }
    }
}