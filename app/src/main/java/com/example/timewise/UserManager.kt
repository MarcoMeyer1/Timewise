package com.example.timewise

object UserManager {
    private val users = mutableListOf<User>()

    fun addUser(user: User) {
        users.add(user)
    }

    fun findUser(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }
    }