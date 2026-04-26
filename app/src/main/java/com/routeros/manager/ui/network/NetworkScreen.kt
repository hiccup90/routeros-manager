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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Security
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
            title = "设备网络配置",
            headline = "从设备直接进入静态绑定与租约定位",
            supporting = "适合先找到 NAS、打印机、手机，再去改静态绑定和设备保留地址。",
            icon = Icons.Default.Devices,
            tint = PrimaryTeal,
            onClick = { navController.navigate("${NetworkRoutes.DHCP_LEASES_BASE}?query=") }
        ),
        NetworkQuickAction(
            title = "IPv4 防火墙",
            headline = "按 RouterOS 官方逻辑管理 NAT 与 Filter",
            supporting = "端口转发、源地址转换、IPv4 input / forward / output 过滤都放这里。",
            icon = Icons.Default.Security,
            tint = StatusWarning,
            onClick = { navController.navigate(NetworkRoutes.IPV4_FIREWALL_HUB) }
        ),
        NetworkQuickAction(
            title = "IPv6 防火墙",
            headline = "独立管理 IPv6 Filter 规则",
            supporting = "按 RouterOS 官方 IPv6 菜单拆开，避免和 IPv4 规则混在一起。",
            icon = Icons.Default.Security,
            tint = PrimaryTeal,
            onClick = { navController.navigate(NetworkRoutes.IPV6_FIREWALL_HUB) }
        ),
        NetworkQuickAction(
            title = "DNS 设置",
            headline = "查看 IP-DNS 服务器、缓存与解析能力",
            supporting = "对应 WinBox 的 IP-DNS；集中展示上游 DNS、缓存占用和静态记录入口。",
            icon = Icons.Default.Dns,
            tint = SecondaryPurple,
            onClick = { navController.navigate(NetworkRoutes.DNS_SETTINGS) }
        )
    )

    GlassScaffold(
        topBar = {
            GlassTitleBar(title = "网络")
        }
    ) { paddingValues ->
        Crossfade(targetState = quickActions, label = "network-actions") { actions ->
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

                items(
                    items = actions,
                    key = { action -> action.title }
                ) { action ->
                    QuickActionCard(action = action)
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(action: NetworkQuickAction) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = action.onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .animateGlassSize(),
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
                        .background(action.tint.copy(alpha = 0.16f), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        action.icon,
                        contentDescription = null,
                        tint = action.tint
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
