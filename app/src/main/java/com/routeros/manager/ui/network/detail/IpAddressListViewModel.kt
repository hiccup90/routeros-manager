package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.IpAddress
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
class IpAddressListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class AddressItem(
        val id: String,
        val address: String,
        val interface_: String,
        val disabled: Boolean
    )

    data class UiState(
        val items: List<AddressItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showAddDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val editingId: String? = null,
        val editingAddress: IpAddress? = null,
        val availableInterfaces: List<String> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        if (repository.isConfigured()) loadData()
    }

    fun loadData() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val (addrResult, ifaceResult) = coroutineScope {
                val addrDeferred = async { repository.getIpAddresses() }
                val ifaceDeferred = async { repository.getInterfaces() }
                addrDeferred.await() to ifaceDeferred.await()
            }

            if (addrResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "加载失败: ${addrResult.exceptionOrNull()?.message}"
                    )
                }
                return@launch
            }

            val interfaces = ifaceResult.getOrDefault(emptyList()).map { it.name }
            val addresses = addrResult.getOrDefault(emptyList())

            val items = addresses.map { addr ->
                AddressItem(
                    id = addr.id,
                    address = addr.address,
                    interface_ = addr.interface_,
                    disabled = addr.disabled == "true"
                )
            }

            _uiState.update {
                it.copy(items = items, isLoading = false, availableInterfaces = interfaces)
            }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun showEditDialog(item: AddressItem) {
        val addr = IpAddress(
            id = item.id,
            address = item.address,
            interface_ = item.interface_,
            disabled = if (item.disabled) "true" else "false"
        )
        _uiState.update { it.copy(showEditDialog = true, editingId = item.id, editingAddress = addr) }
    }
    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingId = null, editingAddress = null) }
    }

    fun addAddress(address: String, interface_: String, comment: String?) {
        viewModelScope.launch {
            val result = repository.addIpAddress(address, interface_, comment)
            if (result.isSuccess) {
                hideAddDialog()
                loadData()
            } else {
                _uiState.update { it.copy(error = "添加失败: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun editAddress(id: String, updates: Map<String, String>) {
        viewModelScope.launch {
            val result = repository.editIpAddress(id, updates)
            if (result.isSuccess) {
                hideEditDialog()
                loadData()
            } else {
                _uiState.update { it.copy(error = "保存失败: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun toggleEnable(id: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            val result = if (currentlyDisabled) {
                repository.editIpAddress(id, mapOf("disabled" to "false"))
            } else {
                repository.editIpAddress(id, mapOf("disabled" to "true"))
            }
            if (result.isSuccess) loadData()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.deleteIpAddress(id)
            loadData()
        }
    }
}
