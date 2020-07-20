package com.example.drone.data.api.factory

import com.example.drone.data.api.model.User
import org.json.JSONObject

class UserFactory {

    companion object {
        fun mapFromJSON(jsonObject: JSONObject) : User {
            return User(
                jsonObject.getInt("id"),
                jsonObject.getString("email"),
                jsonObject.getString("pseudo"),
                jsonObject.getString("password")
            )
        }
    }
}