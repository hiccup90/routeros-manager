package com.routeros.manager.data.repository

import com.routeros.manager.data.api.DhcpLeaseMakeStaticRequest
import com.routeros.manager.data.api.NetworkClient
import com.routeros.manager.data.api.RouterOSApi
import com.routeros.manager.data.preferences.SecurePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RouterOSRepositoryDhcpLeaseTest {
    private val api = mockk<RouterOSApi>(relaxed = true)
    private val networkClient = mockk<NetworkClient>()
    private val securePreferences = mockk<SecurePreferences>(relaxed = true)

    private val repository = RouterOSRepository(networkClient, securePreferences)

    @Test
    fun `makeDhcpLeaseStatic sends make-static request with lease id`() = runTest {
        every { networkClient.getApi() } returns api
        coEvery { api.makeDhcpLeaseStatic(any()) } returns Unit

        repository.makeDhcpLeaseStatic("*86")

        coVerify(exactly = 1) {
            api.makeDhcpLeaseStatic(DhcpLeaseMakeStaticRequest(numbers = "*86"))
        }
    }

    @Test
    fun `editDhcpLease sends patch updates to lease endpoint`() = runTest {
        val updates = mapOf(
            "comment" to "printer",
            "address" to "192.168.88.20",
            "server" to "dhcp1"
        )
        every { networkClient.getApi() } returns api
        coEvery { api.editDhcpLease(any(), any()) } returns listOf(emptyMap())

        repository.editDhcpLease("*85", updates)

        coVerify(exactly = 1) {
            api.editDhcpLease("*85", updates)
        }
    }
}
