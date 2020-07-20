package com.example.drone.data.manager

import com.example.drone.data.api.model.User
import com.example.drone.data.storage.UserStorage

class UserManager {

    companion object {
        fun login(user: User) {
            UserStorage.loggedUser = user
        }
        fun logout() {
            UserStorage.loggedUser = UserStorage.defaultUser
        }
    }
}