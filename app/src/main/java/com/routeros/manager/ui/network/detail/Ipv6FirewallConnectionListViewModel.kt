package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.FirewallConnection
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class Ipv6FirewallConnectionListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class Summary(
        val total: Int = 0,
        val tcp: Int = 0,
        val udp: Int = 0,
        val established: Int = 0,
        val lastRefreshedAt: String = ""
    )

    data class UiState(
        val summary: Summary = Summary(),
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
            val props = listOf("protocol", "tcp-state")
            val result = repository.getIpv6FirewallConnections(props)
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    summary = buildSummary(items),
                    isLoading = false
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun buildSummary(items: List<FirewallConnection>): Summary {
        val tcp = items.count { it.protocol.equals("tcp", ignoreCase = true) }
        val udp = items.count { it.protocol.equals("udp", ignoreCase = true) }
        val established = items.count { it.tcpState.equals("established", ignoreCase = true) }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return Summary(
            total = items.size,
            tcp = tcp,
            udp = udp,
            established = established,
            lastRefreshedAt = LocalDateTime.now().format(formatter)
        )
    }
}
