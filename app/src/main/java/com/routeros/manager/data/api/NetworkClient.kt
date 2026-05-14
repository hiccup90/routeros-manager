package com.routeros.manager.data.api

import com.routeros.manager.data.preferences.SecurePreferences
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor(
    private val securePreferences: SecurePreferences
) {
    data class EndpointConfig(
        val scheme: String,
        val host: String
    ) {
        val normalizedInput: String
            get() = "$scheme://$host"
    }

    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var currentConfig: String = ""

    fun getApi(): RouterOSApi {
        val endpoint = normalizeEndpointInput(securePreferences.host)
        val currentPort = securePreferences.port
        val username = securePreferences.username
        val password = securePreferences.password

        val configKey = "${endpoint.scheme}://${endpoint.host}:$currentPort:$username"
        if (retrofit == null || currentConfig != configKey) {
            currentConfig = configKey
            rebuild(endpoint, currentPort, username, password)
        }

        return retrofit!!.create(RouterOSApi::class.java)
    }

    fun createApi(host: String, port: Int, username: String, password: String): RouterOSApi {
        val endpoint = normalizeEndpointInput(host)
        return buildRetrofit(endpoint, port, username, password).create(RouterOSApi::class.java)
    }

    private fun rebuild(endpoint: EndpointConfig, port: Int, username: String, password: String) {
        retrofit = buildRetrofit(endpoint, port, username, password)
    }

    private fun buildRetrofit(
        endpoint: EndpointConfig,
        port: Int,
        username: String,
        password: String
    ): Retrofit {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (username.isNotEmpty()) {
            val credentials = Credentials.basic(username, password)
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", credentials)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()

        return Retrofit.Builder()
            .baseUrl("${endpoint.scheme}://${endpoint.host}:$port/rest/")
            .client(okHttpClient!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun updateConfig(host: String, port: Int, username: String, password: String) {
        val endpoint = normalizeEndpointInput(host)
        securePreferences.host = endpoint.normalizedInput
        securePreferences.port = port
        securePreferences.username = username
        securePreferences.password = password
        rebuild(endpoint, port, username, password)
        currentConfig = "${endpoint.scheme}://${endpoint.host}:$port:$username"
    }

    fun normalizeEndpointInput(value: String): EndpointConfig {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return EndpointConfig(scheme = "https", host = "")
        }

        val candidate = if ("://" in trimmed) trimmed else "https://$trimmed"
        return runCatching {
            val uri = URI(candidate)
            val scheme = "https"
            val host = uri.host?.trim().orEmpty().ifBlank {
                trimmed.substringAfter("://", trimmed)
                    .substringBefore('/')
                    .substringBefore('?')
                    .substringBefore('#')
                    .substringBefore(':')
                    .trim()
            }
            EndpointConfig(scheme = scheme, host = host)
        }.getOrElse {
            EndpointConfig(
                scheme = "https",
                host = trimmed.substringAfter("://", trimmed)
                    .substringBefore('/')
                    .substringBefore('?')
                    .substringBefore('#')
                    .substringBefore(':')
                    .trim()
            )
        }
    }
}
