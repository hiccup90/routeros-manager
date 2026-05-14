package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.Ipv6Address
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private var loadJob: Job? = null

    init {
        if (repository.isConfigured()) loadData()
        else _uiState.update { it.copy(isLoading = false, error = "请先在设置中配置 RouterOS 连接") }
    }

    fun loadData() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val (addrResult, ifaceResult) = coroutineScope {
                val addrDeferred = async { repository.getIpv6Addresses() }
                val ifaceDeferred = async { repository.getInterfaces() }
                addrDeferred.await() to ifaceDeferred.await()
            }
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
            else _uiState.update { it.copy(error = "切换失败") }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val result = repository.deleteIpv6Address(id)
            if (result.isSuccess) loadData()
            else _uiState.update { it.copy(error = "删除失败") }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
