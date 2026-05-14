package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.DhcpLease
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DhcpLeaseListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class LeaseItem(
        val id: String,
        val searchKey: String,
        val address: String,
        val macAddress: String,
        val displayName: String,
        val hostname: String,
        val activeHostName: String,
        val status: String,
        val server: String,
        val addressList: String,
        val dhcpOption: String,
        val expires: String,
        val comment: String,
        val isDynamic: Boolean
    )

    data class UiState(
        val items: List<LeaseItem> = emptyList(),
        val filteredItems: List<LeaseItem> = emptyList(),
        val query: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val showEditDialog: Boolean = false,
        val editingItem: LeaseItem? = null,
        val showStaticBindingDialog: Boolean = false,
        val staticBindingItem: LeaseItem? = null,
        val staticBindingGateway: String = "",
        val staticBindingDnsServer: String = "",
        val staticBindingNetworkId: String? = null
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
            val result = repository.getDhcpLeases()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList()).map { it.toLeaseItem() }
            _uiState.update { state ->
                state.copy(
                    items = items,
                    filteredItems = filterItems(items, state.query),
                    isLoading = false
                )
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { state ->
            state.copy(query = query, filteredItems = filterItems(state.items, query))
        }
    }

    fun makeStatic(id: String) {
        viewModelScope.launch {
            val result = repository.makeDhcpLeaseStatic(id)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.update { it.copy(error = "静态绑定失败") }
            }
        }
    }

    fun showStaticBindingDialog(item: LeaseItem) {
        viewModelScope.launch {
            val network = repository.getDhcpNetworks().getOrDefault(emptyList()).firstOrNull { candidate ->
                sameSubnet(item.address, candidate.address)
            }
            _uiState.update {
                it.copy(
                    showStaticBindingDialog = true,
                    staticBindingItem = item,
                    staticBindingGateway = network?.gateway.orEmpty(),
                    staticBindingDnsServer = network?.dnsServer.orEmpty(),
                    staticBindingNetworkId = network?.id,
                    error = null
                )
            }
        }
    }

    fun hideStaticBindingDialog() {
        _uiState.update {
            it.copy(
                showStaticBindingDialog = false,
                staticBindingItem = null,
                staticBindingGateway = "",
                staticBindingDnsServer = "",
                staticBindingNetworkId = null
            )
        }
    }

    fun saveStaticBinding(id: String, comment: String, address: String, server: String, addressList: String, dhcpOption: String, gateway: String, dnsServer: String) {
        viewModelScope.launch {
            val makeStaticResult = repository.makeDhcpLeaseStatic(id)
            if (makeStaticResult.isFailure) {
                _uiState.update { it.copy(error = "静态绑定失败") }
                return@launch
            }

            val leaseUpdates = buildMap {
                put("comment", comment)
                put("address", address)
                put("server", server)
                put("address-list", addressList)
                put("dhcp-option", dhcpOption)
            }
            val editLeaseResult = repository.editDhcpLease(id, leaseUpdates)
            if (editLeaseResult.isFailure) {
                _uiState.update { it.copy(error = "保存失败") }
                return@launch
            }

            val networkId = uiState.value.staticBindingNetworkId
            if (!networkId.isNullOrBlank() && (gateway.isNotBlank() || dnsServer.isNotBlank())) {
                val networkUpdates = buildMap {
                    if (gateway.isNotBlank()) put("gateway", gateway)
                    if (dnsServer.isNotBlank()) put("dns-server", dnsServer)
                }
                val editNetworkResult = repository.editDhcpNetwork(networkId, networkUpdates)
                if (editNetworkResult.isFailure) {
                    _uiState.update { it.copy(error = "静态绑定已保存，但 DHCP 网络参数保存失败") }
                    return@launch
                }
            }

            hideStaticBindingDialog()
            loadData()
        }
    }

    fun showEditDialog(item: LeaseItem) {
        _uiState.update { it.copy(showEditDialog = true, editingItem = item, error = null) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingItem = null) }
    }

    fun editLease(id: String, comment: String, address: String, server: String, addressList: String, dhcpOption: String) {
        viewModelScope.launch {
            val updates = buildMap {
                put("comment", comment)
                put("address", address)
                put("server", server)
                put("address-list", addressList)
                put("dhcp-option", dhcpOption)
            }
            val result = repository.editDhcpLease(id, updates)
            if (result.isSuccess) {
                hideEditDialog()
                loadData()
            } else {
                _uiState.update { it.copy(error = "保存失败") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun filterItems(items: List<LeaseItem>, query: String): List<LeaseItem> {
        val keyword = query.trim().lowercase()
        if (keyword.isEmpty()) return items
        return items.filter { item -> item.searchKey.contains(keyword) }
    }

    private fun sameSubnet(ipAddress: String, cidr: String): Boolean {
        val ip = ipAddress.substringBefore('/').trim()
        val networkIp = cidr.substringBefore('/').trim()
        val prefix = cidr.substringAfter('/', "24").toIntOrNull() ?: return false
        if (prefix !in 0..32) return false
        val ipValue = ipv4ToInt(ip) ?: return false
        val networkValue = ipv4ToInt(networkIp) ?: return false
        val mask = if (prefix == 0) 0 else (-1 shl (32 - prefix))
        return (ipValue and mask) == (networkValue and mask)
    }

    private fun ipv4ToInt(value: String): Int? {
        val parts = value.split('.')
        if (parts.size != 4) return null
        var result = 0
        for (part in parts) {
            val octet = part.toIntOrNull() ?: return null
            if (octet !in 0..255) return null
            result = (result shl 8) or octet
        }
        return result
    }

    private fun DhcpLease.toLeaseItem(): LeaseItem {
        val displayName = activeHostName.ifBlank {
            hostname.ifBlank {
                address
            }
        }
        return LeaseItem(
            id = id,
            searchKey = listOf(displayName, address, macAddress, server, comment, hostname, activeHostName)
                .joinToString("\n")
                .lowercase(),
            address = address,
            macAddress = macAddress,
            displayName = displayName,
            hostname = hostname,
            activeHostName = activeHostName,
            status = status,
            server = server,
            addressList = addressList,
            dhcpOption = dhcpOption,
            expires = expires,
            comment = comment,
            isDynamic = dynamic == "true"
        )
    }
}
