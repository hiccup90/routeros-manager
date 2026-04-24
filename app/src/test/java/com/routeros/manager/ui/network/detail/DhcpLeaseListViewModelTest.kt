package com.routeros.manager.ui.network.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.routeros.manager.data.api.DhcpLease
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
class DhcpLeaseListViewModelTest {

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
    fun `makeStatic calls repository and refreshes leases`() = runTest {
        val initialLease = lease(id = "*86", dynamic = true, comment = "before")
        val refreshedLease = lease(id = "*86", dynamic = false, comment = "before")
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getDhcpLeases() } returns Result.success(listOf(initialLease)) andThen Result.success(listOf(refreshedLease))
        coEvery { repository.makeDhcpLeaseStatic("*86") } returns Result.success(Unit)

        val viewModel = DhcpLeaseListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.makeStatic("*86")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.makeDhcpLeaseStatic("*86") }
        coVerify(exactly = 2) { repository.getDhcpLeases() }
        assertEquals(false, viewModel.uiState.value.items.single().isDynamic)
    }

    @Test
    fun `editLease calls repository and updates displayed fields after refresh`() = runTest {
        val initialLease = lease(id = "*86", dynamic = false, comment = "old", address = "192.168.88.10", server = "dhcp1")
        val refreshedLease = lease(id = "*86", dynamic = false, comment = "new", address = "192.168.88.20", server = "dhcp2")
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getDhcpLeases() } returns Result.success(listOf(initialLease)) andThen Result.success(listOf(refreshedLease))
        coEvery {
            repository.editDhcpLease(
                "*86",
                mapOf("comment" to "new", "address" to "192.168.88.20", "server" to "dhcp2")
            )
        } returns Result.success(refreshedLease)

        val viewModel = DhcpLeaseListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.editLease(
            id = "*86",
            comment = "new",
            address = "192.168.88.20",
            server = "dhcp2"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.editDhcpLease(
                "*86",
                mapOf("comment" to "new", "address" to "192.168.88.20", "server" to "dhcp2")
            )
        }
        assertEquals("new", viewModel.uiState.value.items.single().comment)
        assertEquals("192.168.88.20", viewModel.uiState.value.items.single().address)
        assertEquals("dhcp2", viewModel.uiState.value.items.single().server)
    }

    @Test
    fun `saveStaticBindingWizard makes lease static then updates lease and dhcp network`() = runTest {
        val initialLease = lease(id = "*86", dynamic = true, comment = "old", address = "192.168.88.10", server = "dhcp1")
        val refreshedLease = lease(id = "*86", dynamic = false, comment = "printer", address = "192.168.88.20", server = "dhcp2")
        coEvery { repository.isConfigured() } returns true
        coEvery { repository.getDhcpLeases() } returns Result.success(listOf(initialLease)) andThen Result.success(listOf(refreshedLease))
        coEvery { repository.getDhcpNetworks() } returns Result.success(
            listOf(
                com.routeros.manager.data.api.DhcpNetwork(
                    id = "*1",
                    address = "192.168.88.0/24",
                    gateway = "192.168.88.1",
                    dnsServer = "192.168.88.1"
                )
            )
        )
        coEvery { repository.makeDhcpLeaseStatic("*86") } returns Result.success(Unit)
        coEvery {
            repository.editDhcpLease(
                "*86",
                mapOf("comment" to "printer", "address" to "192.168.88.20", "server" to "dhcp2")
            )
        } returns Result.success(refreshedLease)
        coEvery {
            repository.editDhcpNetwork(
                "*1",
                mapOf("gateway" to "192.168.88.1", "dns-server" to "192.168.88.1,1.1.1.1")
            )
        } returns Result.success(
            com.routeros.manager.data.api.DhcpNetwork(
                id = "*1",
                address = "192.168.88.0/24",
                gateway = "192.168.88.1",
                dnsServer = "192.168.88.1,1.1.1.1"
            )
        )

        val viewModel = DhcpLeaseListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.showStaticBindingDialog(viewModel.uiState.value.items.single())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveStaticBinding(
            id = "*86",
            comment = "printer",
            address = "192.168.88.20",
            server = "dhcp2",
            gateway = "192.168.88.1",
            dnsServer = "192.168.88.1,1.1.1.1"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.makeDhcpLeaseStatic("*86") }
        coVerify(exactly = 1) {
            repository.editDhcpLease(
                "*86",
                mapOf("comment" to "printer", "address" to "192.168.88.20", "server" to "dhcp2")
            )
        }
        coVerify(exactly = 1) {
            repository.editDhcpNetwork(
                "*1",
                mapOf("gateway" to "192.168.88.1", "dns-server" to "192.168.88.1,1.1.1.1")
            )
        }
        assertEquals(false, viewModel.uiState.value.items.single().isDynamic)
        assertEquals("printer", viewModel.uiState.value.items.single().comment)
        assertEquals(false, viewModel.uiState.value.showStaticBindingDialog)
    }

    private fun lease(
        id: String,
        dynamic: Boolean,
        comment: String,
        address: String = "192.168.88.10",
        server: String = "dhcp1"
    ) = DhcpLease(
        id = id,
        address = address,
        macAddress = "AA:BB:CC:DD:EE:FF",
        activeHostName = "device",
        hostname = "device",
        status = "bound",
        server = server,
        expires = "10m",
        lastSeen = "1m",
        comment = comment,
        dynamic = if (dynamic) "true" else "false"
    )
}
