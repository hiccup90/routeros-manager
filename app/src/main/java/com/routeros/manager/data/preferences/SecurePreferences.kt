package com.routeros.manager.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "routeros_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_HOST = "router_host"
        private const val KEY_PORT = "router_port"
        private const val KEY_USERNAME = "router_username"
        private const val KEY_PASSWORD = "router_password"
        private const val KEY_DISPLAY_MODE = "display_mode"
        private const val KEY_CONNECTED = "is_connected"
        private const val KEY_LAST_CONNECTED = "last_connected"
        private const val KEY_HOME_INTERFACES = "home_interfaces"
    }

    var host: String
        get() = sharedPreferences.getString(KEY_HOST, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_HOST, value).apply()

    var port: Int
        get() = sharedPreferences.getInt(KEY_PORT, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_PORT, value).apply()

    var username: String
        get() = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_USERNAME, value).apply()

    var password: String
        get() = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_PASSWORD, value).apply()

    var displayMode: String
        get() = sharedPreferences.getString(KEY_DISPLAY_MODE, "all") ?: "all"
        set(value) = sharedPreferences.edit().putString(KEY_DISPLAY_MODE, value).apply()

    var isConnected: Boolean
        get() = sharedPreferences.getBoolean(KEY_CONNECTED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_CONNECTED, value).apply()

    var lastConnected: Long
        get() = sharedPreferences.getLong(KEY_LAST_CONNECTED, 0L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_CONNECTED, value).apply()

    var homeInterfaceNames: Set<String>
        get() = sharedPreferences.getStringSet(KEY_HOME_INTERFACES, emptySet()) ?: emptySet()
        set(value) = sharedPreferences.edit().putStringSet(KEY_HOME_INTERFACES, value).apply()

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    fun hasCredentials(): Boolean {
        return host.isNotEmpty() && username.isNotEmpty()
    }
}
