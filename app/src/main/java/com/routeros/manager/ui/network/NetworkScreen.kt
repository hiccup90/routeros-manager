package com.routeros.manager.ui.network

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import com.routeros.manager.ui.navigation.NetworkRoutes
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.SecondaryPurple
import com.routeros.manager.ui.theme.StatusInfo
import com.routeros.manager.ui.theme.StatusWarning

private data class NetworkQuickAction(
    val title: String,
    val headline: String,
    val supporting: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    navController: NavController
) {
    val quickActions = listOf(
        NetworkQuickAction(
            title = "防火墙",
            headline = "NAT 与 Filter 规则集中管理",
            supporting = "优先承接真实可编辑的防护、放行与端口转发能力",
            icon = Icons.Default.Security,
            tint = StatusWarning,
            onClick = { navController.navigate(NetworkRoutes.FIREWALL_HUB) }
        ),
        NetworkQuickAction(
            title = "地址分配",
            headline = "DHCP 服务器与租约",
            supporting = "服务器可启停，租约当前以查看为主，不夸大设备级配置能力",
            icon = Icons.Default.SettingsEthernet,
            tint = StatusInfo,
            onClick = { navController.navigate(NetworkRoutes.ADDRESS_ALLOCATION) }
        ),
        NetworkQuickAction(
            title = "IP 地址",
            headline = "IPv4 / IPv6 地址管理",
            supporting = "集中放置已支持真实增删改与启停的地址配置",
            icon = Icons.Default.Language,
            tint = PrimaryTeal,
            onClick = { navController.navigate(NetworkRoutes.IP_MANAGEMENT) }
        ),
        NetworkQuickAction(
            title = "更多网络设置",
            headline = "DNS 与后续网络能力入口",
            supporting = "DNS 静态记录可编辑；路由、Bridge、VLAN 保留为后续扩展入口",
            icon = Icons.Default.Dns,
            tint = SecondaryPurple,
            onClick = { navController.navigate(NetworkRoutes.ADVANCED) }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "网络",
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                SectionHint(
                    title = "网络配置控制中心",
                    subtitle = "首页只保留当前真正可进入并操作的配置模块；设备展示继续留在终端页。"
                )
            }

            items(quickActions.size) { index ->
                val action = quickActions[index]
                QuickActionCard(action = action)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionHint(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(action: NetworkQuickAction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = action.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        )
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
                        .size(46.dp)
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
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
