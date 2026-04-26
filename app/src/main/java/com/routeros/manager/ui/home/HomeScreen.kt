package com.routeros.manager.ui.home

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.routeros.manager.ui.components.animateGlassSize
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.PrimaryTealLight
import com.routeros.manager.ui.theme.SecondaryPurple
import com.routeros.manager.ui.theme.StatusError
import com.routeros.manager.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.ensurePollingState()
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    GlassScaffold(
        topBar = {
            GlassTitleBar(title = uiState.routerName)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Crossfade(targetState = !uiState.isConnected && !uiState.isLoading, label = "home-content") { showEmpty ->
                if (showEmpty) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.WifiOff, contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "未连接到路由器",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "请在设置中配置连接信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            HomeOverviewCard(
                                routerName = uiState.routerName,
                                isConnected = uiState.isConnected,
                                interfaceCount = uiState.interfaces.size
                            )
                        }

                        item {
                            SystemStatusCard(
                                cpuLoad = uiState.cpuLoad,
                                memoryPercent = uiState.memoryPercent,
                                memoryUsed = uiState.memoryUsed,
                                memoryTotal = uiState.memoryTotal,
                                uptime = uiState.uptime,
                                version = uiState.version
                            )
                        }


                        if (uiState.interfaces.isEmpty() && !uiState.isLoading) {
                            item {
                                Text(
                                    "暂无接口数据",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
}

@Composable
private fun HomeOverviewCard(
    routerName: String,
    isConnected: Boolean,
    interfaceCount: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .animateGlassSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SYSTEM OVERVIEW",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryTeal
            )
            Text(
                text = if (routerName.isBlank()) "RouterOS 控制台已就绪" else routerName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isConnected) {
                    "当前链路稳定，建议先查看 CPU / 内存，再观察高频接口速率。"
                } else {
                    "连接已中断，请先检查 REST 地址、凭据或证书配置。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeOverviewPill(
                    text = if (isConnected) "连接正常" else "连接中断",
                    tint = if (isConnected) StatusSuccess else StatusError
                )
                HomeOverviewPill(text = "$interfaceCount 个接口", tint = SecondaryPurple)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "HEALTH",
                    value = if (isConnected) "ONLINE" else "OFFLINE",
                    accent = if (isConnected) StatusSuccess else StatusError
                )
                OverviewMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "INTERFACES",
                    value = interfaceCount.toString(),
                    accent = SecondaryPurple
                )
            }
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
            .background(
                color = tint.copy(alpha = 0.14f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}

@Composable
fun SystemStatusCard(
    cpuLoad: String,
    memoryPercent: Int,
    memoryUsed: Long,
    memoryTotal: Long,
    uptime: String,
    version: String
) {
    val cpuLoadPercent = remember(cpuLoad) { cpuLoad.toIntOrNull() ?: 0 }
    val memoryUsedText = remember(memoryUsed) { formatBytes(memoryUsed) }
    val memoryTotalText = remember(memoryTotal) { formatBytes(memoryTotal) }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .animateGlassSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SYSTEM HEALTH",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryTeal
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    label = "MEMORY",
                    value = "$memoryPercent%",
                    progress = memoryPercent / 100f,
                    accent = if (memoryPercent > 80) StatusError else SecondaryPurple,
                    supporting = "$memoryUsedText / $memoryTotalText"
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeOverviewPill(text = uptime, tint = PrimaryTealLight)
                HomeOverviewPill(text = version, tint = SecondaryPurple)
            }
        }
    }
}

@Composable
private fun OverviewMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent
            )
        }
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InterfaceCard(iface: HomeViewModel.InterfaceUiModel) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .animateGlassSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (iface.disabled) MaterialTheme.colorScheme.error
                                else StatusSuccess
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        iface.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (iface.disabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowDownward, null,
                        modifier = Modifier.size(14.dp),
                        tint = PrimaryTealLight
                    )
                    Text(iface.rxRate, style = MaterialTheme.typography.bodySmall, color = PrimaryTealLight)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowUpward, null,
                        modifier = Modifier.size(14.dp),
                        tint = SecondaryPurple
                    )
                    Text(iface.txRate, style = MaterialTheme.typography.bodySmall, color = SecondaryPurple)
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
