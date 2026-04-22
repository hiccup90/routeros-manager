package com.routeros.manager.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.InterfaceItem
import com.routeros.manager.data.api.NetworkDevice
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TerminalDeviceUiModel(
    val key: String,
    val displayName: String,
    val primaryAddress: String,
    val macAddress: String,
    val ipv6Display: String,
    val interfaceDisplay: String,
    val rxRate: String,
    val txRate: String,
    val status: String,
    val expires: String,
    val lastSeen: String,
    val sources: List<String>,
    val hostname: String,
    val inferredName: String,
    val comment: String
)

private data class TerminalContentState(
    val devices: List<TerminalDeviceUiModel> = emptyList(),
    val lastUpdatedAt: Long? = null
)

data class TerminalUiState(
    val devices: List<TerminalDeviceUiModel> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isConfigured: Boolean = true,
    val lastUpdatedAt: Long? = null
)

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    private val contentState = MutableStateFlow(TerminalContentState())
    private val _uiState = MutableStateFlow(
        TerminalUiState(isLoading = repository.isConfigured(), isConfigured = repository.isConfigured())
    )
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var previousInterfaceBytes: Map<String, Pair<Long, Long>> = emptyMap()
    private var pollingJob: Job? = null

    init {
        if (repository.isConfigured()) {
            startPolling()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isConfigured = false,
                    error = "请先在设置中配置 RouterOS 连接"
                )
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                query = query,
                devices = filterDevices(contentState.value.devices, query)
            )
        }
    }

    fun refresh() {
        loadDevices(forceRefresh = true)
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            loadDevices()
            while (isActive) {
                delay(3000)
                loadDevices(forceRefresh = true)
            }
        }
    }

    private fun loadDevices(forceRefresh: Boolean = false) {
        if (!repository.isConfigured()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isConfigured = false,
                    error = "请先在设置中配置 RouterOS 连接"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = contentState.value.devices.isEmpty() && !forceRefresh,
                    isRefreshing = forceRefresh || contentState.value.devices.isNotEmpty(),
                    isConfigured = true,
                    error = null
                )
            }

            val devicesResult = repository.getNetworkDevices()
            val interfacesResult = repository.getInterfaces()

            if (devicesResult.isFailure) {
                _uiState.update { current ->
                    current.copy(
                        devices = filterDevices(contentState.value.devices, current.query),
                        isLoading = false,
                        isRefreshing = false,
                        error = devicesResult.exceptionOrNull()?.message ?: "加载设备失败"
                    )
                }
                return@launch
            }

            val devices = devicesResult.getOrDefault(emptyList())
            val interfaceRates = buildInterfaceRateMap(interfacesResult.getOrDefault(emptyList()))
            val uiModels = devices.map { device -> device.toUiModel(interfaceRates[device.interface_]) }
            val updatedAt = System.currentTimeMillis()

            contentState.value = TerminalContentState(
                devices = uiModels,
                lastUpdatedAt = updatedAt
            )
            _uiState.update { current ->
                current.copy(
                    devices = filterDevices(uiModels, current.query),
                    isLoading = false,
                    isRefreshing = false,
                    isConfigured = true,
                    error = interfacesResult.exceptionOrNull()?.message,
                    lastUpdatedAt = updatedAt
                )
            }
        }
    }

    private fun buildInterfaceRateMap(interfaces: List<InterfaceItem>): Map<String, InterfaceRate> {
        return interfaces.associate { iface ->
            val rxCurrent = iface.rxByte.toLongOrNull() ?: 0L
            val txCurrent = iface.txByte.toLongOrNull() ?: 0L
            val previous = previousInterfaceBytes[iface.name]
            val rxDiff = if (previous != null) (rxCurrent - previous.first).coerceAtLeast(0L) else 0L
            val txDiff = if (previous != null) (txCurrent - previous.second).coerceAtLeast(0L) else 0L
            previousInterfaceBytes = previousInterfaceBytes + (iface.name to (rxCurrent to txCurrent))
            iface.name to InterfaceRate(
                rxRate = formatRate(rxDiff / 3),
                txRate = formatRate(txDiff / 3)
            )
        }
    }

    private fun filterDevices(devices: List<TerminalDeviceUiModel>, query: String): List<TerminalDeviceUiModel> {
        val keyword = query.trim().lowercase()
        if (keyword.isEmpty()) return devices
        return devices.filter { device ->
            buildList {
                add(device.displayName)
                add(device.primaryAddress)
                add(device.macAddress)
                add(device.ipv6Display)
                add(device.interfaceDisplay)
                add(device.status)
                add(device.hostname)
                add(device.inferredName)
                add(device.comment)
                addAll(device.sources)
            }.any { candidate -> candidate.lowercase().contains(keyword) }
        }
    }

    private fun NetworkDevice.toUiModel(rate: InterfaceRate?): TerminalDeviceUiModel {
        val ipv6Value = ipv6Addresses.firstOrNull().orEmpty().ifBlank { "--" }
        val interfaceValue = buildString {
            append(interface_.ifBlank { "--" })
            if (interfaceType.isNotBlank()) append(" · $interfaceType")
        }
        val resolvedDisplayName = displayName.ifBlank {
            hostname.ifBlank {
                inferredName.ifBlank {
                    primaryAddress.ifBlank { "未知设备" }
                }
            }
        }
        return TerminalDeviceUiModel(
            key = key,
            displayName = resolvedDisplayName,
            primaryAddress = primaryAddress.ifBlank { "--" },
            macAddress = macAddress.ifBlank { "--" },
            ipv6Display = ipv6Value,
            interfaceDisplay = interfaceValue,
            rxRate = rate?.rxRate ?: "--",
            txRate = rate?.txRate ?: "--",
            status = status.ifBlank { "未知" },
            expires = expires,
            lastSeen = lastSeen,
            sources = sources,
            hostname = hostname,
            inferredName = inferredName,
            comment = comment
        )
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

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}

private data class InterfaceRate(
    val rxRate: String,
    val txRate: String
)
