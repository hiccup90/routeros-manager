package com.routeros.manager.ui.network.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DhcpServerListViewModel @Inject constructor(
    private val repository: RouterOSRepository
) : ViewModel() {

    data class ServerItem(val id: String, val name: String, val interface_: String, val addressPool: String, val disabled: Boolean, val comment: String)

    data class UiState(
        val items: List<ServerItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
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
            val result = repository.getDhcpServers()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "加载失败: ${result.exceptionOrNull()?.message}") }
                return@launch
            }
            val items = result.getOrDefault(emptyList()).map { s ->
                ServerItem(id = s.id, name = s.name, interface_ = s.interface_, addressPool = s.addressPool, disabled = s.disabled == "true", comment = s.comment)
            }
            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }

    fun toggleEnable(id: String, currentlyDisabled: Boolean) {
        viewModelScope.launch {
            val result = if (currentlyDisabled) repository.enableDhcpServer(id) else repository.disableDhcpServer(id)
            if (result.isSuccess) loadData()
            else _uiState.update { it.copy(error = "切换失败") }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
