package com.routeros.manager.data.repository

import com.routeros.manager.data.api.NetworkClient
import com.routeros.manager.data.api.RouterOSApi
import com.routeros.manager.data.preferences.SecurePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RouterOSRepositoryDhcpNetworkTest {
    private val api = mockk<RouterOSApi>(relaxed = true)
    private val networkClient = mockk<NetworkClient>()
    private val securePreferences = mockk<SecurePreferences>(relaxed = true)

    private val repository = RouterOSRepository(networkClient, securePreferences)

    @Test
    fun `getDhcpNetworks maps gateway and dns-server fields`() = runTest {
        every { networkClient.getApi() } returns api
        coEvery { api.getDhcpNetworks(any()) } returns listOf(
            mapOf(
                ".id" to "*1",
                "address" to "192.168.88.0/24",
                "gateway" to "192.168.88.1",
                "dns-server" to "192.168.88.1,1.1.1.1",
                "comment" to "lan"
            )
        )

        val result = repository.getDhcpNetworks()

        val item = result.getOrThrow().single()
        assertEquals("*1", item.id)
        assertEquals("192.168.88.0/24", item.address)
        assertEquals("192.168.88.1", item.gateway)
        assertEquals("192.168.88.1,1.1.1.1", item.dnsServer)
        assertEquals("lan", item.comment)
    }

    @Test
    fun `editDhcpNetwork sends gateway and dns-server updates`() = runTest {
        val updates = mapOf(
            "gateway" to "192.168.88.1",
            "dns-server" to "192.168.88.1,1.1.1.1",
            "comment" to "lan"
        )
        every { networkClient.getApi() } returns api
        coEvery { api.editDhcpNetwork(any(), any()) } returns listOf(emptyMap())

        repository.editDhcpNetwork("*1", updates)

        coVerify(exactly = 1) {
            api.editDhcpNetwork("*1", updates)
        }
    }
}
