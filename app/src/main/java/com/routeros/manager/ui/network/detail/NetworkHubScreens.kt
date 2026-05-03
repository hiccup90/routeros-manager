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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.routeros.manager.ui.components.GlassCard
import com.routeros.manager.ui.components.GlassScaffold
import com.routeros.manager.ui.components.animateGlassSize
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.SecondaryPurple
import com.routeros.manager.ui.theme.StatusInfo
import com.routeros.manager.ui.theme.StatusWarning

private data class HubEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
fun Ipv4FirewallHubScreen(
    onNavigateBack: () -> Unit,
    onOpenFilterRules: () -> Unit,
    onOpenNatRules: () -> Unit,
    onOpenMangle: () -> Unit,
    onOpenConnections: () -> Unit,
    onOpenAddressLists: () -> Unit
) {
    HubScreen(
        title = "IPv4 防火墙",
        description = "保留高频 IPv4 防火墙能力：Filter、NAT、Address Lists、Mangle、连接统计。",
        onNavigateBack = onNavigateBack,
        entries = listOf(
            HubEntry("Filter", "增删改、启停 IPv4 访问控制规则", Icons.Outlined.Security, StatusWarning, onOpenFilterRules),
            HubEntry("NAT", "端口转发、源地址转换与重定向", Icons.Outlined.SwapHoriz, PrimaryTeal, onOpenNatRules),
            HubEntry("Address Lists", "维护防火墙地址列表", Icons.Outlined.Dns, StatusWarning, onOpenAddressLists),
            HubEntry("Mangle", "连接标记、路由标记、报文标记", Icons.Outlined.Hub, SecondaryPurple, onOpenMangle),
            HubEntry("连接统计", "查看总连接数、TCP / UDP、已建立连接", Icons.AutoMirrored.Filled.List, SecondaryPurple, onOpenConnections)
        )
    )
}

@Composable
fun Ipv6FirewallHubScreen(
    onNavigateBack: () -> Unit,
    onOpenFilterRules: () -> Unit,
    onOpenMangle: () -> Unit,
    onOpenConnections: () -> Unit,
    onOpenAddressLists: () -> Unit
) {
    HubScreen(
        title = "IPv6 防火墙",
        description = "保留高频 IPv6 防火墙能力：Filter、Address Lists、Mangle、连接统计。",
        onNavigateBack = onNavigateBack,
        entries = listOf(
            HubEntry("Filter", "增删改、启停 IPv6 input / forward / output 规则", Icons.Outlined.Security, PrimaryTeal, onOpenFilterRules),
            HubEntry("Address Lists", "维护 IPv6 防火墙地址列表", Icons.Outlined.Dns, StatusWarning, onOpenAddressLists),
            HubEntry("Mangle", "IPv6 报文与连接标记规则", Icons.Outlined.Hub, SecondaryPurple, onOpenMangle),
            HubEntry("连接统计", "查看总连接数、TCP / UDP、已建立连接", Icons.AutoMirrored.Filled.List, SecondaryPurple, onOpenConnections)
        )
    )
}

@Composable
fun AddressAllocationScreen(
    onNavigateBack: () -> Unit,
    onOpenDhcpServers: () -> Unit,
    onOpenDhcpNetworks: () -> Unit,
    onOpenDhcpLeases: () -> Unit
) {
    HubScreen(
        title = "地址分配",
        description = "这里聚焦 DHCP 地址分配相关内容：服务器可启停，网络项负责下发网关 / DNS，租约页负责设备级静态绑定。",
        onNavigateBack = onNavigateBack,
        entries = listOf(
            HubEntry(
                title = "DHCP 租约",
                subtitle = "按设备查找地址、执行静态绑定与备注维护",
                icon = Icons.AutoMirrored.Filled.List,
                tint = PrimaryTeal,
                onClick = onOpenDhcpLeases
            ),
            HubEntry(
                title = "DHCP 网络",
                subtitle = "编辑网段下发的网关与 DNS 参数",
                icon = Icons.Outlined.Dns,
                tint = SecondaryPurple,
                onClick = onOpenDhcpNetworks
            ),
            HubEntry(
                title = "DHCP 服务器",
                subtitle = "查看服务端配置并执行启用 / 停用",
                icon = Icons.Outlined.SettingsEthernet,
                tint = StatusInfo,
                onClick = onOpenDhcpServers
            )
        )
    )
}

@Composable
fun IpManagementScreen(
    onNavigateBack: () -> Unit,
    onOpenIpv4Addresses: () -> Unit,
    onOpenIpv6Addresses: () -> Unit
) {
    HubScreen(
        title = "IP 地址",
        description = "集中管理已具备真实编辑能力的 IPv4 / IPv6 地址配置。",
        onNavigateBack = onNavigateBack,
        entries = listOf(
            HubEntry(
                title = "IPv4 地址",
                subtitle = "增删改、启停 IPv4 地址项",
                icon = Icons.Outlined.SettingsEthernet,
                tint = PrimaryTeal,
                onClick = onOpenIpv4Addresses
            ),
            HubEntry(
                title = "IPv6 地址",
                subtitle = "增删改、启停 IPv6 地址项",
                icon = Icons.Outlined.Language,
                tint = StatusInfo,
                onClick = onOpenIpv6Addresses
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedNetworkScreen(
    onNavigateBack: () -> Unit,
    onOpenDns: () -> Unit
) {
    HubScreen(
        title = "更多网络设置",
        description = "收纳低频但已可用的入口；当前提供 DNS 静态记录，其他 RouterOS 网络模块后续再补。",
        onNavigateBack = onNavigateBack,
        entries = listOf(
            HubEntry(
                title = "DNS 静态记录",
                subtitle = "增删改、启停本地解析记录",
                icon = Icons.Outlined.Dns,
                tint = PrimaryTeal,
                onClick = onOpenDns
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HubScreen(
    title: String,
    description: String,
    onNavigateBack: () -> Unit,
    entries: List<HubEntry>
) {
    GlassScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = description,
                        modifier = Modifier
                            .padding(20.dp)
                            .animateGlassSize(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(
                items = entries,
                key = { entry -> entry.title }
            ) { entry ->
                HubEntryCard(item = entry)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HubEntryCard(item: HubEntry) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = item.onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(item.tint.copy(alpha = 0.16f), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.tint
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
