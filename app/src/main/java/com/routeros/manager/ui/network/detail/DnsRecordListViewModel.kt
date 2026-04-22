package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.DnsRecord
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DnsRecordListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class RecordItem(val id: String, val name: String, val address: String, val ttl: String, val disabled: Boolean, val comment: String)

    data class UiState(
        val items: List<RecordItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showAddDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val editingId: String? = null,
        val editingItem: DnsRecord? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { if (repository.isConfigured()) loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getDnsRecords()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList()).map { r ->
                RecordItem(id = r.id, name = r.name, address = r.address, ttl = r.ttl, disabled = r.disabled == "true", comment = r.comment)
            }
            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun showEditDialog(item: RecordItem) {
        val record = DnsRecord(id = item.id, name = item.name, address = item.address, ttl = item.ttl, disabled = if (item.disabled) "true" else "false", comment = item.comment)
        _uiState.update { it.copy(showEditDialog = true, editingId = item.id, editingItem = record) }
    }
    fun hideEditDialog() = _uiState.update { it.copy(showEditDialog = false, editingId = null, editingItem = null) }

    fun addRecord(name: String, address: String, ttl: String?, comment: String?) {
        viewModelScope.launch {
            val result = repository.addDnsRecord(name, address, ttl, comment)
            if (result.isSuccess) { hideAddDialog(); loadData() }
            else _uiState.update { it.copy(error = "添加失败") }
        }
    }

    fun editRecord(id: String, updates: Map<String, String>) {
        viewModelScope.launch {
            val result = repository.editDnsRecord(id, updates)
            if (result.isSuccess) { hideEditDialog(); loadData() }
            else _uiState.update { it.copy(error = "保存失败") }
        }
    }

    fun toggleEnable(id: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            val result = repository.editDnsRecord(id, mapOf("disabled" to if (currentlyDisabled) "false" else "true"))
            if (result.isSuccess) loadData()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteDnsRecord(id); loadData() }
    }
}
