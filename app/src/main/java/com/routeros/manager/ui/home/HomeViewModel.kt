package com.routeros.manager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.InterfaceItem
import com.routeros.manager.data.preferences.SecurePreferences
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

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

    private var previousInterfaceBytes: Map<String, Pair<Long, Long>> = emptyMap()
    private var pollingJob: Job? = null

    init {
        ensurePollingState()
    }

    fun ensurePollingState() {
        if (repository.isConfigured()) {
            startPolling()
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val results = withTimeoutOrNull(10_000) {
                coroutineScope {
                    val identityDeferred = async { repository.getSystemIdentity() }
                    val resourceDeferred = async { repository.getSystemResource() }
                    val interfacesDeferred = async { repository.getInterfaces() }
                    Triple(identityDeferred.await(), resourceDeferred.await(), interfacesDeferred.await())
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

            val (identityResult, resourceResult, interfacesResult) = results
            if (resourceResult.isFailure) {
                securePreferences.isConnected = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "连接失败: ${resourceResult.exceptionOrNull()?.message ?: "未知错误"}",
                        isConnected = false
                    )
                }
                return@launch
            }

            val identity = identityResult.getOrNull()
            val resource = resourceResult.getOrNull()
            if (resource == null) {
                securePreferences.isConnected = false
                _uiState.update {
                    it.copy(isLoading = false, error = "获取系统信息失败", isConnected = false)
                }
                return@launch
            }

            val interfaces = if (interfacesResult.isSuccess) {
                val all = interfacesResult.getOrDefault(emptyList())
                val filtered = filterHomeInterfaces(all)
                calculateInterfaceRates(filtered)
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
                    error = interfacesResult.exceptionOrNull()?.message,
                    isConnected = true
                )
            }
        }
    }

    private fun filterHomeInterfaces(interfaces: List<InterfaceItem>): List<InterfaceItem> {
        val selected = securePreferences.homeInterfaceNames
        if (selected.isEmpty()) return interfaces
        return interfaces.filter { it.name in selected }
    }

    private fun calculateInterfaceRates(interfaces: List<InterfaceItem>): List<InterfaceUiModel> {
        return interfaces.map { iface ->
            val prev = previousInterfaceBytes[iface.name]
            val rxCurrent = iface.rxByte.toLongOrNull() ?: 0L
            val txCurrent = iface.txByte.toLongOrNull() ?: 0L
            val rxDiff = if (prev != null) rxCurrent - prev.first else 0L
            val txDiff = if (prev != null) txCurrent - prev.second else 0L

            previousInterfaceBytes = previousInterfaceBytes + (iface.name to Pair(rxCurrent, txCurrent))

            InterfaceUiModel(
                id = iface.id,
                name = iface.name,
                type = iface.type,
                disabled = iface.disabled == "true",
                rxRate = formatRate(rxDiff / 3),
                txRate = formatRate(txDiff / 3),
                rxBytes = rxCurrent,
                txBytes = txCurrent
            )
        }
    }

    private fun formatRate(bytesPerSec: Long): String {
        return when {
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
                delay(3000)
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
        stopPolling()
    }
}
