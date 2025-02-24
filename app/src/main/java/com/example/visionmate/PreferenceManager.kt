package com.example.visionmate
import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "MyPreferences"
        private const val KEY_BOOLEAN = "myBooleanKey"
    }

    // Method to save a boolean value
    fun saveBoolean(value: Boolean) {
        editor.putBoolean(KEY_BOOLEAN, value)
        editor.apply() // or editor.commit()
    }

    // Method to fetch a boolean value
    fun fetchBoolean(): Boolean {
        return sharedPreferences.getBoolean(KEY_BOOLEAN, true) // false is the default value
    }
}