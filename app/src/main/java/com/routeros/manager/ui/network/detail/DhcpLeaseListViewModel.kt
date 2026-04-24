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
        val address: String,
        val macAddress: String,
        val displayName: String,
        val hostname: String,
        val activeHostName: String,
        val status: String,
        val server: String,
        val expires: String,
        val comment: String,
        val isDynamic: Boolean
    )

    data class UiState(
        val items: List<LeaseItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showEditDialog: Boolean = false,
        val editingItem: LeaseItem? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { if (repository.isConfigured()) loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getDhcpLeases()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList()).map { it.toLeaseItem() }
            _uiState.update { it.copy(items = items, isLoading = false) }
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

    fun showEditDialog(item: LeaseItem) {
        _uiState.update { it.copy(showEditDialog = true, editingItem = item, error = null) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingItem = null) }
    }

    fun editLease(id: String, comment: String, address: String, server: String) {
        viewModelScope.launch {
            val updates = buildMap {
                put("comment", comment)
                put("address", address)
                put("server", server)
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

    private fun DhcpLease.toLeaseItem(): LeaseItem {
        val displayName = activeHostName.ifBlank {
            hostname.ifBlank {
                address
            }
        }
        return LeaseItem(
            id = id,
            address = address,
            macAddress = macAddress,
            displayName = displayName,
            hostname = hostname,
            activeHostName = activeHostName,
            status = status,
            server = server,
            expires = expires,
            comment = comment,
            isDynamic = dynamic == "true"
        )
    }
}
