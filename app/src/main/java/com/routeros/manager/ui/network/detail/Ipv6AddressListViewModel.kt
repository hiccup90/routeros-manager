package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.Ipv6Address
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Ipv6AddressListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class AddressItem(
        val id: String,
        val address: String,
        val interface_: String,
        val advertise: Boolean,
        val disabled: Boolean,
        val comment: String
    )

    data class UiState(
        val items: List<AddressItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showAddDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val editingId: String? = null,
        val editingItem: Ipv6Address? = null,
        val availableInterfaces: List<String> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { if (repository.isConfigured()) loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val addrResult = repository.getIpv6Addresses()
            val ifaceResult = repository.getInterfaces()
            if (addrResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${addrResult.exceptionOrNull()?.message}") }
                return@launch
            }
            val interfaces = ifaceResult.getOrDefault(emptyList()).map { it.name }
            val items = addrResult.getOrDefault(emptyList()).map { addr ->
                AddressItem(id = addr.id, address = addr.address, interface_ = addr.interface_,
                    advertise = addr.advertise == "true", disabled = addr.disabled == "true", comment = addr.comment)
            }
            _uiState.update { it.copy(items = items, isLoading = false, availableInterfaces = interfaces) }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun showEditDialog(item: AddressItem) {
        val addr = Ipv6Address(id = item.id, address = item.address, interface_ = item.interface_,
            advertise = if (item.advertise) "true" else "false", disabled = if (item.disabled) "true" else "false", comment = item.comment)
        _uiState.update { it.copy(showEditDialog = true, editingId = item.id, editingItem = addr) }
    }
    fun hideEditDialog() = _uiState.update { it.copy(showEditDialog = false, editingId = null, editingItem = null) }

    fun addAddress(address: String, interface_: String, advertise: Boolean, comment: String?) {
        viewModelScope.launch {
            val result = repository.addIpv6Address(address, interface_, if (advertise) "true" else "false", comment)
            if (result.isSuccess) { hideAddDialog(); loadData() }
            else _uiState.update { it.copy(error = "添加失败") }
        }
    }

    fun editAddress(id: String, updates: Map<String, String>) {
        viewModelScope.launch {
            val result = repository.editIpv6Address(id, updates)
            if (result.isSuccess) { hideEditDialog(); loadData() }
            else _uiState.update { it.copy(error = "保存失败") }
        }
    }

    fun toggleEnable(id: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            val result = if (currentlyDisabled) repository.editIpv6Address(id, mapOf("disabled" to "false"))
            else repository.editIpv6Address(id, mapOf("disabled" to "true"))
            if (result.isSuccess) loadData()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteIpv6Address(id); loadData() }
    }
}
