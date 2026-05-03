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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
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
import com.routeros.manager.ui.navigation.NetworkRoutes
import com.routeros.manager.ui.theme.DarkSurfaceVariant
import com.routeros.manager.ui.theme.OnDarkSurface
import com.routeros.manager.ui.theme.OnDarkSurfaceVariant
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.SecondaryPurple
import com.routeros.manager.ui.theme.StatusWarning

private data class NetworkQuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val badge: String,
    val onClick: () -> Unit
)

private data class NetworkSection(
    val title: String,
    val subtitle: String,
    val actions: List<NetworkQuickAction>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    navController: NavController
) {
    val sections = listOf(
        NetworkSection(
            title = "常用功能",
            subtitle = "最常访问的网络配置入口。",
            actions = listOf(
                NetworkQuickAction("IP 地址", "编辑接口地址与网络归属", Icons.Default.SettingsEthernet, PrimaryTeal, "编辑") {
                    navController.navigate(NetworkRoutes.IP_MANAGEMENT)
                },
                NetworkQuickAction("DNS 静态记录", "增删改、启停本地解析记录", Icons.Default.Dns, SecondaryPurple, "DNS") {
                    navController.navigate(NetworkRoutes.DNS_RECORDS)
                }
            )
        ),
        NetworkSection(
            title = "防火墙",
            subtitle = "管理 IPv4 与 IPv6 的连接、规则和地址列表。",
            actions = listOf(
                NetworkQuickAction("IPv4 防火墙", "查看连接、规则与地址列表", Icons.Default.Security, StatusWarning, "策略") {
                    navController.navigate(NetworkRoutes.IPV4_FIREWALL_HUB)
                },
                NetworkQuickAction("IPv6 防火墙", "单独管理 IPv6 规则与地址对象", Icons.Default.Language, PrimaryTeal, "独立") {
                    navController.navigate(NetworkRoutes.IPV6_FIREWALL_HUB)
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
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnDarkSurface
                )
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkSurfaceVariant
                )
            }
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
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(action.tint.copy(alpha = 0.16f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(action.icon, contentDescription = null, tint = action.tint)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = OnDarkSurface
                        )
                        QuickActionBadge(text = action.badge, tint = action.tint)
                    }
                    Text(
                        text = action.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnDarkSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(30.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = OnDarkSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionBadge(text: String, tint: Color) {
    Box(
        modifier = Modifier
            .background(color = tint.copy(alpha = 0.14f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}
