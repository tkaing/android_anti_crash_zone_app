package com.example.drone.data.storage

import com.example.drone.data.api.model.User

class UserStorage {

    companion object {
        var defaultUser = User(-1, "", "", "")
        var loggedUser = defaultUser
        fun isLoggedIn() : Boolean {
            return loggedUser.id != -1
        }
    }
}