package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.DhcpNetwork
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DhcpNetworkListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class NetworkItem(
        val id: String,
        val address: String,
        val gateway: String,
        val dnsServer: String,
        val comment: String
    )

    data class UiState(
        val items: List<NetworkItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showEditDialog: Boolean = false,
        val editingItem: NetworkItem? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        if (repository.isConfigured()) loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getDhcpNetworks()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    items = result.getOrDefault(emptyList()).map { it.toItem() },
                    isLoading = false
                )
            }
        }
    }

    fun showEditDialog(item: NetworkItem) {
        _uiState.update { it.copy(showEditDialog = true, editingItem = item, error = null) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingItem = null) }
    }

    fun saveEdit(id: String, gateway: String, dnsServer: String, comment: String) {
        viewModelScope.launch {
            val updates = buildMap {
                put("gateway", gateway)
                put("dns-server", dnsServer)
                put("comment", comment)
            }
            val result = repository.editDhcpNetwork(id, updates)
            if (result.isFailure) {
                _uiState.update { it.copy(error = "保存失败") }
                return@launch
            }
            hideEditDialog()
            loadData()
        }
    }

    private fun DhcpNetwork.toItem() = NetworkItem(
        id = id,
        address = address,
        gateway = gateway,
        dnsServer = dnsServer,
        comment = comment
    )
}
