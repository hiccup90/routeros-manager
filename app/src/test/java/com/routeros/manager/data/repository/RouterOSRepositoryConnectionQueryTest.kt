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
import org.junit.Test

class RouterOSRepositoryConnectionQueryTest {
    private val api = mockk<RouterOSApi>(relaxed = true)
    private val networkClient = mockk<NetworkClient>()
    private val securePreferences = mockk<SecurePreferences>(relaxed = true)

    private val repository = RouterOSRepository(networkClient, securePreferences)

    @Test
    fun `getFirewallConnections sends query and requested proplist`() = runTest {
        val props = listOf("src-address", "dst-address", "orig-rate", "repl-rate")
        val query = listOf("src-address=192.168.88.10", "dst-address=192.168.88.10", "#|")
        every { networkClient.getApi() } returns api
        coEvery { api.getFirewallConnections(any()) } returns emptyList()

        repository.getFirewallConnections(props = props, query = query)

        coVerify(exactly = 1) {
            api.getFirewallConnections(
                PrintRequest(
                    proplist = props,
                    query = query,
                    withoutPaging = ""
                )
            )
        }
    }

    @Test
    fun `getIpv6FirewallConnections sends query and requested proplist`() = runTest {
        val props = listOf("src-address", "dst-address", "orig-rate", "repl-rate")
        val query = listOf("src-address=fe80::1", "dst-address=fe80::1", "#|")
        every { networkClient.getApi() } returns api
        coEvery { api.getIpv6FirewallConnections(any()) } returns emptyList()

        repository.getIpv6FirewallConnections(props = props, query = query)

        coVerify(exactly = 1) {
            api.getIpv6FirewallConnections(
                PrintRequest(
                    proplist = props,
                    query = query,
                    withoutPaging = ""
                )
            )
        }
    }
}
