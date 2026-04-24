package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhcpLeaseListScreen(
    viewModel: DhcpLeaseListViewModel = hiltViewModel(),
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
                title = { Text("DHCP 租约") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("暂无 DHCP 租约", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.displayName, style = MaterialTheme.typography.bodyLarge)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                item.macAddress,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(if (item.isDynamic) "动态" else "静态") }
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            item.address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PrimaryTeal
                                        )
                                        if (item.status.isNotEmpty()) {
                                            Text(
                                                item.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (item.status == "bound") StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (item.server.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "服务器: ${item.server}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.expires.isNotEmpty()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "过期: ${item.expires}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.comment.isNotEmpty()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "备注: ${item.comment}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (item.isDynamic) {
                                            TextButton(onClick = { viewModel.showStaticBindingDialog(item) }) {
                                                Icon(Icons.Default.PushPin, contentDescription = null)
                                                Spacer(Modifier.height(0.dp))
                                                Text("静态绑定")
                                            }
                                        }
                                        TextButton(onClick = { viewModel.showEditDialog(item) }) {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                            Spacer(Modifier.height(0.dp))
                                            Text("编辑")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showStaticBindingDialog && uiState.staticBindingItem != null) {
            val item = uiState.staticBindingItem!!
            var comment by rememberSaveable(item.id) { mutableStateOf(item.comment) }
            var address by rememberSaveable(item.id) { mutableStateOf(item.address) }
            var server by rememberSaveable(item.id) { mutableStateOf(item.server) }

            AlertDialog(
                onDismissRequest = { viewModel.hideStaticBindingDialog() },
                title = { Text("静态绑定") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "先转为静态租约，再保存固定 IP / 服务器 / 备注。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("固定 IP") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = { Text("服务器") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("备注") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.saveStaticBinding(
                                id = item.id,
                                comment = comment,
                                address = address,
                                server = server
                            )
                        }
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideStaticBindingDialog() }) {
                        Text("取消")
                    }
                }
            )
        }

        if (uiState.showEditDialog && uiState.editingItem != null) {
            val item = uiState.editingItem!!
            var comment by rememberSaveable(item.id) { mutableStateOf(item.comment) }
            var address by rememberSaveable(item.id) { mutableStateOf(item.address) }
            var server by rememberSaveable(item.id) { mutableStateOf(item.server) }

            AlertDialog(
                onDismissRequest = { viewModel.hideEditDialog() },
                title = { Text("编辑 DHCP 租约") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("IP 地址") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = { Text("服务器") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("备注") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.editLease(
                                id = item.id,
                                comment = comment,
                                address = address,
                                server = server
                            )
                        }
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideEditDialog() }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
