package com.routeros.manager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routeros.manager.data.api.NetworkClient
import com.routeros.manager.data.preferences.SecurePreferences
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val host: String = "",
    val port: Int = 0,
    val username: String = "",
    val password: String = "",
    val displayMode: String = "dark",
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val availableInterfaces: List<String> = emptyList(),
    val selectedInterfaces: Set<String> = emptySet(),
    val isLoadingInterfaces: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val repository: RouterOSRepository,
    private val networkClient: NetworkClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            host = securePreferences.host,
            port = securePreferences.port,
            username = securePreferences.username,
            password = securePreferences.password,
            displayMode = securePreferences.displayMode,
            isConnected = securePreferences.isConnected,
            selectedInterfaces = securePreferences.homeInterfaceNames
        )
        if (securePreferences.isConnected) {
            loadInterfaces()
        }
    }

    fun updateHost(host: String) {
        _uiState.value = _uiState.value.copy(host = host)
    }

    fun updatePort(port: String) {
        _uiState.value = _uiState.value.copy(port = port.toIntOrNull() ?: 0)
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun saveConnection() {
        val state = _uiState.value
        val endpoint = networkClient.normalizeEndpointInput(state.host)
        val sanitizedUsername = state.username.trim()
        val normalizedPort = normalizePort(state.port)
        if (endpoint.host.isBlank()) {
            _uiState.value = state.copy(error = "请输入主机地址", successMessage = null)
            return
        }
        if (sanitizedUsername.isBlank()) {
            _uiState.value = state.copy(error = "请输入用户名", successMessage = null)
            return
        }
        if (normalizedPort == null) {
            _uiState.value = state.copy(error = "请输入端口", successMessage = null)
            return
        }

        repository.updateConnection(endpoint.normalizedInput, normalizedPort, sanitizedUsername, state.password)
        securePreferences.isConnected = false
        _uiState.value = state.copy(
            host = endpoint.normalizedInput,
            port = normalizedPort,
            username = sanitizedUsername,
            isConnected = false,
            error = null,
            successMessage = "连接设置已保存"
        )
    }

    fun testConnection() {
        val state = _uiState.value
        val endpoint = networkClient.normalizeEndpointInput(state.host)
        val sanitizedUsername = state.username.trim()
        val normalizedPort = normalizePort(state.port)
        if (endpoint.host.isBlank()) {
            _uiState.value = state.copy(error = "请输入主机地址", successMessage = null)
            return
        }
        if (sanitizedUsername.isBlank()) {
            _uiState.value = state.copy(error = "请输入用户名", successMessage = null)
            return
        }
        if (normalizedPort == null) {
            _uiState.value = state.copy(error = "请输入端口", successMessage = null)
            return
        }

        _uiState.value = state.copy(
            host = endpoint.normalizedInput,
            port = normalizedPort,
            username = sanitizedUsername,
            isLoading = true,
            error = null,
            successMessage = null
        )

        viewModelScope.launch {
            val result = runCatching {
                val api = networkClient.createApi(
                    host = endpoint.normalizedInput,
                    port = normalizedPort,
                    username = sanitizedUsername,
                    password = state.password
                )
                val identity = api.getSystemIdentity().firstOrNull().orEmpty()
                val resource = api.getSystemResource().firstOrNull().orEmpty()
                identity["name"].orEmpty() to resource["version"].orEmpty()
            }

            if (result.isSuccess) {
                val (routerName, version) = result.getOrDefault("" to "")
                securePreferences.isConnected = true
                securePreferences.lastConnected = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = true,
                    error = null,
                    successMessage = buildString {
                        append("连接成功")
                        if (routerName.isNotBlank()) append(" · $routerName")
                        if (version.isNotBlank()) append(" · RouterOS $version")
                    }
                )
                loadInterfaces()
            } else {
                securePreferences.isConnected = false
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = false,
                    error = "连接失败: ${result.exceptionOrNull()?.message ?: "未知错误"}",
                    successMessage = null
                )
            }
        }
    }

    private fun normalizePort(port: Int): Int? {
        return port.takeIf { it in 1..65535 }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun clearAllData() {
        securePreferences.clearAll()
        loadSettings()
        _uiState.value = _uiState.value.copy(
            successMessage = "所有数据已清除"
        )
    }

    fun loadInterfaces() {
        if (!securePreferences.hasCredentials()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingInterfaces = true)
            val result = repository.getInterfaces()
            if (result.isSuccess) {
                val names = result.getOrDefault(emptyList()).map { it.name }.sorted()
                val availableSet = names.toSet()
                val cleanedSelection = _uiState.value.selectedInterfaces.intersect(availableSet)
                if (cleanedSelection != securePreferences.homeInterfaceNames) {
                    securePreferences.homeInterfaceNames = cleanedSelection
                }
                _uiState.value = _uiState.value.copy(
                    availableInterfaces = names,
                    selectedInterfaces = cleanedSelection,
                    isLoadingInterfaces = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingInterfaces = false,
                    error = "加载接口列表失败"
                )
            }
        }
    }

    fun toggleInterface(name: String) {
        val current = _uiState.value.selectedInterfaces.toMutableSet()
        if (current.contains(name)) {
            current.remove(name)
        } else {
            current.add(name)
        }
        _uiState.value = _uiState.value.copy(selectedInterfaces = current)
        securePreferences.homeInterfaceNames = current
    }

    fun selectAllInterfaces() {
        val all = _uiState.value.availableInterfaces.toSet()
        _uiState.value = _uiState.value.copy(selectedInterfaces = all)
        securePreferences.homeInterfaceNames = all
    }

    fun clearInterfaceSelection() {
        _uiState.value = _uiState.value.copy(selectedInterfaces = emptySet())
        securePreferences.homeInterfaceNames = emptySet()
    }
}
