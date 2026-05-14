package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.FirewallAddressList
import com.routeros.manager.data.api.FirewallAddressListRequest
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirewallAddressListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class Item(
        val id: String,
        val list: String,
        val address: String,
        val timeout: String,
        val comment: String,
        val creationTime: String,
        val disabled: Boolean,
        val dynamic: Boolean
    )

    data class UiState(
        val items: List<Item> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showAddDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val editingId: String? = null,
        val editingItem: FirewallAddressList? = null
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
            val result = repository.getFirewallAddressLists()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    items = result.getOrDefault(emptyList()).map { item ->
                        Item(
                            id = item.id,
                            list = item.list,
                            address = item.address,
                            timeout = item.timeout,
                            comment = item.comment,
                            creationTime = item.creationTime,
                            disabled = item.disabled == "true",
                            dynamic = item.dynamic == "true"
                        )
                    },
                    isLoading = false
                )
            }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun showEditDialog(item: Item) {
        val model = FirewallAddressList(
            id = item.id,
            list = item.list,
            address = item.address,
            disabled = if (item.disabled) "true" else "false",
            dynamic = if (item.dynamic) "true" else "false",
            creationTime = item.creationTime,
            timeout = item.timeout,
            comment = item.comment
        )
        _uiState.update { it.copy(showEditDialog = true, editingId = item.id, editingItem = model) }
    }

    fun hideEditDialog() = _uiState.update { it.copy(showEditDialog = false, editingId = null, editingItem = null) }

    fun addItem(list: String, address: String, timeout: String, comment: String?) {
        viewModelScope.launch {
            val result = repository.addFirewallAddressList(
                FirewallAddressListRequest(
                    list = list,
                    address = address,
                    timeout = timeout.ifBlank { null },
                    comment = comment
                )
            )
            if (result.isSuccess) {
                hideAddDialog()
                loadData()
            } else {
                _uiState.update { it.copy(error = "添加失败") }
            }
        }
    }

    fun editItem(id: String, list: String, address: String, timeout: String, comment: String) {
        viewModelScope.launch {
            val updates = buildMap {
                put("list", list)
                put("address", address)
                put("comment", comment)
                if (timeout.isNotBlank()) put("timeout", timeout)
            }
            val result = repository.editFirewallAddressList(id, updates)
            if (result.isSuccess) {
                hideEditDialog()
                loadData()
            } else {
                _uiState.update { it.copy(error = "保存失败") }
            }
        }
    }

    fun toggleEnable(id: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            val result = if (currentlyDisabled) repository.enableFirewallAddressList(id) else repository.disableFirewallAddressList(id)
            if (result.isSuccess) loadData()
            else _uiState.update { it.copy(error = "切换失败") }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val result = repository.deleteFirewallAddressList(id)
            if (result.isSuccess) loadData()
            else _uiState.update { it.copy(error = "删除失败") }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
