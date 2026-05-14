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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
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
import com.routeros.manager.ui.components.GlassButton
import com.routeros.manager.ui.components.GlassFilterChip
import com.routeros.manager.ui.components.GlassTextField
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhcpLeaseListScreen(
    viewModel: DhcpLeaseListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    initialQuery: String = ""
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            viewModel.updateQuery(initialQuery)
        }
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
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        GlassTextField(
                            value = uiState.query,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            singleLine = true,
                            readOnly = true,
                            label = { Text("筛选设备 / IP / MAC") }
                        )
                        DetailListSkeleton(itemCount = 4)
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        GlassTextField(
                            value = uiState.query,
                            onValueChange = viewModel::updateQuery,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            singleLine = true,
                            label = { Text("筛选设备 / IP / MAC") }
                        )
                        if (uiState.filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("没有匹配的设备或租约", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                        items(uiState.filteredItems, key = { it.id }) { item ->
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
                                        GlassFilterChip(
                                            selected = !item.isDynamic,
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
                                    if (item.addressList.isNotEmpty()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "Address List: ${item.addressList}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.dhcpOption.isNotEmpty()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "DHCP Options: ${item.dhcpOption}",
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
                                            GlassButton(
                                                text = "静态绑定",
                                                onClick = { viewModel.showStaticBindingDialog(item) },
                                                primary = false,
                                                leadingIcon = Icons.Outlined.PushPin
                                            )
                                        }
                                        GlassButton(
                                            text = "编辑",
                                            onClick = { viewModel.showEditDialog(item) },
                                            primary = false,
                                            leadingIcon = Icons.Outlined.Edit
                                        )
                                    }
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
            var addressList by rememberSaveable(item.id) { mutableStateOf(item.addressList) }
            var dhcpOption by rememberSaveable(item.id) { mutableStateOf(item.dhcpOption) }
            var gateway by rememberSaveable(item.id) { mutableStateOf(uiState.staticBindingGateway) }
            var dnsServer by rememberSaveable(item.id) { mutableStateOf(uiState.staticBindingDnsServer) }

            AlertDialog(
                onDismissRequest = { viewModel.hideStaticBindingDialog() },
                title = { Text("静态绑定") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "先转为静态租约，再保存固定 IP / 服务器 / 备注；网关和 DNS 来自匹配到的 DHCP 网络。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        GlassTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("固定 IP") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = { Text("服务器") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = gateway,
                            onValueChange = { gateway = it },
                            label = { Text("网关") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = dnsServer,
                            onValueChange = { dnsServer = it },
                            label = { Text("DNS 服务器") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = addressList,
                            onValueChange = { addressList = it },
                            label = { Text("Address List") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = dhcpOption,
                            onValueChange = { dhcpOption = it },
                            label = { Text("DHCP Options") },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                        text = "保存",
                        onClick = {
                            viewModel.saveStaticBinding(
                                id = item.id,
                                comment = comment,
                                address = address,
                                server = server,
                                addressList = addressList,
                                dhcpOption = dhcpOption,
                                gateway = gateway,
                                dnsServer = dnsServer
                            )
                        }
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = "取消",
                        onClick = { viewModel.hideStaticBindingDialog() },
                        primary = false
                    )
                }
            )
        }

        if (uiState.showEditDialog && uiState.editingItem != null) {
            val item = uiState.editingItem!!
            var comment by rememberSaveable(item.id) { mutableStateOf(item.comment) }
            var address by rememberSaveable(item.id) { mutableStateOf(item.address) }
            var server by rememberSaveable(item.id) { mutableStateOf(item.server) }
            var addressList by rememberSaveable(item.id) { mutableStateOf(item.addressList) }
            var dhcpOption by rememberSaveable(item.id) { mutableStateOf(item.dhcpOption) }

            AlertDialog(
                onDismissRequest = { viewModel.hideEditDialog() },
                title = { Text("编辑 DHCP 租约") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("IP 地址") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = { Text("服务器") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = addressList,
                            onValueChange = { addressList = it },
                            label = { Text("Address List") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        GlassTextField(
                            value = dhcpOption,
                            onValueChange = { dhcpOption = it },
                            label = { Text("DHCP Options") },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                        text = "保存",
                        onClick = {
                            viewModel.editLease(
                                id = item.id,
                                comment = comment,
                                address = address,
                                server = server,
                                addressList = addressList,
                                dhcpOption = dhcpOption
                            )
                        }
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
