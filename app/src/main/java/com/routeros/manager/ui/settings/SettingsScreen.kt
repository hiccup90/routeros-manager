package com.routeros.manager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.routeros.manager.ui.components.GlassButton
import com.routeros.manager.ui.components.GlassCard
import com.routeros.manager.ui.components.GlassScaffold
import com.routeros.manager.ui.components.GlassTextField
import com.routeros.manager.ui.components.GlassTitleBar
import com.routeros.manager.ui.components.animateGlassSize
import com.routeros.manager.ui.theme.PrimaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    GlassScaffold(
        topBar = { GlassTitleBar(title = "设置") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
                Text("连接设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .animateGlassSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GlassTextField(
                            value = uiState.host,
                            onValueChange = { viewModel.updateHost(it) },
                            label = { Text("主机地址") },
                            placeholder = { Text("https://router.example.com 或 192.168.1.1") },
                            leadingIcon = { Icon(Icons.Default.Router, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )

                        GlassTextField(
                            value = if (uiState.port == 0) "" else uiState.port.toString(),
                            onValueChange = { viewModel.updatePort(it) },
                            label = { Text("端口") },
                            leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        GlassTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.updateUsername(it) },
                            label = { Text("用户名") },
                            placeholder = { Text("admin") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        GlassTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = { Text("密码") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = if (uiState.isLoading) "连接中..." else "测试连接",
                        onClick = { viewModel.testConnection() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading && uiState.host.isNotEmpty(),
                        primary = false
                    )
                    GlassButton(
                        text = "保存",
                        onClick = { viewModel.saveConnection() },
                        modifier = Modifier.weight(1f),
                        primary = true,
                        leadingIcon = Icons.Default.Save
                    )
                }

                Text("首页接口显示", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lan, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.selectedInterfaces.isEmpty()) "显示前 3 个接口" else "已选择 ${uiState.selectedInterfaces.size} 个",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row {
                                IconButton(onClick = { viewModel.selectAllInterfaces() }) {
                                    Icon(Icons.Default.SelectAll, contentDescription = "全选", modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { viewModel.clearInterfaceSelection() }) {
                                    Icon(Icons.Default.Delete, contentDescription = "清空", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (uiState.isLoadingInterfaces) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (uiState.availableInterfaces.isEmpty()) {
                            Text("暂无可用接口，请先测试连接", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val orderedInterfaces = uiState.selectedInterfaces + uiState.availableInterfaces.filterNot { it in uiState.selectedInterfaces }
                            orderedInterfaces.forEach { name ->
                                val checked = uiState.selectedInterfaces.contains(name)
                                val selectedIndex = uiState.selectedInterfaces.indexOf(name)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .toggleable(
                                            value = checked,
                                            role = Role.Checkbox,
                                            onValueChange = { viewModel.toggleInterface(name) }
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryTeal)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    if (checked) {
                                        IconButton(onClick = { viewModel.moveInterface(name, -1) }, enabled = selectedIndex > 0) {
                                            Icon(Icons.Default.ArrowUpward, contentDescription = "上移")
                                        }
                                        IconButton(
                                            onClick = { viewModel.moveInterface(name, 1) },
                                            enabled = selectedIndex in 0 until uiState.selectedInterfaces.lastIndex
                                        ) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = "下移")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Text("危险区域", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                            .animateGlassSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("清除所有本地保存的 RouterOS 连接信息", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("此操作会删除当前保存的主机、端口、用户名、密码以及首页接口选择。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        GlassButton(
                            text = "清除全部数据",
                            onClick = { viewModel.clearAllData() },
                            primary = false,
                            leadingIcon = Icons.Default.Delete,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
    }
}
