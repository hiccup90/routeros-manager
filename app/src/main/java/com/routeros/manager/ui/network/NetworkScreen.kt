package com.routeros.manager.ui.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.routeros.manager.ui.navigation.NetworkRoutes
import com.routeros.manager.ui.theme.PrimaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    navController: NavController,
    viewModel: NetworkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "网络配置",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // IP 地址分组
            item {
                GroupCard(
                    title = "IP 地址",
                    icon = Icons.Default.Language,
                    expanded = uiState.expandedSection == "ip",
                    onToggle = { viewModel.toggleSection("ip") },
                    items = listOf(
                        GroupItem("IPv4 地址", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.IP_ADDRESSES)
                        },
                        GroupItem("IPv6 地址", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.IPV6_ADDRESSES)
                        }
                    )
                )
            }

            // DHCP 分组
            item {
                GroupCard(
                    title = "DHCP",
                    icon = Icons.Default.Cloud,
                    expanded = uiState.expandedSection == "dhcp",
                    onToggle = { viewModel.toggleSection("dhcp") },
                    items = listOf(
                        GroupItem("DHCP 客户端", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.DHCP_CLIENTS)
                        },
                        GroupItem("DHCP 服务器", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.DHCP_SERVERS)
                        },
                        GroupItem("租约列表", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.DHCP_LEASES)
                        }
                    )
                )
            }

            // DNS 分组
            item {
                GroupCard(
                    title = "DNS",
                    icon = Icons.Default.Dns,
                    expanded = uiState.expandedSection == "dns",
                    onToggle = { viewModel.toggleSection("dns") },
                    items = listOf(
                        GroupItem("静态记录", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.DNS_RECORDS)
                        }
                    )
                )
            }

            // 防火墙分组
            item {
                GroupCard(
                    title = "防火墙",
                    icon = Icons.Default.Security,
                    expanded = uiState.expandedSection == "firewall",
                    onToggle = { viewModel.toggleSection("firewall") },
                    items = listOf(
                        GroupItem("NAT 规则", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.NAT_RULES)
                        },
                        GroupItem("过滤规则", Icons.Default.ExpandMore) {
                            navController.navigate(NetworkRoutes.FILTER_RULES)
                        }
                    )
                )
            }
        }
    }
}

data class GroupItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun GroupCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    items: List<GroupItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Column {
            // 头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 展开子项
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = item.onClick)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}