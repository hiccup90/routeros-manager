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
import com.routeros.manager.ui.network.detail.FirewallHubScreen
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

        composable(NetworkRoutes.FIREWALL_HUB) {
            FirewallHubScreen(
                onNavigateBack = popBack,
                onOpenNatRules = { navController.navigate(NetworkRoutes.NAT_RULES) },
                onOpenFilterRules = { navController.navigate(NetworkRoutes.FILTER_RULES) }
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
    }
}
