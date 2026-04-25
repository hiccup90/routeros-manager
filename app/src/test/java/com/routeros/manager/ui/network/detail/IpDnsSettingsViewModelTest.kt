package com.routeros.manager.ui.network.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.routeros.manager.data.api.IpDnsSettings
import com.routeros.manager.data.repository.RouterOSRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IpDnsSettingsViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RouterOSRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.isConfigured() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData formats dns settings for display`() = runTest {
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getIpDnsSettings() } returns Result.success(
            IpDnsSettings(
                servers = "1.1.1.1,8.8.8.8",
                dynamicServers = "192.168.88.1",
                allowRemoteRequests = "true",
                cacheSize = "2048KiB",
                cacheUsed = "256KiB",
                maxConcurrentQueries = "100",
                maxConcurrentTcpSessions = "20"
            )
        )

        val viewModel = IpDnsSettingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("1.1.1.1, 8.8.8.8", state.servers)
        assertEquals("192.168.88.1", state.dynamicServers)
        assertEquals("已开启", state.allowRemoteRequests)
        assertEquals("2.00 MiB", state.cacheSize)
        assertEquals("256.00 KiB", state.cacheUsed)
        assertEquals("100", state.maxConcurrentQueries)
        assertEquals("20", state.maxConcurrentTcpSessions)
    }
}
