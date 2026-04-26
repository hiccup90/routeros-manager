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
    const val IPV4_FIREWALL_HUB = "network/firewall/ipv4"
    const val IPV6_FIREWALL_HUB = "network/firewall/ipv6"
    const val ADDRESS_ALLOCATION = "network/address_allocation"
    const val IP_MANAGEMENT = "network/ip_management"
    const val IP_ADDRESSES = "network/ip_addresses"
    const val IPV6_ADDRESSES = "network/ipv6_addresses"
    const val DHCP_CLIENTS = "network/dhcp_clients"
    const val DHCP_SERVERS = "network/dhcp_servers"
    const val DHCP_NETWORKS = "network/dhcp_networks"
    const val DHCP_LEASES = "network/dhcp_leases?query={query}"
    const val DHCP_LEASES_BASE = "network/dhcp_leases"
    const val DNS_SETTINGS = "network/dns_settings"
    const val DNS_RECORDS = "network/dns_records"
    const val ADVANCED_NETWORK = "network/advanced"
    const val NAT_RULES = "network/nat_rules"
    const val FILTER_RULES = "network/filter_rules"
    const val IPV6_FILTER_RULES = "network/ipv6_filter_rules"
    const val IPV4_MANGLE = "network/ipv4_mangle"
    const val IPV4_RAW = "network/ipv4_raw"
    const val IPV4_SERVICE_PORTS = "network/ipv4_service_ports"
    const val IPV4_CONNECTIONS = "network/ipv4_connections"
    const val IPV4_ADDRESS_LISTS = "network/ipv4_address_lists"
    const val IPV4_LAYER7 = "network/ipv4_layer7"
    const val IPV6_MANGLE = "network/ipv6_mangle"
    const val IPV6_RAW = "network/ipv6_raw"
    const val IPV6_CONNECTIONS = "network/ipv6_connections"
    const val IPV6_ADDRESS_LISTS = "network/ipv6_address_lists"
}
