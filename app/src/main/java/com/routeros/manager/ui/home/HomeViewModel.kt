package com.routeros.manager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.InterfaceItem
import com.routeros.manager.data.api.IpAddress
import com.routeros.manager.data.preferences.SecurePreferences
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val HOME_REFRESH_MS = 5000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RouterOSRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    data class UiState(
        val routerName: String = "---",
        val cpuLoad: String = "--",
        val memoryUsed: Long = 0L,
        val memoryTotal: Long = 0L,
        val memoryPercent: Int = 0,
        val uptime: String = "---",
        val version: String = "---",
        val boardName: String = "---",
        val interfaces: List<InterfaceUiModel> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val isConnected: Boolean = false
    )

    data class InterfaceUiModel(
        val id: String,
        val name: String,
        val type: String,
        val disabled: Boolean,
        val ipv4Address: String,
        val rxRate: String,
        val txRate: String,
        val rxBytes: Long,
        val txBytes: Long
    )

    private val _uiState = MutableStateFlow(
        UiState(
            isLoading = repository.isConfigured(),
            error = if (repository.isConfigured()) null else "请先在设置中配置连接"
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var previousInterfaceBytes: MutableMap<String, Pair<Long, Long>> = mutableMapOf()
    private var pollingJob: Job? = null
    private var refreshJob: Job? = null

    init {
        ensurePollingState()
    }

    fun ensurePollingState() {
        if (repository.isConfigured()) {
            if (pollingJob?.isActive != true) {
                startPolling()
            }
        } else {
            stopPolling()
            securePreferences.isConnected = false
            _uiState.update {
                UiState(
                    isLoading = false,
                    error = "请先在设置中配置连接",
                    isConnected = false
                )
            }
        }
    }

    fun refresh() {
        if (!repository.isConfigured()) {
            securePreferences.isConnected = false
            _uiState.update { it.copy(isLoading = false, error = "请先在设置中配置连接", isConnected = false) }
            return
        }
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val results = withTimeoutOrNull(10_000) {
                coroutineScope {
                    val identityDeferred = async { repository.getSystemIdentity() }
                    val resourceDeferred = async { repository.getSystemResource() }
                    val interfacesDeferred = async { repository.getInterfaces() }
                    val ipAddressesDeferred = async { repository.getIpAddresses() }
                    HomeRefreshResults(
                        identityDeferred.await(),
                        resourceDeferred.await(),
                        interfacesDeferred.await(),
                        ipAddressesDeferred.await()
                    )
                }
            }

            if (results == null) {
                securePreferences.isConnected = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "首页状态获取超时，请检查 RouterOS REST API 是否可用",
                        isConnected = false
                    )
                }
                return@launch
            }

            if (results.resourceResult.isFailure) {
                securePreferences.isConnected = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "连接失败: ${results.resourceResult.exceptionOrNull()?.message ?: "未知错误"}",
                        isConnected = false
                    )
                }
                return@launch
            }

            val identity = results.identityResult.getOrNull()
            val resource = results.resourceResult.getOrNull()
            if (resource == null) {
                securePreferences.isConnected = false
                _uiState.update {
                    it.copy(isLoading = false, error = "获取系统信息失败", isConnected = false)
                }
                return@launch
            }

            val interfaces = if (results.interfacesResult.isSuccess) {
                val allInterfaces = results.interfacesResult.getOrDefault(emptyList())
                val filtered = filterAndOrderHomeInterfaces(allInterfaces)
                val ipByInterface = buildIpAddressMap(results.ipAddressesResult.getOrDefault(emptyList()))
                calculateInterfaceRates(filtered, ipByInterface).take(3)
            } else {
                emptyList()
            }

            val memTotal = (resource.totalMemory.toLongOrNull() ?: 0L).coerceAtLeast(1L)
            val memFree = (resource.freeMemory.toLongOrNull() ?: 0L).coerceAtLeast(0L)
            val memUsed = (memTotal - memFree).coerceAtLeast(0L)
            val memPercent = ((memUsed.toDouble() / memTotal) * 100).toInt().coerceIn(0, 100)

            securePreferences.isConnected = true
            _uiState.update {
                it.copy(
                    routerName = identity?.name?.ifBlank { "RouterOS" } ?: "RouterOS",
                    cpuLoad = resource.cpuLoad.ifBlank { "0" },
                    memoryUsed = memUsed,
                    memoryTotal = memTotal,
                    memoryPercent = memPercent,
                    uptime = resource.uptime.ifBlank { "---" },
                    version = resource.version.ifBlank { "---" },
                    boardName = resource.boardName.ifBlank { "---" },
                    interfaces = interfaces,
                    isLoading = false,
                    error = firstError(listOf(results.interfacesResult, results.ipAddressesResult)),
                    isConnected = true
                )
            }
        }
    }

    private fun filterAndOrderHomeInterfaces(interfaces: List<InterfaceItem>): List<InterfaceItem> {
        val order = securePreferences.homeInterfaceOrder
        val selected = if (order.isNotEmpty()) order else securePreferences.homeInterfaceNames.toList()
        if (selected.isEmpty()) {
            return interfaces.sortedBy { it.name }.take(3)
        }
        val byName = interfaces.associateBy { it.name }
        return selected.mapNotNull(byName::get)
    }

    private fun buildIpAddressMap(addresses: List<IpAddress>): Map<String, String> {
        return addresses
            .filter { it.disabled != "true" }
            .groupBy { it.interface_ }
            .mapValues { (_, items) ->
                items.firstOrNull { it.dynamic != "true" }?.address?.substringBefore("/")
                    ?: items.firstOrNull()?.address?.substringBefore("/")
                    ?: "--"
            }
    }

    private fun calculateInterfaceRates(
        interfaces: List<InterfaceItem>,
        ipByInterface: Map<String, String>
    ): List<InterfaceUiModel> {
        return interfaces.map { iface ->
            val prev = previousInterfaceBytes[iface.name]
            val rxCurrent = iface.rxByte.toLongOrNull() ?: 0L
            val txCurrent = iface.txByte.toLongOrNull() ?: 0L
            val rxDiff = if (prev != null) (rxCurrent - prev.first).coerceAtLeast(0L) else 0L
            val txDiff = if (prev != null) (txCurrent - prev.second).coerceAtLeast(0L) else 0L

            previousInterfaceBytes[iface.name] = rxCurrent to txCurrent

            InterfaceUiModel(
                id = iface.id,
                name = iface.name,
                type = iface.type,
                disabled = iface.disabled == "true",
                ipv4Address = ipByInterface[iface.name] ?: "--",
                rxRate = formatRate(rxDiff / 3),
                txRate = formatRate(txDiff / 3),
                rxBytes = rxCurrent,
                txBytes = txCurrent
            )
        }
    }

    private fun firstError(results: List<Result<*>>): String? {
        return results.firstNotNullOfOrNull { it.exceptionOrNull()?.message }
    }

    private fun formatRate(bytesPerSec: Long): String {
        return when {
            bytesPerSec <= 0L -> "0 B/s"
            bytesPerSec < 1024 -> "$bytesPerSec B/s"
            bytesPerSec < 1024 * 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024.0)
            bytesPerSec < 1024 * 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSec / (1024.0 * 1024))
            else -> String.format("%.2f GB/s", bytesPerSec / (1024.0 * 1024 * 1024))
        }
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            refresh()
            while (isActive) {
                delay(HOME_REFRESH_MS)
                refresh()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        stopPolling()
    }
}

private data class HomeRefreshResults(
    val identityResult: Result<com.routeros.manager.data.api.SystemIdentity>,
    val resourceResult: Result<com.routeros.manager.data.api.SystemResource>,
    val interfacesResult: Result<List<InterfaceItem>>,
    val ipAddressesResult: Result<List<IpAddress>>
)
