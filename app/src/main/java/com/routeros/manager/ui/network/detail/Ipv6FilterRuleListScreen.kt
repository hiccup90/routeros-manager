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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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
fun Ipv6FilterRuleListScreen(
    viewModel: Ipv6FilterRuleListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("IPv6 Filter") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = { IconButton(onClick = { viewModel.loadData() }) { Icon(Icons.Default.Refresh, "刷新") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, "添加")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.items.isEmpty() -> Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("暂无 IPv6 Filter 规则", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        var showDelete by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            onClick = { viewModel.showEditDialog(item) }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${item.chain} → ${item.action}", style = MaterialTheme.typography.bodyLarge)
                                    val subtitle = listOfNotNull(item.srcAddress.ifBlank { null }, item.dstAddress.ifBlank { null }).joinToString(" | ")
                                    if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.comment.isNotEmpty()) Text(item.comment, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(checked = !item.disabled, onCheckedChange = { viewModel.toggleEnable(item.id, item.disabled) }, modifier = Modifier.scale(0.8f))
                                    IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                        if (showDelete) {
                            AlertDialog(
                                onDismissRequest = { showDelete = false },
                                title = { Text("确认删除") },
                                text = { Text("确定要删除此 IPv6 Filter 规则吗？") },
                                confirmButton = { TextButton(onClick = { showDelete = false; viewModel.delete(item.id) }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showAddDialog) {
            var chain by remember { mutableStateOf("input") }
            var action by remember { mutableStateOf("accept") }
            var srcAddress by remember { mutableStateOf("") }
            var dstAddress by remember { mutableStateOf("") }
            var comment by remember { mutableStateOf("") }
            var chainExpanded by remember { mutableStateOf(false) }
            var actionExpanded by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { viewModel.hideAddDialog() },
                title = { Text("添加 IPv6 Filter 规则") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = chainExpanded, onExpandedChange = { chainExpanded = it }) {
                            OutlinedTextField(value = chain, onValueChange = {}, readOnly = true, label = { Text("Chain") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(chainExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = chainExpanded, onDismissRequest = { chainExpanded = false }) {
                                listOf("input", "forward", "output").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { chain = it; chainExpanded = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = actionExpanded, onExpandedChange = { actionExpanded = it }) {
                            OutlinedTextField(value = action, onValueChange = {}, readOnly = true, label = { Text("Action") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(actionExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                listOf("accept", "drop", "reject", "add-src-to-address-list", "add-dst-to-address-list").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { action = it; actionExpanded = false }) }
                            }
                        }
                        OutlinedTextField(value = srcAddress, onValueChange = { srcAddress = it }, label = { Text("源地址（可选）") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = dstAddress, onValueChange = { dstAddress = it }, label = { Text("目标地址（可选）") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = { viewModel.addRule(chain, action, srcAddress, dstAddress, comment.ifBlank { null }) }) { Text("确定") } },
                dismissButton = { TextButton(onClick = { viewModel.hideAddDialog() }) { Text("取消") } }
            )
        }

        if (uiState.showEditDialog && uiState.editingItem != null) {
            val item = uiState.editingItem!!
            var chain by remember { mutableStateOf(item.chain) }
            var action by remember { mutableStateOf(item.action) }
            var srcAddress by remember { mutableStateOf(item.srcAddress) }
            var dstAddress by remember { mutableStateOf(item.dstAddress) }
            var comment by remember { mutableStateOf(item.comment) }
            var chainExpanded by remember { mutableStateOf(false) }
            var actionExpanded by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { viewModel.hideEditDialog() },
                title = { Text("编辑 IPv6 Filter 规则") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = chainExpanded, onExpandedChange = { chainExpanded = it }) {
                            OutlinedTextField(value = chain, onValueChange = {}, readOnly = true, label = { Text("Chain") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(chainExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = chainExpanded, onDismissRequest = { chainExpanded = false }) {
                                listOf("input", "forward", "output").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { chain = it; chainExpanded = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = actionExpanded, onExpandedChange = { actionExpanded = it }) {
                            OutlinedTextField(value = action, onValueChange = {}, readOnly = true, label = { Text("Action") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(actionExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                listOf("accept", "drop", "reject", "add-src-to-address-list", "add-dst-to-address-list").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { action = it; actionExpanded = false }) }
                            }
                        }
                        OutlinedTextField(value = srcAddress, onValueChange = { srcAddress = it }, label = { Text("源地址") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = dstAddress, onValueChange = { dstAddress = it }, label = { Text("目标地址") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = { viewModel.editRule(uiState.editingId!!, mapOf("chain" to chain, "action" to action, "src-address" to srcAddress, "dst-address" to dstAddress, "comment" to comment)) }) { Text("确定") } },
                dismissButton = { TextButton(onClick = { viewModel.hideEditDialog() }) { Text("取消") } }
            )
        }
    }
}
