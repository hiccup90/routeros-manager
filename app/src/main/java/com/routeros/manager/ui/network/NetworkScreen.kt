package com.routeros.manager.ui.network

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
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
    val headline: String,
    val supporting: String,
    val icon: ImageVector,
    val tint: Color,
    val badge: String,
    val onClick: () -> Unit
)

private data class NetworkSection(
    val title: String,
    val description: String,
    val eyebrow: String,
    val accent: Color,
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
            description = "把最常用的设备定位、地址分配和 DNS 能力前置，减少层级跳转。",
            eyebrow = "FAST LANE",
            accent = PrimaryTeal,
            actions = listOf(
                NetworkQuickAction(
                    title = "设备网络配置",
                    headline = "从设备直接进入静态绑定与租约定位",
                    supporting = "适合先找到 NAS、打印机、手机，再去改静态绑定和设备保留地址。",
                    icon = Icons.Default.Devices,
                    tint = PrimaryTeal,
                    badge = "高频",
                    onClick = { navController.navigate("${NetworkRoutes.DHCP_LEASES_BASE}?query=") }
                ),
                NetworkQuickAction(
                    title = "地址分配",
                    headline = "集中处理 DHCP 服务器、网络和租约",
                    supporting = "把网段参数、服务器启停、设备级绑定统一收纳在一个入口。",
                    icon = Icons.Default.Hub,
                    tint = SecondaryPurple,
                    badge = "推荐",
                    onClick = { navController.navigate(NetworkRoutes.ADDRESS_ALLOCATION) }
                ),
                NetworkQuickAction(
                    title = "DNS 设置",
                    headline = "查看 IP-DNS 服务器、缓存与解析能力",
                    supporting = "对应 WinBox 的 IP-DNS；集中展示上游 DNS、缓存占用和静态记录入口。",
                    icon = Icons.Default.Dns,
                    tint = SecondaryPurple,
                    badge = "常用",
                    onClick = { navController.navigate(NetworkRoutes.DNS_SETTINGS) }
                )
            )
        ),
        NetworkSection(
            title = "地址与防火墙",
            description = "保留真实可用的 IPv4 / IPv6 地址与安全策略入口。",
            eyebrow = "SECURITY",
            accent = StatusWarning,
            actions = listOf(
                NetworkQuickAction(
                    title = "IP 地址",
                    headline = "进入 IPv4 / IPv6 地址管理",
                    supporting = "查看并编辑接口上的地址项，适合做网段调整与排障。",
                    icon = Icons.Default.SettingsEthernet,
                    tint = PrimaryTeal,
                    badge = "编辑",
                    onClick = { navController.navigate(NetworkRoutes.IP_MANAGEMENT) }
                ),
                NetworkQuickAction(
                    title = "IPv4 防火墙",
                    headline = "按 RouterOS 官方逻辑管理 NAT 与 Filter",
                    supporting = "端口转发、源地址转换、IPv4 input / forward / output 过滤都放这里。",
                    icon = Icons.Default.Security,
                    tint = StatusWarning,
                    badge = "策略",
                    onClick = { navController.navigate(NetworkRoutes.IPV4_FIREWALL_HUB) }
                ),
                NetworkQuickAction(
                    title = "IPv6 防火墙",
                    headline = "独立管理 IPv6 Filter 规则",
                    supporting = "按 RouterOS 官方 IPv6 菜单拆开，避免和 IPv4 规则混在一起。",
                    icon = Icons.Default.Language,
                    tint = PrimaryTeal,
                    badge = "独立",
                    onClick = { navController.navigate(NetworkRoutes.IPV6_FIREWALL_HUB) }
                )
            )
        ),
        NetworkSection(
            title = "更多网络设置",
            description = "收纳低频但已可用的入口，保持首页干净而不失可达性。",
            eyebrow = "EXTENDED",
            accent = SecondaryPurple,
            actions = listOf(
                NetworkQuickAction(
                    title = "更多网络设置",
                    headline = "进入 DNS 静态记录等低频入口",
                    supporting = "保持首页高频优先，把低频但已可用的能力单独收纳。",
                    icon = Icons.Default.ChevronRight,
                    tint = SecondaryPurple,
                    badge = "低频",
                    onClick = { navController.navigate(NetworkRoutes.ADVANCED_NETWORK) }
                )
            )
        )
    )

    GlassScaffold(
        topBar = {
            GlassTitleBar(title = "网络")
        }
    ) { paddingValues ->
        Crossfade(targetState = sections, label = "network-sections") { currentSections ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                item {
                    NetworkOverviewCard(totalEntries = currentSections.sumOf { it.actions.size })
                }

                items(
                    items = currentSections,
                    key = { section -> section.title }
                ) { section ->
                    NetworkSectionCard(section = section)
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun NetworkOverviewCard(totalEntries: Int) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .animateGlassSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "NETWORK CONTROL",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryTeal.copy(alpha = 0.9f)
            )
            Text(
                text = "把高频操作放到前排，让网络管理更像精炼控制台，而不是菜单堆叠。",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "当前保留 $totalEntries 个真实入口，优先覆盖设备定位、地址分配、DNS 与防火墙。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewPill(text = "高频优先", tint = PrimaryTeal)
                OverviewPill(text = "真实入口", tint = SecondaryPurple)
                OverviewPill(text = "低频收纳", tint = StatusWarning)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "优先从“设备网络配置”进入，再按场景分流到 DHCP、DNS 与防火墙。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverviewPill(
    text: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = tint.copy(alpha = 0.14f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = section.eyebrow,
                            style = MaterialTheme.typography.labelSmall,
                            color = section.accent.copy(alpha = 0.92f)
                        )
                        QuickActionBadge(text = "${section.actions.size} 项", tint = section.accent)
                    }
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = section.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 2.dp)
                        .size(width = 10.dp, height = 54.dp)
                        .background(
                            color = section.accent.copy(alpha = 0.30f),
                            shape = MaterialTheme.shapes.large
                        )
                )
            }

            section.actions.forEach { action ->
                QuickActionRow(action = action)
            }
        }
    }
}

@Composable
private fun QuickActionRow(action: NetworkQuickAction) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = action.onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(action.tint.copy(alpha = 0.16f), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        action.icon,
                        contentDescription = null,
                        tint = action.tint
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        QuickActionBadge(text = action.badge, tint = action.tint)
                    }
                    Text(
                        text = action.headline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = action.tint
                    )
                    Text(
                        text = action.supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionBadge(
    text: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = tint.copy(alpha = 0.14f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}
