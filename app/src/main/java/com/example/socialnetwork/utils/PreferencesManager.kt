package com.example.socialnetwork.utils

import android.content.Context

object PreferencesManager {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_TOKEN = "auth_token"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            remove(KEY_TOKEN)
            apply()
        }
    }
}
