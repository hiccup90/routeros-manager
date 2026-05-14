package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import com.routeros.manager.ui.components.GlassButton
import com.routeros.manager.ui.components.GlassTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ipv6FirewallAddressListScreen(
    viewModel: Ipv6FirewallAddressListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("IPv6 Address Lists") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                actions = { IconButton(onClick = { viewModel.loadData() }) { Icon(Icons.Outlined.Refresh, "刷新") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Outlined.Add, "添加")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> DetailListSkeleton()
                uiState.items.isEmpty() -> Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("暂无 IPv6 Address List", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        val meta = remember(item.id, item.timeout, item.creationTime, item.dynamic) {
                            listOfNotNull(item.timeout.ifBlank { null }, item.creationTime.ifBlank { null }, if (item.dynamic) "dynamic" else null).joinToString(" | ")
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            onClick = { viewModel.showEditDialog(item) }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.list.ifBlank { "(未命名列表)" }, style = MaterialTheme.typography.bodyLarge)
                                    Text(item.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (meta.isNotEmpty()) Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.comment.isNotEmpty()) Text(item.comment, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(checked = !item.disabled, onCheckedChange = { viewModel.toggleEnable(item.id, item.disabled) }, modifier = Modifier.scale(0.8f))
                                    IconButton(onClick = { pendingDeleteId = item.id }) { Icon(Icons.Outlined.Delete, "删除", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }

        pendingDeleteId?.let { deletingId ->
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除此 IPv6 Address List 项吗？") },
                confirmButton = {
                    GlassButton(
                        text = "删除",
                        onClick = { pendingDeleteId = null; viewModel.delete(deletingId) },
                        primary = false
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = { pendingDeleteId = null },
                        primary = false
                    )
                }
            )
        }

        if (uiState.showAddDialog) {
            var list by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var timeout by remember { mutableStateOf("") }
            var comment by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.hideAddDialog() },
                title = { Text("添加 IPv6 Address List") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(value = list, onValueChange = { list = it }, label = { Text("List") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = timeout, onValueChange = { timeout = it }, label = { Text("Timeout（可选）") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "确定",
                        onClick = { viewModel.addItem(list, address, timeout, comment.ifBlank { null }) }
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = { viewModel.hideAddDialog() },
                        primary = false
                    )
                }
            )
        }

        if (uiState.showEditDialog && uiState.editingItem != null) {
            val item = uiState.editingItem!!
            var list by remember(item.id) { mutableStateOf(item.list) }
            var address by remember(item.id) { mutableStateOf(item.address) }
            var timeout by remember(item.id) { mutableStateOf(item.timeout) }
            var comment by remember(item.id) { mutableStateOf(item.comment) }
            AlertDialog(
                onDismissRequest = { viewModel.hideEditDialog() },
                title = { Text("编辑 IPv6 Address List") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(value = list, onValueChange = { list = it }, label = { Text("List") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = timeout, onValueChange = { timeout = it }, label = { Text("Timeout") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "确定",
                        onClick = { viewModel.editItem(uiState.editingId!!, list, address, timeout, comment) }
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = { viewModel.hideEditDialog() },
                        primary = false
                    )
                }
            )
        }
    }
}
