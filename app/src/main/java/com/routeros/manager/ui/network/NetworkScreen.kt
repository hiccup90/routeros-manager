package com.routeros.manager.ui.network

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.routeros.manager.ui.components.GlassCard
import com.routeros.manager.ui.components.GlassScaffold
import com.routeros.manager.ui.components.GlassTitleBar
import com.routeros.manager.ui.components.animateGlassSize
import com.routeros.manager.ui.navigation.NetworkRoutes
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.SecondaryPurple
import com.routeros.manager.ui.theme.StatusWarning

private data class NetworkQuickAction(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val badge: String,
    val onClick: () -> Unit
)

private data class NetworkSection(
    val title: String,
    val actions: List<NetworkQuickAction>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    navController: NavController
) {
    val sections = listOf(
        NetworkSection(
            title = "高频操作",
            actions = listOf(
                NetworkQuickAction("设备网络配置", Icons.Default.Devices, PrimaryTeal, "高频") {
                    navController.navigate("${NetworkRoutes.DHCP_LEASES_BASE}?query=")
                },
                NetworkQuickAction("地址分配", Icons.Default.Hub, SecondaryPurple, "常用") {
                    navController.navigate(NetworkRoutes.ADDRESS_ALLOCATION)
                },
                NetworkQuickAction("DNS 设置", Icons.Default.Dns, SecondaryPurple, "常用") {
                    navController.navigate(NetworkRoutes.DNS_SETTINGS)
                }
            )
        ),
        NetworkSection(
            title = "地址与防火墙",
            actions = listOf(
                NetworkQuickAction("IP 地址", Icons.Default.SettingsEthernet, PrimaryTeal, "编辑") {
                    navController.navigate(NetworkRoutes.IP_MANAGEMENT)
                },
                NetworkQuickAction("IPv4 防火墙", Icons.Default.Security, StatusWarning, "策略") {
                    navController.navigate(NetworkRoutes.IPV4_FIREWALL_HUB)
                },
                NetworkQuickAction("IPv6 防火墙", Icons.Default.Language, PrimaryTeal, "独立") {
                    navController.navigate(NetworkRoutes.IPV6_FIREWALL_HUB)
                }
            )
        ),
        NetworkSection(
            title = "更多网络设置",
            actions = listOf(
                NetworkQuickAction("更多网络设置", Icons.Default.ChevronRight, SecondaryPurple, "更多") {
                    navController.navigate(NetworkRoutes.ADVANCED_NETWORK)
                }
            )
        )
    )

    GlassScaffold(topBar = { GlassTitleBar(title = "网络") }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            items(items = sections, key = { it.title }) { section ->
                NetworkSectionCard(section)
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun NetworkSectionCard(section: NetworkSection) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .animateGlassSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = section.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            section.actions.forEach { action ->
                QuickActionRow(action)
            }
        }
    }
}

@Composable
private fun QuickActionRow(action: NetworkQuickAction) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = action.onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(action.tint.copy(alpha = 0.16f), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(action.icon, contentDescription = null, tint = action.tint)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = action.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        QuickActionBadge(text = action.badge, tint = action.tint)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(34.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuickActionBadge(text: String, tint: Color) {
    Box(
        modifier = Modifier
            .background(color = tint.copy(alpha = 0.14f), shape = MaterialTheme.shapes.large)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
