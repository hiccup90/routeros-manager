package com.routeros.manager.ui.network.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.routeros.manager.data.api.DhcpNetwork
import com.routeros.manager.data.repository.RouterOSRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class DhcpNetworkListViewModelTest {

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
    fun `loadData maps dhcp networks into ui items`() = runTest {
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getDhcpNetworks() } returns Result.success(
            listOf(
                DhcpNetwork(
                    id = "*1",
                    address = "192.168.88.0/24",
                    gateway = "192.168.88.1",
                    dnsServer = "192.168.88.1,1.1.1.1",
                    comment = "main lan"
                )
            )
        )

        val viewModel = DhcpNetworkListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("192.168.88.1", viewModel.uiState.value.items.single().gateway)
        assertEquals("192.168.88.1,1.1.1.1", viewModel.uiState.value.items.single().dnsServer)
    }

    @Test
    fun `saveEdit calls repository and refreshes data`() = runTest {
        val initial = DhcpNetwork(
            id = "*1",
            address = "192.168.88.0/24",
            gateway = "192.168.88.1",
            dnsServer = "192.168.88.1",
            comment = "before"
        )
        val refreshed = initial.copy(dnsServer = "192.168.88.1,1.1.1.1", comment = "after")
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getDhcpNetworks() } returns Result.success(listOf(initial)) andThen Result.success(listOf(refreshed))
        coEvery {
            repository.editDhcpNetwork(
                "*1",
                mapOf(
                    "gateway" to "192.168.88.1",
                    "dns-server" to "192.168.88.1,1.1.1.1",
                    "comment" to "after"
                )
            )
        } returns Result.success(refreshed)

        val viewModel = DhcpNetworkListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveEdit(
            id = "*1",
            gateway = "192.168.88.1",
            dnsServer = "192.168.88.1,1.1.1.1",
            comment = "after"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.editDhcpNetwork(
                "*1",
                mapOf(
                    "gateway" to "192.168.88.1",
                    "dns-server" to "192.168.88.1,1.1.1.1",
                    "comment" to "after"
                )
            )
        }
        assertEquals("after", viewModel.uiState.value.items.single().comment)
        assertEquals(false, viewModel.uiState.value.showEditDialog)
    }
}
