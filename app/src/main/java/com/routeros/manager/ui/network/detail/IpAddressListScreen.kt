package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.routeros.manager.data.api.IpAddress
import com.routeros.manager.ui.components.GlassButton
import com.routeros.manager.ui.components.GlassTextField
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.network.detail.IpAddressListViewModel.AddressItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpAddressListScreen(
    viewModel: IpAddressListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("IPv4 地址") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "添加")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    DetailListSkeleton()
                }
                uiState.error != null && uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        GlassButton(text = "重试", onClick = { viewModel.loadData() }, primary = false)
                    }
                }
                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("暂无 IP 地址", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            IpAddressListItem(
                                item = item,
                                onToggle = { viewModel.toggleEnable(item.id, item.disabled) },
                                onEdit = { viewModel.showEditDialog(item) },
                                onDelete = { viewModel.delete(item.id) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showAddDialog) {
            AddIpAddressDialog(
                interfaces = uiState.availableInterfaces,
                onDismiss = { viewModel.hideAddDialog() },
                onConfirm = { addr, iface, comment ->
                    viewModel.addAddress(addr, iface, comment)
                }
            )
        }

        if (uiState.showEditDialog && uiState.editingAddress != null) {
            EditIpAddressDialog(
                item = uiState.editingAddress!!,
                interfaces = uiState.availableInterfaces,
                onDismiss = { viewModel.hideEditDialog() },
                onConfirm = { updates -> viewModel.editAddress(uiState.editingId!!, updates) }
            )
        }
    }
}

@Composable
fun IpAddressListItem(
    item: AddressItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.disabled)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.interface_,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = !item.disabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.8f)
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此 IP 地址吗？") },
            confirmButton = {
                GlassButton(
                    text = "删除",
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    primary = false
                )
            },
            dismissButton = {
                GlassButton(
                    text = "取消",
                    onClick = { showDeleteDialog = false },
                    primary = false
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIpAddressDialog(
    interfaces: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var selectedInterface by remember {
        mutableStateOf(interfaces.firstOrNull() ?: "")
    }
    var comment by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加 IP 地址") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址（如 192.168.1.100/24）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    GlassTextField(
                        value = selectedInterface,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("接口") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        interfaces.forEach { iface ->
                            DropdownMenuItem(
                                text = { Text(iface) },
                                onClick = {
                                    selectedInterface = iface
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                GlassTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassButton(
                text = "确定",
                onClick = {
                    if (address.isNotBlank() && selectedInterface.isNotBlank()) {
                        onConfirm(address, selectedInterface, comment.ifBlank { null })
                    }
                }
            )
        },
        dismissButton = {
            GlassButton(text = "取消", onClick = onDismiss, primary = false)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIpAddressDialog(
    item: IpAddress,
    interfaces: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit
) {
    var address by remember { mutableStateOf(item.address) }
    var selectedInterface by remember { mutableStateOf(item.interface_) }
    var comment by remember { mutableStateOf(item.comment) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑 IP 地址") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    GlassTextField(
                        value = selectedInterface,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("接口") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        interfaces.forEach { iface ->
                            DropdownMenuItem(
                                text = { Text(iface) },
                                onClick = {
                                    selectedInterface = iface
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                GlassTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassButton(
                text = "确定",
                onClick = {
                    onConfirm(
                        mapOf(
                            "address" to address,
                            "interface" to selectedInterface,
                            "comment" to comment
                        )
                    )
                }
            )
        },
        dismissButton = {
            GlassButton(text = "取消", onClick = onDismiss, primary = false)
        }
    )
}
