package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.FirewallConnection
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Ipv6FirewallConnectionListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class UiState(
        val items: List<FirewallConnection> = emptyList(),
        val filteredItems: List<FirewallConnection> = emptyList(),
        val query: String = "",
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { if (repository.isConfigured()) loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val props = listOf(".id", "protocol", "src-address", "dst-address", "src-port", "dst-port", "reply-src-address", "reply-dst-address", "reply-src-port", "reply-dst-port", "tcp-state", "timeout", "orig-bytes", "repl-bytes", "orig-packets", "repl-packets", "assured", "seen-reply", "fasttrack", "connection-mark")
            val result = repository.getIpv6FirewallConnections(props)
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList())
            _uiState.update { state -> state.copy(items = items, filteredItems = filter(items, state.query), isLoading = false) }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { state -> state.copy(query = query, filteredItems = filter(state.items, query)) }
    }

    private fun filter(items: List<FirewallConnection>, query: String): List<FirewallConnection> {
        val keyword = query.trim().lowercase()
        if (keyword.isEmpty()) return items
        return items.filter { item ->
            listOf(item.protocol, item.srcAddress, item.dstAddress, item.srcPort, item.dstPort, item.tcpState, item.connectionMark)
                .any { it.lowercase().contains(keyword) }
        }
    }
}
