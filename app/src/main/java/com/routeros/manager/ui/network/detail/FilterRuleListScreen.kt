package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
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
fun FilterRuleListScreen(
    viewModel: FilterRuleListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("过滤规则") },
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
                        Text("暂无过滤规则", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        val subtitle = remember(item.id, item.protocol, item.srcAddress, item.dstAddress) {
                            listOfNotNull(item.protocol.ifBlank { null }, item.srcAddress.ifBlank { null }, item.dstAddress.ifBlank { null }).joinToString(" | ")
                        }
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            onClick = { viewModel.showEditDialog(item) }) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${item.chain} → ${item.action}", style = MaterialTheme.typography.bodyLarge, color = if (item.disabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface)
                                    if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                text = { Text("确定要删除此过滤规则吗？") },
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
                            GlassTextField(value = chain, onValueChange = {}, readOnly = true, label = { Text("Chain") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(chainExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = chainExpanded, onDismissRequest = { chainExpanded = false }) {
                                listOf("input", "forward", "output").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { chain = it; chainExpanded = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = actionExpanded, onExpandedChange = { actionExpanded = it }) {
                            GlassTextField(value = action, onValueChange = {}, readOnly = true, label = { Text("Action") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(actionExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                listOf("accept", "drop", "reject", "add-src-to-address-list", "add-dst-to-address-list").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { action = it; actionExpanded = false }) }
                            }
                        }
                        GlassTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("协议（可选）") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = srcAddress, onValueChange = { srcAddress = it }, label = { Text("源地址（可选）") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = dstAddress, onValueChange = { dstAddress = it }, label = { Text("目标地址（可选）") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "确定",
                        onClick = { viewModel.addRule(chain, action, protocol, srcAddress, dstAddress, comment.ifBlank { null }) }
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
            var chain by remember(item.id) { mutableStateOf(item.chain) }
            var action by remember(item.id) { mutableStateOf(item.action) }
            var protocol by remember(item.id) { mutableStateOf(item.protocol) }
            var comment by remember(item.id) { mutableStateOf(item.comment) }
            var chainExpanded by remember(item.id) { mutableStateOf(false) }
            var actionExpanded by remember(item.id) { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { viewModel.hideEditDialog() }, title = { Text("编辑过滤规则") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = chainExpanded, onExpandedChange = { chainExpanded = it }) {
                            GlassTextField(value = chain, onValueChange = {}, readOnly = true, label = { Text("Chain") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(chainExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = chainExpanded, onDismissRequest = { chainExpanded = false }) {
                                listOf("input", "forward", "output").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { chain = it; chainExpanded = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = actionExpanded, onExpandedChange = { actionExpanded = it }) {
                            GlassTextField(value = action, onValueChange = {}, readOnly = true, label = { Text("Action") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(actionExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                listOf("accept", "drop", "reject", "add-src-to-address-list", "add-dst-to-address-list").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { action = it; actionExpanded = false }) }
                            }
                        }
                        GlassTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("协议") }, modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = comment, onValueChange = { comment = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "确定",
                        onClick = { viewModel.editRule(uiState.editingId!!, mapOf("chain" to chain, "action" to action, "protocol" to protocol, "comment" to comment)) }
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
