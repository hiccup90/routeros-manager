package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.routeros.manager.data.api.FirewallFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRuleListScreen(
    viewModel: FilterRuleListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("过滤规则") },
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
                uiState.isLoading && uiState.items.isEmpty() ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.items.isEmpty() ->
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("暂无过滤规则", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        var showDelete by remember { mutableStateOf(false) }
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            onClick = { viewModel.showEditDialog(item) }) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${item.chain} → ${item.action}", style = MaterialTheme.typography.bodyLarge, color = if (item.disabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface)
                                    val subtitle = listOfNotNull(item.protocol.ifBlank { null }, item.srcAddress.ifBlank { null }, item.dstAddress.ifBlank { null }).joinToString(" | ")
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
                            AlertDialog(onDismissRequest = { showDelete = false }, title = { Text("确认删除") }, text = { Text("确定要删除此过滤规则吗？") },
                                confirmButton = { TextButton(onClick = { showDelete = false; viewModel.delete(item.id) }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } })
                        }
                    }
                }
            }
        }
        if (uiState.showAddDialog) {
            var chain by remember { mutableStateOf("input") }
            var action by remember { mutableStateOf("accept") }
            var protocol by remember { mutableStateOf("") }
            var srcAddress by remember { mutableStateOf("") }
            var dstAddress by remember { mutableStateOf("") }
            var comment by remember { mutableStateOf("") }
            var chainExpanded by remember { mutableStateOf(false) }
            var actionExpanded by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { viewModel.hideAddDialog() }, title = { Text("添加过滤规则") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = chainExpanded, onExpandedChange = { chainExpanded = it }) {
                            OutlinedTextField(value = chain, onValueChange = {}, readOnly = true, label = { Text("Chain") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(chainExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = chainExpanded, onDismissRequest = { chainExpanded = false }) {
                                listOf("input", "forward", "output").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { chain = it; chainExpanded = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = actionExpanded, onExpandedChange = { actionExpanded = it }) {
                            OutlinedTextField(value = action, onValueChange = {}, readOnly = true, label = { Text("Action") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(actionExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                listOf("accept", "drop", "reject", "add-src-to-address-list", "add-dst-to-address-list").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { action = it; actionExpanded = false }) }
                            }
                        }
                        OutlinedTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("协议（可选）") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = srcAddress, onValueChange = { srcAddress = it }, label = { Text("源地址（可选）") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = dstAddress, onValueChange = { dstAddress = it }, label = { Text("目标地址（可选）") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = { viewModel.addRule(chain, action, protocol, srcAddress, dstAddress, comment.ifBlank { null }) }) { Text("确定") } },
                dismissButton = { TextButton(onClick = { viewModel.hideAddDialog() }) { Text("取消") } })
        }
        if (uiState.showEditDialog && uiState.editingItem != null) {
            val item = uiState.editingItem!!
            var chain by remember { mutableStateOf(item.chain) }
            var action by remember { mutableStateOf(item.action) }
            var protocol by remember { mutableStateOf(item.protocol) }
            var comment by remember { mutableStateOf(item.comment) }
            var chainExpanded by remember { mutableStateOf(false) }
            var actionExpanded by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { viewModel.hideEditDialog() }, title = { Text("编辑过滤规则") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = chainExpanded, onExpandedChange = { chainExpanded = it }) {
                            OutlinedTextField(value = chain, onValueChange = {}, readOnly = true, label = { Text("Chain") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(chainExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = chainExpanded, onDismissRequest = { chainExpanded = false }) {
                                listOf("input", "forward", "output").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { chain = it; chainExpanded = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = actionExpanded, onExpandedChange = { actionExpanded = it }) {
                            OutlinedTextField(value = action, onValueChange = {}, readOnly = true, label = { Text("Action") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(actionExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                listOf("accept", "drop", "reject", "add-src-to-address-list", "add-dst-to-address-list").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { action = it; actionExpanded = false }) }
                            }
                        }
                        OutlinedTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("协议") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = { viewModel.editRule(uiState.editingId!!, mapOf("chain" to chain, "action" to action, "protocol" to protocol, "comment" to comment)) }) { Text("确定") } },
                dismissButton = { TextButton(onClick = { viewModel.hideEditDialog() }) { Text("取消") } })
        }
    }
}
