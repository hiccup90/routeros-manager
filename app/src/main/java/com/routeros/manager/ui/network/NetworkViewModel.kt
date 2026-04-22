package com.routeros.manager.ui.network

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class NetworkMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String
)

data class NetworkUiState(
    val menuItems: List<NetworkMenuItem> = emptyList(),
    val expandedSection: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NetworkViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    init {
        loadMenuItems()
    }

    private fun loadMenuItems() {
        _uiState.value = NetworkUiState(
            menuItems = listOf(
                NetworkMenuItem(
                    id = "ip_addresses",
                    title = "IP 地址",
                    subtitle = "IPv4/IPv6 地址管理",
                    icon = "ip"
                ),
                NetworkMenuItem(
                    id = "dhcp",
                    title = "DHCP",
                    subtitle = "DHCP 客户端/服务器/租约",
                    icon = "dhcp"
                ),
                NetworkMenuItem(
                    id = "dns",
                    title = "DNS",
                    subtitle = "DNS 静态记录",
                    icon = "dns"
                ),
                NetworkMenuItem(
                    id = "firewall",
                    title = "防火墙",
                    subtitle = "NAT/过滤规则",
                    icon = "firewall"
                )
            )
        )
    }

    fun toggleSection(sectionId: String) {
        _uiState.value = _uiState.value.copy(
            expandedSection = if (_uiState.value.expandedSection == sectionId) null else sectionId
        )
    }
}