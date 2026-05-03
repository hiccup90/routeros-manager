package com.routeros.manager.ui.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.routeros.manager.ui.components.GlassCard
import com.routeros.manager.ui.components.GlassScaffold
import com.routeros.manager.ui.components.GlassTitleBar
import com.routeros.manager.ui.theme.DarkSurfaceElevated
import com.routeros.manager.ui.theme.DarkSurfaceVariant
import com.routeros.manager.ui.theme.OnDarkSurface
import com.routeros.manager.ui.theme.OnDarkSurfaceVariant
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.PrimaryTealLight
import com.routeros.manager.ui.theme.StatusError
import com.routeros.manager.ui.theme.StatusSuccess

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    GlassScaffold(
        topBar = { GlassTitleBar(title = uiState.routerName) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            val showEmpty = !uiState.isConnected && !uiState.isLoading
            if (showEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnDarkSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("未连接到路由器", style = MaterialTheme.typography.titleMedium, color = OnDarkSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("请在设置中配置连接信息", style = MaterialTheme.typography.bodyMedium, color = OnDarkSurfaceVariant.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        HomeSummaryCard(
                            routerName = uiState.routerName,
                            isConnected = uiState.isConnected,
                            interfaceCount = uiState.interfaces.size,
                            cpuLoad = uiState.cpuLoad,
                            memoryPercent = uiState.memoryPercent,
                            memoryUsed = uiState.memoryUsed,
                            memoryTotal = uiState.memoryTotal,
                            uptime = uiState.uptime,
                            version = uiState.version,
                            boardName = uiState.boardName
                        )
                    }

                    item {
                        HomeSectionHeader(
                            title = "网络接口",
                            subtitle = if (uiState.interfaces.isEmpty()) "暂无接口数据" else "实时查看接口状态和上下行速率"
                        )
                    }

                    if (uiState.interfaces.isEmpty() && !uiState.isLoading) {
                        item {
                            Text("暂无接口数据", style = MaterialTheme.typography.bodyMedium, color = OnDarkSurfaceVariant)
                        }
                    }

                    items(uiState.interfaces, key = { it.id.ifBlank { it.name } }) { iface ->
                        InterfaceCard(iface)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSummaryCard(
    routerName: String,
    isConnected: Boolean,
    interfaceCount: Int,
    cpuLoad: String,
    memoryPercent: Int,
    memoryUsed: Long,
    memoryTotal: Long,
    uptime: String,
    version: String,
    boardName: String
) {
    val cpuLoadPercent = remember(cpuLoad) { cpuLoad.toIntOrNull() ?: 0 }
    val memoryUsedText = remember(memoryUsed) { formatBytes(memoryUsed) }
    val memoryTotalText = remember(memoryTotal) { formatBytes(memoryTotal) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (routerName.isBlank()) "RouterOS" else routerName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnDarkSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeOverviewPill(
                            text = if (isConnected) "在线" else "离线",
                            tint = if (isConnected) StatusSuccess else StatusError
                        )
                        HomeOverviewPill(text = "$interfaceCount 个接口", tint = PrimaryTeal)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant
                ) {
                    Text(
                        text = version,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = OnDarkSurfaceVariant
                    )
                }
            }

            // CPU / Memory metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricPanel(
                    modifier = Modifier.weight(1f),
                    label = "CPU",
                    value = "$cpuLoad%",
                    progress = cpuLoadPercent / 100f,
                    accent = if (cpuLoadPercent > 80) StatusError else PrimaryTeal
                )
                StatusMetricPanel(
                    modifier = Modifier.weight(1f),
                    label = "内存",
                    value = "$memoryPercent%",
                    progress = memoryPercent / 100f,
                    accent = if (memoryPercent > 80) StatusError else PrimaryTeal,
                    supporting = "$memoryUsedText / $memoryTotalText"
                )
            }

            // Info tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeInfoTile(
                    modifier = Modifier.weight(1f),
                    label = "系统版本",
                    value = version
                )
                HomeInfoTile(
                    modifier = Modifier.weight(1f),
                    label = "设备型号",
                    value = boardName
                )
            }

            // Uptime
            HorizontalDivider(color = DarkSurfaceVariant, thickness = 0.5.dp)
            Text(text = "运行时间：$uptime", style = MaterialTheme.typography.bodyMedium, color = OnDarkSurfaceVariant)
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = OnDarkSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkSurfaceVariant
        )
    }
}

@Composable
private fun HomeInfoTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnDarkSurfaceVariant)
            Text(text = value.ifBlank { "--" }, style = MaterialTheme.typography.titleMedium, color = OnDarkSurface)
        }
    }
}

@Composable
private fun HomeOverviewPill(
    text: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .background(color = tint.copy(alpha = 0.14f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@Composable
private fun StatusMetricPanel(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    progress: Float,
    accent: androidx.compose.ui.graphics.Color,
    supporting: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnDarkSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = OnDarkSurface)
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accent,
                trackColor = DarkSurfaceVariant
            )
            supporting?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = OnDarkSurfaceVariant)
            }
        }
    }
}

@Composable
fun InterfaceCard(iface: HomeViewModel.InterfaceUiModel) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (iface.disabled) StatusError else StatusSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = iface.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (iface.disabled) OnDarkSurfaceVariant else OnDarkSurface
                        )
                    }
                    Text(
                        text = iface.ipv4Address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryTealLight
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(14.dp), tint = PrimaryTealLight)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(iface.rxRate, style = MaterialTheme.typography.bodySmall, color = PrimaryTealLight)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ArrowUpward, null, modifier = Modifier.size(14.dp), tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(iface.txRate, style = MaterialTheme.typography.bodySmall, color = PrimaryTeal)
                    }
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}
