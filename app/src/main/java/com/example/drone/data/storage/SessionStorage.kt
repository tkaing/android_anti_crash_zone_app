package com.example.drone.data.storage

import com.example.drone.data.api.model.Session
import com.example.drone.data.api.model.SessionDetails

class SessionStorage {

    companion object {
        var defaultSession = Session(-1, "", "")
        var currentSession = defaultSession
        fun isActive() : Boolean {
            return currentSession.id != -1
        }
        val detailsArray : MutableList<SessionDetails> = mutableListOf()
    }
}