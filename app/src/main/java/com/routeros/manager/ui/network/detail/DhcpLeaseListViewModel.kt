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
        val comment: String
    )

    data class UiState(
        val items: List<LeaseItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
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
            val items = result.getOrDefault(emptyList()).map { l ->
                val displayName = l.activeHostName.ifBlank {
                    l.hostname.ifBlank {
                        l.address
                    }
                }
                LeaseItem(
                    id = l.id,
                    address = l.address,
                    macAddress = l.macAddress,
                    displayName = displayName,
                    hostname = l.hostname,
                    activeHostName = l.activeHostName,
                    status = l.status,
                    server = l.server,
                    expires = l.expires,
                    comment = l.comment
                )
            }
            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }
}
