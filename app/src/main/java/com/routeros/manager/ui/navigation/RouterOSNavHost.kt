package com.routeros.manager.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.routeros.manager.ui.home.HomeScreen
import com.routeros.manager.ui.network.NetworkScreen
import com.routeros.manager.ui.network.detail.AddressAllocationScreen
import com.routeros.manager.ui.network.detail.AdvancedNetworkScreen
import com.routeros.manager.ui.network.detail.DhcpClientListScreen
import com.routeros.manager.ui.network.detail.DhcpLeaseListScreen
import com.routeros.manager.ui.network.detail.DhcpNetworkListScreen
import com.routeros.manager.ui.network.detail.DhcpServerListScreen
import com.routeros.manager.ui.network.detail.DnsRecordListScreen
import com.routeros.manager.ui.network.detail.FilterRuleListScreen
import com.routeros.manager.ui.network.detail.FirewallAddressListScreen
import com.routeros.manager.ui.network.detail.FirewallConnectionListScreen
import com.routeros.manager.ui.network.detail.FirewallPlaceholderScreen
import com.routeros.manager.ui.network.detail.Ipv4FirewallHubScreen
import com.routeros.manager.ui.network.detail.Ipv6FilterRuleListScreen
import com.routeros.manager.ui.network.detail.Ipv6FirewallAddressListScreen
import com.routeros.manager.ui.network.detail.Ipv6FirewallConnectionListScreen
import com.routeros.manager.ui.network.detail.Ipv6FirewallHubScreen
import com.routeros.manager.ui.network.detail.IpAddressListScreen
import com.routeros.manager.ui.network.detail.IpManagementScreen
import com.routeros.manager.ui.network.detail.Ipv6AddressListScreen
import com.routeros.manager.ui.network.detail.NatRuleListScreen
import com.routeros.manager.ui.settings.SettingsScreen
import com.routeros.manager.ui.terminal.TerminalScreen

@Composable
fun RouterOSNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    val popBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.Terminal.route) {
            TerminalScreen(
                onOpenNetworkConfig = { query ->
                    navController.navigate("${NetworkRoutes.DHCP_LEASES_BASE}?query=${Uri.encode(query)}")
                }
            )
        }

        composable(Screen.Network.route) {
            NetworkScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(NetworkRoutes.IPV4_FIREWALL_HUB) {
            Ipv4FirewallHubScreen(
                onNavigateBack = popBack,
                onOpenFilterRules = { navController.navigate(NetworkRoutes.FILTER_RULES) },
                onOpenNatRules = { navController.navigate(NetworkRoutes.NAT_RULES) },
                onOpenMangle = { navController.navigate(NetworkRoutes.IPV4_MANGLE) },
                onOpenRaw = { navController.navigate(NetworkRoutes.IPV4_RAW) },
                onOpenServicePorts = { navController.navigate(NetworkRoutes.IPV4_SERVICE_PORTS) },
                onOpenConnections = { navController.navigate(NetworkRoutes.IPV4_CONNECTIONS) },
                onOpenAddressLists = { navController.navigate(NetworkRoutes.IPV4_ADDRESS_LISTS) },
                onOpenLayer7 = { navController.navigate(NetworkRoutes.IPV4_LAYER7) }
            )
        }

        composable(NetworkRoutes.IPV6_FIREWALL_HUB) {
            Ipv6FirewallHubScreen(
                onNavigateBack = popBack,
                onOpenFilterRules = { navController.navigate(NetworkRoutes.IPV6_FILTER_RULES) },
                onOpenMangle = { navController.navigate(NetworkRoutes.IPV6_MANGLE) },
                onOpenRaw = { navController.navigate(NetworkRoutes.IPV6_RAW) },
                onOpenConnections = { navController.navigate(NetworkRoutes.IPV6_CONNECTIONS) },
                onOpenAddressLists = { navController.navigate(NetworkRoutes.IPV6_ADDRESS_LISTS) }
            )
        }

        composable(NetworkRoutes.ADDRESS_ALLOCATION) {
            AddressAllocationScreen(
                onNavigateBack = popBack,
                onOpenDhcpServers = { navController.navigate(NetworkRoutes.DHCP_SERVERS) },
                onOpenDhcpNetworks = { navController.navigate(NetworkRoutes.DHCP_NETWORKS) },
                onOpenDhcpLeases = { navController.navigate("${NetworkRoutes.DHCP_LEASES_BASE}?query=") }
            )
        }

        composable(NetworkRoutes.IP_MANAGEMENT) {
            IpManagementScreen(
                onNavigateBack = popBack,
                onOpenIpv4Addresses = { navController.navigate(NetworkRoutes.IP_ADDRESSES) },
                onOpenIpv6Addresses = { navController.navigate(NetworkRoutes.IPV6_ADDRESSES) }
            )
        }

        composable(NetworkRoutes.IP_ADDRESSES) {
            IpAddressListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.IPV6_ADDRESSES) {
            Ipv6AddressListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.DHCP_CLIENTS) {
            DhcpClientListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.DHCP_SERVERS) {
            DhcpServerListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.DHCP_NETWORKS) {
            DhcpNetworkListScreen(onNavigateBack = popBack)
        }

        composable(
            route = NetworkRoutes.DHCP_LEASES,
            arguments = listOf(navArgument("query") { defaultValue = "" })
        ) { backStackEntry ->
            DhcpLeaseListScreen(
                onNavigateBack = popBack,
                initialQuery = backStackEntry.arguments?.getString("query").orEmpty()
            )
        }

        composable(NetworkRoutes.DNS_RECORDS) {
            DnsRecordListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.NAT_RULES) {
            NatRuleListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.ADVANCED) {
            AdvancedNetworkScreen(
                onNavigateBack = popBack,
                onOpenDns = { navController.navigate(NetworkRoutes.DNS_RECORDS) }
            )
        }

        composable(NetworkRoutes.FILTER_RULES) {
            FilterRuleListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.IPV6_FILTER_RULES) {
            Ipv6FilterRuleListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.IPV4_MANGLE) {
            FirewallPlaceholderScreen("IPv4 Mangle", "这里将补 RouterOS IPv4 mangle 规则：连接标记、路由标记、报文标记。", popBack)
        }

        composable(NetworkRoutes.IPV4_RAW) {
            FirewallPlaceholderScreen("IPv4 Raw", "这里将补 RouterOS IPv4 raw 规则：在连接跟踪前做快速匹配、drop、notrack。", popBack)
        }

        composable(NetworkRoutes.IPV4_SERVICE_PORTS) {
            FirewallPlaceholderScreen("IPv4 Service Ports", "这里将补 RouterOS service-port / ALG 开关管理。", popBack)
        }

        composable(NetworkRoutes.IPV4_CONNECTIONS) {
            FirewallConnectionListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.IPV4_ADDRESS_LISTS) {
            FirewallAddressListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.IPV4_LAYER7) {
            FirewallPlaceholderScreen("IPv4 Layer7", "这里将补 RouterOS Layer7 协议匹配规则管理。", popBack)
        }

        composable(NetworkRoutes.IPV6_MANGLE) {
            FirewallPlaceholderScreen("IPv6 Mangle", "这里将补 RouterOS IPv6 mangle 规则：连接标记、路由标记、报文标记。", popBack)
        }

        composable(NetworkRoutes.IPV6_RAW) {
            FirewallPlaceholderScreen("IPv6 Raw", "这里将补 RouterOS IPv6 raw 规则：在连接跟踪前做快速匹配、drop、notrack。", popBack)
        }

        composable(NetworkRoutes.IPV6_CONNECTIONS) {
            Ipv6FirewallConnectionListScreen(onNavigateBack = popBack)
        }

        composable(NetworkRoutes.IPV6_ADDRESS_LISTS) {
            Ipv6FirewallAddressListScreen(onNavigateBack = popBack)
        }
    }
}
