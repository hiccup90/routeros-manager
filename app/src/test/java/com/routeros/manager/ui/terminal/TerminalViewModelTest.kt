package com.routeros.manager.ui.terminal

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.routeros.manager.data.api.FirewallConnection
import com.routeros.manager.data.api.NetworkDevice
import com.routeros.manager.data.repository.RouterOSRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RouterOSRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `expanding device loads traffic with query filtered connections`() = runTest {
        val device = networkDevice()
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getNetworkDevices() } returns Result.success(listOf(device))
        coEvery { repository.getFirewallConnections(any(), any()) } returns Result.success(
            listOf(
                FirewallConnection(
                    srcAddress = "192.168.88.10",
                    dstAddress = "1.1.1.1",
                    origRate = "2048",
                    replRate = "1024"
                )
            )
        )
        coEvery { repository.getIpv6FirewallConnections(any(), any()) } returns Result.success(emptyList())

        val viewModel = TerminalViewModel(repository)
        runCurrent()

        viewModel.setDeviceExpanded(device.key, true)
        runCurrent()

        val uiDevice = viewModel.uiState.value.devices.single()
        assertEquals("1.0 KB/s", uiDevice.downloadRate)
        assertEquals("2.0 KB/s", uiDevice.uploadRate)
        assertFalse(uiDevice.isTrafficLoading)
        assertTrue(uiDevice.trafficLoaded)
        coVerify(atLeast = 1) {
            repository.getFirewallConnections(
                listOf("src-address", "dst-address", "orig-rate", "repl-rate"),
                listOf("src-address=192.168.88.10", "dst-address=192.168.88.10", "#|")
            )
        }
        clearViewModel(viewModel)
    }

    private fun clearViewModel(viewModel: TerminalViewModel) {
        val method = TerminalViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)
    }

    private fun networkDevice() = NetworkDevice(
        key = "dev-1",
        displayName = "phone",
        primaryAddress = "192.168.88.10",
        ipv4Addresses = listOf("192.168.88.10"),
        ipv6Addresses = emptyList(),
        macAddress = "AA:BB:CC:DD:EE:FF",
        hostname = "phone",
        inferredName = "",
        interface_ = "bridge",
        interfaceType = "bridge",
        interfaceComment = "",
        status = "bound",
        expires = "10m",
        lastSeen = "1m",
        comment = "",
        sources = listOf("DHCP")
    )
}
