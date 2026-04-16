package com.example.appsuivitension.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class User(
    val id: String,
    val login: String,
    val passwordHash: String
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val usersKey = "registered_users"
    private val activeUserKey = "active_user_id"

    fun register(login: String, passwordHash: String): Boolean {
        val users = getRegisteredUsers().toMutableList()
        if (users.any { it.login == login }) return false
        
        val newUser = User(id = UUID.randomUUID().toString(), login = login, passwordHash = passwordHash)
        users.add(newUser)
        saveUsers(users)
        return true
    }

    fun login(login: String, passwordHash: String): User? {
        val user = getRegisteredUsers().find { it.login == login && it.passwordHash == passwordHash }
        if (user != null) {
            prefs.edit().putString(activeUserKey, user.id).apply()
        }
        return user
    }

    fun logout() {
        prefs.edit().remove(activeUserKey).apply()
    }

    fun getActiveUserId(): String? {
        return prefs.getString(activeUserKey, null)
    }

    fun getActiveUserLogin(): String? {
        val activeId = getActiveUserId() ?: return null
        return getRegisteredUsers().find { it.id == activeId }?.login
    }

    private fun getRegisteredUsers(): List<User> {
        val json = prefs.getString(usersKey, "[]")
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveUsers(users: List<User>) {
        val json = gson.toJson(users)
        prefs.edit().putString(usersKey, json).apply()
    }
}
