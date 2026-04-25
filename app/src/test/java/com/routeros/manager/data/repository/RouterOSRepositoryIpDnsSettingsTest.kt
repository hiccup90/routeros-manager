package com.routeros.manager.data.repository

import com.routeros.manager.data.api.NetworkClient
import com.routeros.manager.data.api.PrintRequest
import com.routeros.manager.data.api.RouterOSApi
import com.routeros.manager.data.preferences.SecurePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RouterOSRepositoryIpDnsSettingsTest {
    private val api = mockk<RouterOSApi>(relaxed = true)
    private val networkClient = mockk<NetworkClient>()
    private val securePreferences = mockk<SecurePreferences>(relaxed = true)

    private val repository = RouterOSRepository(networkClient, securePreferences)

    @Test
    fun `getIpDnsSettings sends requested proplist and maps first row`() = runTest {
        val props = listOf("servers", "dynamic-servers", "allow-remote-requests", "cache-size", "cache-used", "max-concurrent-queries")
        every { networkClient.getApi() } returns api
        coEvery { api.getIpDnsSettings(any()) } returns listOf(
            mapOf(
                "servers" to "1.1.1.1,8.8.8.8",
                "dynamic-servers" to "192.168.88.1",
                "allow-remote-requests" to "true",
                "cache-size" to "2048KiB",
                "cache-used" to "256KiB",
                "max-concurrent-queries" to "100"
            )
        )

        val result = repository.getIpDnsSettings(props)

        coVerify(exactly = 1) {
            api.getIpDnsSettings(
                PrintRequest(
                    proplist = props,
                    withoutPaging = ""
                )
            )
        }
        val settings = result.getOrThrow()
        assertEquals("1.1.1.1,8.8.8.8", settings.servers)
        assertEquals("192.168.88.1", settings.dynamicServers)
        assertEquals("true", settings.allowRemoteRequests)
        assertEquals("2048KiB", settings.cacheSize)
        assertEquals("256KiB", settings.cacheUsed)
        assertEquals("100", settings.maxConcurrentQueries)
    }
}
