package com.routeros.manager.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.PrimaryTealLight
import com.routeros.manager.ui.theme.SecondaryPurple
import com.routeros.manager.ui.theme.StatusError
import com.routeros.manager.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel = hiltViewModel(),
    onOpenNetworkConfig: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("终端") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                deviceCount = uiState.devices.size,
                query = uiState.query,
                isRefreshing = uiState.isRefreshing,
                lastUpdatedAt = uiState.lastUpdatedAt
            )

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清空搜索")
                        }
                    }
                },
                placeholder = { Text("按设备名 / IP / MAC / 接口搜索") }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    !uiState.isConfigured -> {
                        EmptyState(
                            title = "未配置连接",
                            message = uiState.error ?: "请先在设置中填写 RouterOS 地址、用户名和密码"
                        )
                    }

                    uiState.devices.isEmpty() -> {
                        EmptyState(
                            title = if (uiState.query.isBlank()) "暂无设备" else "未找到匹配设备",
                            message = uiState.error ?: if (uiState.query.isBlank()) {
                                "当前列表基于 DHCP、ARP、IPv6 邻居和接口流量数据"
                            } else {
                                "请尝试更换搜索关键词"
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                uiState.error?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                            items(uiState.devices, key = { it.key }) { device ->
                                val expanded = expandedMap[device.key] ?: false
                                DeviceCard(
                                    device = device,
                                    expanded = expanded,
                                    onToggle = { expandedMap[device.key] = !expanded },
                                    onOpenNetworkConfig = onOpenNetworkConfig
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    deviceCount: Int,
    query: String,
    isRefreshing: Boolean,
    lastUpdatedAt: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = PrimaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("在线设备", style = MaterialTheme.typography.titleMedium)
                }
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            }
            Text(
                text = if (query.isBlank()) "共 $deviceCount 台设备" else "筛选后 $deviceCount 台设备",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "数据源：DHCP / ARP / IPv6 邻居 / 接口字节差分",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            lastUpdatedAt?.let {
                Text(
                    text = "更新于 ${formatTimestamp(it)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: TerminalDeviceUiModel,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenNetworkConfig: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor(device.status))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = device.primaryAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryTealLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailLine("MAC", device.macAddress)
                        DetailLine("IPv6", device.ipv6Display)
                        DetailLine("接口", device.interfaceDisplay)
                        TrafficLine(rxRate = device.rxRate, txRate = device.txRate)
                        if (device.hostname.isNotBlank() && device.hostname != device.displayName) {
                            DetailLine("主机名", device.hostname)
                        }
                        if (device.inferredName.isNotBlank() && device.inferredName != device.displayName) {
                            DetailLine("推测名称", device.inferredName)
                        }
                        if (device.expires.isNotBlank()) {
                            DetailLine("租约", device.expires)
                        }
                        if (device.lastSeen.isNotBlank()) {
                            DetailLine("最近出现", device.lastSeen)
                        }
                        if (device.comment.isNotBlank()) {
                            DetailLine("备注", device.comment)
                        }
                        Button(
                            onClick = {
                                val query = listOf(device.primaryAddress, device.macAddress, device.displayName)
                                    .firstOrNull { it.isNotBlank() && it != "--" }
                                    .orEmpty()
                                onOpenNetworkConfig(query)
                            }
                        ) {
                            Text("网络配置")
                        }
                        SourceChips(device.sources)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrafficLine(rxRate: String, txRate: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = PrimaryTeal.copy(alpha = 0.12f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("下载", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(rxRate, style = MaterialTheme.typography.bodyMedium, color = PrimaryTealLight, fontWeight = FontWeight.Medium)
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = SecondaryPurple.copy(alpha = 0.12f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("上传", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(txRate, style = MaterialTheme.typography.bodyMedium, color = SecondaryPurple, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SourceChips(sources: List<String>) {
    if (sources.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        sources.take(3).forEach { source ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            ) {
                Text(
                    text = source,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Router,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun statusColor(status: String) = when {
    status.contains("bound", ignoreCase = true) || status.contains("complete", ignoreCase = true) -> StatusSuccess
    status.contains("incomplete", ignoreCase = true) || status.contains("failed", ignoreCase = true) -> StatusError
    else -> PrimaryTeal
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
