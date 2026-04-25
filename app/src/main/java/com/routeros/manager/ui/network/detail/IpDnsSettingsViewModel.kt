package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class IpDnsSettingsViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class UiState(
        val servers: String = "--",
        val dynamicServers: String = "--",
        val allowRemoteRequests: String = "--",
        val cacheSize: String = "--",
        val cacheUsed: String = "--",
        val maxConcurrentQueries: String = "--",
        val maxConcurrentTcpSessions: String = "--",
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        if (repository.isConfigured()) loadData()
        else _uiState.update { it.copy(isLoading = false, error = "请先在设置中配置 RouterOS 连接") }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getIpDnsSettings()
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            servers = formatServerList(settings.servers),
                            dynamicServers = formatServerList(settings.dynamicServers),
                            allowRemoteRequests = if (settings.allowRemoteRequests == "true") "已开启" else "已关闭",
                            cacheSize = formatSize(settings.cacheSize),
                            cacheUsed = formatSize(settings.cacheUsed),
                            maxConcurrentQueries = settings.maxConcurrentQueries.ifBlank { "--" },
                            maxConcurrentTcpSessions = settings.maxConcurrentTcpSessions.ifBlank { "--" },
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = "加载失败: ${error.message}") }
                }
        }
    }

    private fun formatServerList(value: String): String {
        if (value.isBlank()) return "--"
        return value.split(',').map { it.trim() }.filter { it.isNotBlank() }.joinToString(", ")
    }

    private fun formatSize(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return "--"
        val kib = trimmed.removeSuffix("KiB").toDoubleOrNull() ?: return trimmed
        return if (kib >= 1024) {
            String.format(Locale.US, "%.2f MiB", kib / 1024.0)
        } else {
            String.format(Locale.US, "%.2f KiB", kib)
        }
    }
}
