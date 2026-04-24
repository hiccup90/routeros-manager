package com.routeros.manager.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.routeros.manager.ui.home.HomeScreen
import com.routeros.manager.ui.network.NetworkScreen
import com.routeros.manager.ui.network.detail.AdvancedNetworkScreen
import com.routeros.manager.ui.network.detail.DhcpClientListScreen
import com.routeros.manager.ui.network.detail.DhcpLeaseListScreen
import com.routeros.manager.ui.network.detail.DhcpServerListScreen
import com.routeros.manager.ui.network.detail.DnsRecordListScreen
import com.routeros.manager.ui.network.detail.FilterRuleListScreen
import com.routeros.manager.ui.network.detail.IpAddressListScreen
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
            TerminalScreen()
        }

        composable(Screen.Network.route) {
            NetworkScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
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

        composable(NetworkRoutes.DHCP_LEASES) {
            DhcpLeaseListScreen(onNavigateBack = popBack)
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
                onOpenDns = { navController.navigate(NetworkRoutes.DNS_RECORDS) },
                onOpenFilterRules = { navController.navigate(NetworkRoutes.FILTER_RULES) },
                onOpenIpv6 = { navController.navigate(NetworkRoutes.IPV6_ADDRESSES) }
            )
        }

        composable(NetworkRoutes.FILTER_RULES) {
            FilterRuleListScreen(onNavigateBack = popBack)
        }
    }
}
