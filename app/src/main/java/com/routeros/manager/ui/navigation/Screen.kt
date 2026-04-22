package com.routeros.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Terminal : Screen(
        route = "terminal",
        title = "终端",
        selectedIcon = Icons.Filled.Computer,
        unselectedIcon = Icons.Outlined.Computer
    )

    data object Network : Screen(
        route = "network",
        title = "网络",
        selectedIcon = Icons.Filled.Lan,
        unselectedIcon = Icons.Outlined.Lan
    )

    data object Settings : Screen(
        route = "settings",
        title = "设置",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavItems = listOf(Home, Terminal, Network, Settings)
    }
}

// Network sub-screens
object NetworkRoutes {
    const val IP_ADDRESSES = "network/ip_addresses"
    const val IPV6_ADDRESSES = "network/ipv6_addresses"
    const val DHCP_CLIENTS = "network/dhcp_clients"
    const val DHCP_SERVERS = "network/dhcp_servers"
    const val DHCP_LEASES = "network/dhcp_leases"
    const val DNS_RECORDS = "network/dns_records"
    const val NAT_RULES = "network/nat_rules"
    const val FILTER_RULES = "network/filter_rules"
}
