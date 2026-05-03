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
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.routeros.manager.ui.components.GlassButton
import com.routeros.manager.ui.components.GlassCard
import com.routeros.manager.ui.components.GlassScaffold
import com.routeros.manager.ui.components.GlassTextField
import com.routeros.manager.ui.components.GlassTitleBar
import com.routeros.manager.ui.theme.DarkSurfaceElevated
import com.routeros.manager.ui.theme.DarkSurfaceVariant
import com.routeros.manager.ui.theme.OnDarkSurface
import com.routeros.manager.ui.theme.OnDarkSurfaceVariant
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.PrimaryTealLight
import com.routeros.manager.ui.theme.StatusError
import com.routeros.manager.ui.theme.StatusSuccess
import com.routeros.manager.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel = hiltViewModel(),
    onOpenNetworkConfig: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val expandedMap = rememberSaveable(
        saver = androidx.compose.runtime.saveable.mapSaver(
            save = { state -> state.toMap() },
            restore = { restored -> mutableStateMapOf<String, Boolean>().apply { restored.forEach { (key, value) -> this[key] = value as Boolean } } }
        )
    ) {
        mutableStateMapOf<String, Boolean>()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.ensurePollingState()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopPolling()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    GlassScaffold(
        topBar = {
            GlassTitleBar(
                title = "设备",
                trailing = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryCard(
                deviceCount = uiState.devices.size,
                query = uiState.query,
                isRefreshing = uiState.isRefreshing,
                showOnlineOnly = uiState.showOnlineOnly,
                onlineCount = uiState.onlineCount,
                totalCount = uiState.totalCount,
                onToggleOnlineOnly = { viewModel.setShowOnlineOnly(!uiState.showOnlineOnly) },
                modifier = Modifier.fillMaxWidth()
            )

            GlassTextField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateQuery("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "清空搜索")
                        }
                    }
                },
                placeholder = { Text("按设备名 / IP / MAC / 接口搜索") },
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    !uiState.isConfigured -> {
                        EmptyState(
                            title = "未配置连接",
                            message = uiState.error ?: "请先在设置中填写 RouterOS 地址、用户名和密码"
                        )
                    }

                    uiState.isLoading && uiState.devices.isEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(3) {
                                TerminalSkeletonCard()
                            }
                        }
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
                            contentPadding = PaddingValues(bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                uiState.error?.let {
                                    Text(
                                        text = it,
                                        color = StatusError,
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
                                    onToggle = {
                                        val nextExpanded = !expanded
                                        expandedMap[device.key] = nextExpanded
                                        viewModel.setDeviceExpanded(device.key, nextExpanded)
                                    },
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
    showOnlineOnly: Boolean,
    onlineCount: Int,
    totalCount: Int,
    onToggleOnlineOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Outlined.Devices,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("设备总览", style = MaterialTheme.typography.titleLarge, color = OnDarkSurface)
                }
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = PrimaryTeal)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Devices,
                    tint = PrimaryTeal,
                    label = "设备数",
                    value = if (query.isBlank()) "$deviceCount 台" else "$deviceCount 台匹配"
                )
                SummaryMetricTile(
                    modifier = Modifier.weight(1f),
                    icon = if (showOnlineOnly) Icons.Outlined.CheckCircle else Icons.Outlined.List,
                    tint = if (showOnlineOnly) StatusSuccess else PrimaryTeal,
                    label = if (showOnlineOnly) "仅在线" else "全部",
                    value = if (showOnlineOnly) "$onlineCount / $totalCount" else "$totalCount 台",
                    onClick = onToggleOnlineOnly
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tint.copy(alpha = 0.14f)
            ) {
                Box(
                    modifier = Modifier.padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnDarkSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OnDarkSurface)
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
    val accent = statusColor(device.status)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = device.primaryAddress,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnDarkSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (device.interfaceDisplay != "--") {
                                DeviceMetaPill(text = device.interfaceDisplay, tint = PrimaryTeal)
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = if (expanded) "收起" else "展开",
                                tint = OnDarkSurfaceVariant
                            )
                        }
                    }
                    val hasRealName = device.displayName != device.primaryAddress && device.displayName != "未知设备"
                    Text(
                        text = if (hasRealName) device.displayName else if (device.macAddress != "--") device.macAddress else "--",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryTealLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = DarkSurfaceVariant, thickness = 0.5.dp)
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DetailLine("DHCP", device.status)
                                DetailLine("MAC", device.macAddress)
                                DetailLine("IPv6", device.ipv6Display)
                                DetailLine("接口", device.interfaceDisplay)
                            }
                        }
                        TrafficSection(device = device)
                        GlassButton(
                            text = "打开网络配置",
                            onClick = {
                                val query = listOf(device.primaryAddress, device.macAddress, device.displayName)
                                    .firstOrNull { it.isNotBlank() && it != "--" }
                                    .orEmpty()
                                onOpenNetworkConfig(query)
                            },
                            primary = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrafficSection(device: TerminalDeviceUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("设备下载", style = MaterialTheme.typography.labelSmall, color = OnDarkSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(device.downloadRate, style = MaterialTheme.typography.bodyMedium, color = PrimaryTealLight, fontWeight = FontWeight.Medium)
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("设备上传", style = MaterialTheme.typography.labelSmall, color = OnDarkSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(device.uploadRate, style = MaterialTheme.typography.bodyMedium, color = PrimaryTeal, fontWeight = FontWeight.Medium)
                }
            }
        }
        when {
            device.isTrafficLoading -> {
                Text(
                    text = if (device.trafficLoaded) "正在刷新设备流量..." else "正在加载设备流量...",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkSurfaceVariant
                )
            }
            device.trafficError != null -> {
                Text(
                    text = device.trafficError,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusError
                )
            }
        }
    }
}

@Composable
private fun DeviceMetaPill(text: String, tint: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = tint.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TerminalSkeletonCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Box(modifier = Modifier.size(28.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Box(modifier = Modifier.width(72.dp).height(18.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSurfaceElevated
                        ) {
                            Box(modifier = Modifier.width(138.dp).height(14.dp))
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceElevated
                ) {
                    Box(modifier = Modifier.width(46.dp).height(24.dp))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant
                ) {
                    Box(modifier = Modifier.height(54.dp))
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated
                ) {
                    Box(modifier = Modifier.height(54.dp))
                }
            }
        }
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
            imageVector = Icons.Outlined.Router,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = OnDarkSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = OnDarkSurface)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkSurfaceVariant
        )
    }
}

private fun statusColor(status: String) = when {
    status.contains("bound", ignoreCase = true) || status.contains("complete", ignoreCase = true) -> StatusSuccess
    status.contains("incomplete", ignoreCase = true) || status.contains("failed", ignoreCase = true) -> StatusError
    status.contains("waiting", ignoreCase = true) || status.contains("offer", ignoreCase = true) -> StatusWarning
    else -> PrimaryTeal
}
