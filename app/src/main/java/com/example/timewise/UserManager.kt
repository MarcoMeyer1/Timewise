package com.example.timewise

object UserManager {
    private val users = mutableListOf<User>()

    fun addUser(user: User) {
        users.add(user)
    }

    fun findUser(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }
    fun getUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }
    fun updateUser(email: String, newName: String, newPassword: String) {
        users.find { it.email == email }?.apply {
            name = newName
            password = newPassword
        }
    }

    }