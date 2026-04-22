package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.FirewallFilter
import com.routeros.manager.data.api.FilterRuleRequest
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterRuleListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class RuleItem(val id: String, val chain: String, val action: String, val protocol: String,
        val srcAddress: String, val dstAddress: String, val disabled: Boolean, val comment: String)

    data class UiState(
        val items: List<RuleItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val showAddDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val editingId: String? = null,
        val editingItem: FirewallFilter? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { if (repository.isConfigured()) loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getFirewallFilters()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList()).map { r ->
                RuleItem(id = r.id, chain = r.chain, action = r.action, protocol = r.protocol,
                    srcAddress = r.srcAddress, dstAddress = r.dstAddress, disabled = r.disabled == "true", comment = r.comment)
            }
            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun showEditDialog(item: RuleItem) {
        val rule = FirewallFilter(id = item.id, chain = item.chain, action = item.action, protocol = item.protocol,
            srcAddress = item.srcAddress, dstAddress = item.dstAddress, disabled = if (item.disabled) "true" else "false", comment = item.comment)
        _uiState.update { it.copy(showEditDialog = true, editingId = item.id, editingItem = rule) }
    }
    fun hideEditDialog() = _uiState.update { it.copy(showEditDialog = false, editingId = null, editingItem = null) }

    fun addRule(chain: String, action: String, protocol: String, srcAddress: String, dstAddress: String, comment: String?) {
        viewModelScope.launch {
            val request = FilterRuleRequest(chain = chain, action = action, protocol = protocol.ifBlank { null },
                srcAddress = srcAddress.ifBlank { null }, dstAddress = dstAddress.ifBlank { null }, comment = comment)
            val result = repository.addFirewallFilter(request)
            if (result.isSuccess) { hideAddDialog(); loadData() }
            else _uiState.update { it.copy(error = "添加失败") }
        }
    }

    fun editRule(id: String, updates: Map<String, String>) {
        viewModelScope.launch {
            val result = repository.editFirewallFilter(id, updates)
            if (result.isSuccess) { hideEditDialog(); loadData() }
            else _uiState.update { it.copy(error = "保存失败") }
        }
    }

    fun toggleEnable(id: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            if (currentlyDisabled) repository.enableFirewallFilter(id) else repository.disableFirewallFilter(id)
            loadData()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteFirewallFilter(id); loadData() }
    }
}
