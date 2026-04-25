package com.routeros.manager.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.FirewallConnection
import com.routeros.manager.data.api.NetworkDevice
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

private const val DEVICE_LIST_REFRESH_MS = 3000L
private const val DEVICE_TRAFFIC_REFRESH_MS = 5000L

data class TerminalDeviceUiModel(
    val key: String,
    val displayName: String,
    val primaryAddress: String,
    val ipv4Addresses: List<String>,
    val ipv6Addresses: List<String>,
    val macAddress: String,
    val ipv6Display: String,
    val interfaceDisplay: String,
    val downloadRate: String,
    val uploadRate: String,
    val isTrafficLoading: Boolean,
    val trafficError: String?,
    val trafficLoaded: Boolean,
    val status: String,
    val expires: String,
    val lastSeen: String,
    val sources: List<String>,
    val hostname: String,
    val inferredName: String,
    val comment: String,
    val isOnline: Boolean
)

private data class TerminalContentState(
    val devices: List<TerminalDeviceUiModel> = emptyList(),
    val lastUpdatedAt: Long? = null
)

data class TerminalUiState(
    val devices: List<TerminalDeviceUiModel> = emptyList(),
    val query: String = "",
    val showOnlineOnly: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isConfigured: Boolean = true,
    val lastUpdatedAt: Long? = null
)

private data class DeviceTrafficSnapshot(
    val downloadBytesPerSecond: Long = 0L,
    val uploadBytesPerSecond: Long = 0L
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

    private var pollingJob: Job? = null
    private val trafficPollingJobs = mutableMapOf<String, Job>()

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
                devices = buildVisibleDevices(
                    devices = contentState.value.devices,
                    query = query,
                    showOnlineOnly = current.showOnlineOnly
                )
            )
        }
    }

    fun setShowOnlineOnly(showOnlineOnly: Boolean) {
        _uiState.update { current ->
            current.copy(
                showOnlineOnly = showOnlineOnly,
                devices = buildVisibleDevices(
                    devices = contentState.value.devices,
                    query = current.query,
                    showOnlineOnly = showOnlineOnly
                )
            )
        }
    }

    fun refresh() {
        loadDevices(forceRefresh = true)
    }

    fun setDeviceExpanded(deviceKey: String, expanded: Boolean) {
        if (expanded) {
            startTrafficPolling(deviceKey)
        } else {
            stopTrafficPolling(deviceKey)
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            loadDevices()
            while (isActive) {
                delay(DEVICE_LIST_REFRESH_MS)
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
            if (devicesResult.isFailure) {
                _uiState.update { current ->
                    current.copy(
                        devices = buildVisibleDevices(
                            devices = contentState.value.devices,
                            query = current.query,
                            showOnlineOnly = current.showOnlineOnly
                        ),
                        isLoading = false,
                        isRefreshing = false,
                        error = devicesResult.exceptionOrNull()?.message ?: "加载设备失败"
                    )
                }
                return@launch
            }

            val previousByKey = contentState.value.devices.associateBy { it.key }
            val uiModels = devicesResult.getOrDefault(emptyList()).map { device ->
                device.toUiModel(previousByKey[device.key])
            }
            val updatedAt = System.currentTimeMillis()

            contentState.value = TerminalContentState(
                devices = uiModels,
                lastUpdatedAt = updatedAt
            )
            _uiState.update { current ->
                current.copy(
                    devices = buildVisibleDevices(
                        devices = uiModels,
                        query = current.query,
                        showOnlineOnly = current.showOnlineOnly
                    ),
                    isLoading = false,
                    isRefreshing = false,
                    isConfigured = true,
                    error = null,
                    lastUpdatedAt = updatedAt
                )
            }
        }
    }

    private fun startTrafficPolling(deviceKey: String) {
        if (trafficPollingJobs[deviceKey]?.isActive == true) return
        trafficPollingJobs[deviceKey] = viewModelScope.launch {
            refreshDeviceTraffic(deviceKey, showLoading = true)
            while (isActive) {
                delay(DEVICE_TRAFFIC_REFRESH_MS)
                refreshDeviceTraffic(deviceKey, showLoading = false)
            }
        }
    }

    private fun stopTrafficPolling(deviceKey: String) {
        trafficPollingJobs.remove(deviceKey)?.cancel()
    }

    private suspend fun refreshDeviceTraffic(deviceKey: String, showLoading: Boolean) {
        val device = contentState.value.devices.firstOrNull { it.key == deviceKey } ?: return
        if (showLoading) {
            updateDevice(deviceKey) {
                it.copy(
                    isTrafficLoading = true,
                    trafficError = null
                )
            }
        }

        val snapshotResult = runCatching { loadTrafficSnapshot(device) }
        snapshotResult.onSuccess { snapshot ->
            updateDevice(deviceKey) {
                it.copy(
                    downloadRate = formatRate(snapshot.downloadBytesPerSecond),
                    uploadRate = formatRate(snapshot.uploadBytesPerSecond),
                    isTrafficLoading = false,
                    trafficError = null,
                    trafficLoaded = true
                )
            }
        }.onFailure { error ->
            updateDevice(deviceKey) {
                it.copy(
                    isTrafficLoading = false,
                    trafficError = error.message ?: "加载设备流量失败"
                )
            }
        }
    }

    private suspend fun loadTrafficSnapshot(device: TerminalDeviceUiModel): DeviceTrafficSnapshot = coroutineScope {
        val ipv4Deferred = async { loadTrafficForAddresses(device.ipv4Addresses, isIpv6 = false) }
        val ipv6Deferred = async { loadTrafficForAddresses(device.ipv6Addresses, isIpv6 = true) }
        val ipv4 = ipv4Deferred.await()
        val ipv6 = ipv6Deferred.await()
        DeviceTrafficSnapshot(
            downloadBytesPerSecond = ipv4.downloadBytesPerSecond + ipv6.downloadBytesPerSecond,
            uploadBytesPerSecond = ipv4.uploadBytesPerSecond + ipv6.uploadBytesPerSecond
        )
    }

    private suspend fun loadTrafficForAddresses(addresses: List<String>, isIpv6: Boolean): DeviceTrafficSnapshot {
        val normalizedAddresses = addresses.map(::normalizeAddress).filter { it.isNotBlank() }.distinct()
        if (normalizedAddresses.isEmpty()) return DeviceTrafficSnapshot()

        val props = listOf("src-address", "dst-address", "orig-rate", "repl-rate")
        val query = buildOrQuery(
            normalizedAddresses.flatMap { address ->
                listOf("src-address=$address", "dst-address=$address")
            }
        )

        val result = if (isIpv6) {
            repository.getIpv6FirewallConnections(props = props, query = query)
        } else {
            repository.getFirewallConnections(props = props, query = query)
        }
        val connections = result.getOrThrow()
        return aggregateTraffic(connections, normalizedAddresses.toSet())
    }

    private fun aggregateTraffic(
        connections: List<FirewallConnection>,
        addresses: Set<String>
    ): DeviceTrafficSnapshot {
        var download = 0L
        var upload = 0L
        connections.forEach { connection ->
            val srcAddress = normalizeAddress(connection.srcAddress)
            val dstAddress = normalizeAddress(connection.dstAddress)
            val origRate = connection.origRate.toLongOrNull() ?: 0L
            val replRate = connection.replRate.toLongOrNull() ?: 0L
            when {
                srcAddress in addresses -> {
                    upload += origRate
                    download += replRate
                }
                dstAddress in addresses -> {
                    upload += replRate
                    download += origRate
                }
            }
        }
        return DeviceTrafficSnapshot(
            downloadBytesPerSecond = download,
            uploadBytesPerSecond = upload
        )
    }

    private fun buildOrQuery(conditions: List<String>): List<String>? {
        if (conditions.isEmpty()) return null
        val query = mutableListOf(conditions.first())
        conditions.drop(1).forEach { condition ->
            query += condition
            query += "#|"
        }
        return query
    }

    private fun updateDevice(
        deviceKey: String,
        transform: (TerminalDeviceUiModel) -> TerminalDeviceUiModel
    ) {
        var changed = false
        val newDevices = contentState.value.devices.map { device ->
            if (device.key != deviceKey) return@map device
            changed = true
            transform(device)
        }
        if (!changed) return
        contentState.value = contentState.value.copy(devices = newDevices)
        _uiState.update { current ->
            current.copy(
                devices = buildVisibleDevices(newDevices, current.query, current.showOnlineOnly)
            )
        }
    }

    private fun buildVisibleDevices(
        devices: List<TerminalDeviceUiModel>,
        query: String,
        showOnlineOnly: Boolean
    ): List<TerminalDeviceUiModel> {
        val keyword = query.trim().lowercase()
        return devices
            .asSequence()
            .filter { device -> !showOnlineOnly || device.isOnline }
            .filter { device ->
                if (keyword.isEmpty()) return@filter true
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
            .sortedWith(
                compareByDescending<TerminalDeviceUiModel> { it.isOnline }
                    .thenBy { it.displayName.lowercase() }
                    .thenBy { it.primaryAddress.lowercase() }
            )
            .toList()
    }

    private fun NetworkDevice.toUiModel(previous: TerminalDeviceUiModel?): TerminalDeviceUiModel {
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
            ipv4Addresses = ipv4Addresses,
            ipv6Addresses = ipv6Addresses,
            macAddress = macAddress.ifBlank { "--" },
            ipv6Display = ipv6Value,
            interfaceDisplay = interfaceValue,
            downloadRate = previous?.downloadRate ?: "--",
            uploadRate = previous?.uploadRate ?: "--",
            isTrafficLoading = previous?.isTrafficLoading ?: false,
            trafficError = previous?.trafficError,
            trafficLoaded = previous?.trafficLoaded ?: false,
            status = status.ifBlank { "未知" },
            expires = expires,
            lastSeen = lastSeen,
            sources = sources,
            hostname = hostname,
            inferredName = inferredName,
            comment = comment,
            isOnline = isOnlineStatus(status)
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

    private fun normalizeAddress(value: String): String = value.substringBefore("/").trim()

    private fun isOnlineStatus(status: String): Boolean {
        return status.contains("bound", ignoreCase = true) ||
            status.contains("complete", ignoreCase = true)
    }

    override fun onCleared() {
        pollingJob?.cancel()
        trafficPollingJobs.values.forEach { it.cancel() }
        trafficPollingJobs.clear()
        super.onCleared()
    }
}
