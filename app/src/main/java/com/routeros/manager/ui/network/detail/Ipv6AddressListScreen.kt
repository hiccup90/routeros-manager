package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.routeros.manager.ui.components.GlassButton
import com.routeros.manager.ui.components.GlassTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ipv6AddressListScreen(
    viewModel: Ipv6AddressListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("IPv6 地址") },
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
                uiState.isLoading && uiState.items.isEmpty() ->
                    DetailListSkeleton()
                uiState.items.isEmpty() ->
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("暂无 IPv6 地址", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            onClick = { viewModel.showEditDialog(item) }) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.address, style = MaterialTheme.typography.bodyLarge, color = if (item.disabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface)
                                    Text(item.interface_, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.advertise) Text("广播", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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
                text = { Text("确定要删除此 IPv6 地址吗？") },
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
            var address by remember { mutableStateOf("") }
            var selectedIface by remember { mutableStateOf(uiState.availableInterfaces.firstOrNull() ?: "") }
            var advertise by remember { mutableStateOf(false) }
            var comment by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { viewModel.hideAddDialog() }, title = { Text("添加 IPv6 地址") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(value = address, onValueChange = { address = it }, label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            GlassTextField(value = selectedIface, onValueChange = {}, readOnly = true, label = { Text("接口") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                uiState.availableInterfaces.forEach { iface -> DropdownMenuItem(text = { Text(iface) }, onClick = { selectedIface = iface; expanded = false }) }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = advertise, onCheckedChange = { advertise = it }); Spacer(Modifier.width(8.dp)); Text("广播") }
                        GlassTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "确定",
                        onClick = { if (address.isNotBlank()) viewModel.addAddress(address, selectedIface, advertise, comment.ifBlank { null }) }
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = { viewModel.hideAddDialog() },
                        primary = false
                    )
                })
        }
        if (uiState.showEditDialog && uiState.editingItem != null) {
            val item = uiState.editingItem!!
            var address by remember(item.id) { mutableStateOf(item.address) }
            var selectedIface by remember(item.id) { mutableStateOf(item.interface_) }
            var comment by remember(item.id) { mutableStateOf(item.comment) }
            var expanded by remember(item.id) { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { viewModel.hideEditDialog() }, title = { Text("编辑 IPv6 地址") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(value = address, onValueChange = { address = it }, label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            GlassTextField(value = selectedIface, onValueChange = {}, readOnly = true, label = { Text("接口") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                uiState.availableInterfaces.forEach { iface -> DropdownMenuItem(text = { Text(iface) }, onClick = { selectedIface = iface; expanded = false }) }
                            }
                        }
                        GlassTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "确定",
                        onClick = { viewModel.editAddress(uiState.editingId!!, mapOf("address" to address, "interface" to selectedIface, "comment" to comment)) }
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = { viewModel.hideEditDialog() },
                        primary = false
                    )
                })
        }
    }
}
