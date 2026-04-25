package com.routeros.manager.data.repository

import com.routeros.manager.data.api.AddIpAddressRequest
import com.routeros.manager.data.api.AddIpv6AddressRequest
import com.routeros.manager.data.api.ArpEntry
import com.routeros.manager.data.api.DhcpClient
import com.routeros.manager.data.api.DhcpLease
import com.routeros.manager.data.api.DhcpLeaseMakeStaticRequest
import com.routeros.manager.data.api.DhcpNetwork
import com.routeros.manager.data.api.DhcpServer
import com.routeros.manager.data.api.DnsRecord
import com.routeros.manager.data.api.DnsRecordRequest
import com.routeros.manager.data.api.FilterRuleRequest
import com.routeros.manager.data.api.FirewallAddressList
import com.routeros.manager.data.api.FirewallAddressListRequest
import com.routeros.manager.data.api.FirewallConnection
import com.routeros.manager.data.api.FirewallFilter
import com.routeros.manager.data.api.IdRequest
import com.routeros.manager.data.api.InterfaceItem
import com.routeros.manager.data.api.IpAddress
import com.routeros.manager.data.api.IpDnsSettings
import com.routeros.manager.data.api.Ipv6Address
import com.routeros.manager.data.api.Ipv6DhcpClient
import com.routeros.manager.data.api.Ipv6DhcpServer
import com.routeros.manager.data.api.Ipv6FirewallAddressList
import com.routeros.manager.data.api.Ipv6FirewallFilter
import com.routeros.manager.data.api.Ipv6Neighbor
import com.routeros.manager.data.api.MonitorTraffic
import com.routeros.manager.data.api.MonitorTrafficRequest
import com.routeros.manager.data.api.NatRule
import com.routeros.manager.data.api.NatRuleRequest
import com.routeros.manager.data.api.NetworkClient
import com.routeros.manager.data.api.NetworkDevice
import com.routeros.manager.data.api.PrintRequest
import com.routeros.manager.data.api.RouterOSApi
import com.routeros.manager.data.api.SystemIdentity
import com.routeros.manager.data.api.SystemResource
import com.routeros.manager.data.preferences.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class RouterOSRepository @Inject constructor(
    private val networkClient: NetworkClient,
    private val securePreferences: SecurePreferences
) {
    private val api: RouterOSApi
        get() = networkClient.getApi()

    fun isConfigured(): Boolean =
        securePreferences.host.isNotEmpty() && securePreferences.username.isNotEmpty()

    // ===== 系统 =====
    suspend fun getSystemIdentity(): Result<SystemIdentity> = runCatching {
        val list = api.getSystemIdentity()
        val map = list.firstOrNull() ?: emptyMap()
        SystemIdentity(
            name = map["name"] ?: "",
            id = map[".id"] ?: ""
        )
    }

    suspend fun getSystemResource(): Result<SystemResource> = runCatching {
        val list = api.getSystemResource()
        val map = list.firstOrNull() ?: emptyMap()
        SystemResource(
            uptime = map["uptime"] ?: "",
            version = map["version"] ?: "",
            cpuLoad = map["cpu-load"] ?: "",
            freeMemory = map["free-memory"] ?: "",
            totalMemory = map["total-memory"] ?: "",
            freeHddSpace = map["free-hdd-space"] ?: "",
            totalHddSpace = map["total-hdd-space"] ?: "",
            boardName = map["board-name"] ?: "",
            model = map["model"] ?: "",
            serialNumber = map["serial-number"] ?: ""
        )
    }

    // ===== 接口 =====
    suspend fun getInterfaces(props: List<String>? = null): Result<List<InterfaceItem>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getInterfaces(request).map { map ->
            InterfaceItem(
                id = map[".id"] ?: "",
                name = map["name"] ?: "",
                type = map["type"] ?: "",
                disabled = map["disabled"] ?: "false",
                rxByte = map["rx-byte"] ?: "0",
                txByte = map["tx-byte"] ?: "0",
                rxPacket = map["rx-packet"] ?: "0",
                txPacket = map["tx-packet"] ?: "0",
                speed = map["speed"] ?: "",
                macAddress = map["mac-address"] ?: "",
                comment = map["comment"] ?: ""
            )
        }
    }

    suspend fun monitorTraffic(numbers: String, interval: String = "00:00:01"): Result<List<MonitorTraffic>> = runCatching {
        val request = MonitorTrafficRequest(numbers = numbers, interval = interval)
        api.monitorTraffic(request).map { map ->
            MonitorTraffic(
                rxBitsPerSecond = map["rx-bits-per-second"] ?: "0",
                txBitsPerSecond = map["tx-bits-per-second"] ?: "0",
                rxBytes = map["rx-bytes"] ?: "0",
                txBytes = map["tx-bytes"] ?: "0"
            )
        }
    }

    // ===== IP 地址 =====
    suspend fun getIpAddresses(props: List<String>? = null): Result<List<IpAddress>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpAddresses(request).map { IpAddress.fromMap(it) }
    }

    suspend fun addIpAddress(address: String, interface_: String, comment: String? = null): Result<IpAddress> = runCatching {
        val req = AddIpAddressRequest(address, interface_, comment)
        val result = api.addIpAddress(req.toMap())
        IpAddress.fromMap(result.firstOrNull() ?: emptyMap())
    }

    suspend fun editIpAddress(id: String, updates: Map<String, String>): Result<IpAddress> = runCatching {
        val result = api.editIpAddress(id, updates)
        IpAddress.fromMap(result.firstOrNull() ?: emptyMap())
    }

    suspend fun deleteIpAddress(id: String): Result<Unit> = runCatching {
        api.deleteIpAddress(id)
    }

    // ===== ARP =====
    suspend fun getArpEntries(props: List<String>? = null): Result<List<ArpEntry>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getArpEntries(request).map { ArpEntry.fromMap(it) }
    }

    // ===== DHCP Lease =====
    suspend fun getDhcpLeases(props: List<String>? = null): Result<List<DhcpLease>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getDhcpLeases(request).map { map ->
            DhcpLease(
                id = map[".id"] ?: "",
                address = map["address"] ?: "",
                macAddress = map["mac-address"] ?: "",
                activeHostName = map["active-host-name"] ?: map["active-hostname"] ?: "",
                hostname = map["host-name"] ?: map["hostname"] ?: "",
                status = map["status"] ?: "",
                server = map["server"] ?: "",
                addressList = map["address-list"] ?: "",
                dhcpOption = map["dhcp-option"] ?: "",
                expires = map["expires"] ?: "",
                lastSeen = map["last-seen"] ?: "",
                comment = map["comment"] ?: "",
                clientId = map["client-id"] ?: "",
                radius = map["radius"] ?: "",
                dynamic = map["dynamic"] ?: "false"
            )
        }
    }

    suspend fun makeDhcpLeaseStatic(id: String): Result<Unit> = runCatching {
        api.makeDhcpLeaseStatic(DhcpLeaseMakeStaticRequest(numbers = id))
    }

    suspend fun editDhcpLease(id: String, updates: Map<String, String>): Result<DhcpLease> = runCatching {
        val result = api.editDhcpLease(id, updates)
        val map = result.firstOrNull() ?: emptyMap()
        DhcpLease(
            id = map[".id"] ?: id,
            address = map["address"] ?: updates["address"] ?: "",
            macAddress = map["mac-address"] ?: "",
            activeHostName = map["active-host-name"] ?: map["active-hostname"] ?: "",
            hostname = map["host-name"] ?: map["hostname"] ?: "",
            status = map["status"] ?: "",
            server = map["server"] ?: updates["server"] ?: "",
            addressList = map["address-list"] ?: updates["address-list"] ?: "",
            dhcpOption = map["dhcp-option"] ?: updates["dhcp-option"] ?: "",
            expires = map["expires"] ?: "",
            lastSeen = map["last-seen"] ?: "",
            comment = map["comment"] ?: updates["comment"] ?: "",
            clientId = map["client-id"] ?: "",
            radius = map["radius"] ?: "",
            dynamic = map["dynamic"] ?: "false"
        )
    }

    // ===== DHCP Client =====
    suspend fun getDhcpClients(props: List<String>? = null): Result<List<DhcpClient>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getDhcpClients(request).map { DhcpClient.fromMap(it) }
    }

    suspend fun disableDhcpClient(id: String): Result<Unit> = runCatching { api.disableDhcpClient(IdRequest(id)) }
    suspend fun enableDhcpClient(id: String): Result<Unit> = runCatching { api.enableDhcpClient(IdRequest(id)) }

    // ===== DHCP Server =====
    suspend fun getDhcpServers(props: List<String>? = null): Result<List<DhcpServer>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getDhcpServers(request).map { DhcpServer.fromMap(it) }
    }

    suspend fun getDhcpNetworks(props: List<String>? = null): Result<List<DhcpNetwork>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getDhcpNetworks(request).map { DhcpNetwork.fromMap(it) }
    }

    suspend fun disableDhcpServer(id: String): Result<Unit> = runCatching { api.disableDhcpServer(IdRequest(id)) }
    suspend fun enableDhcpServer(id: String): Result<Unit> = runCatching { api.enableDhcpServer(IdRequest(id)) }

    suspend fun editDhcpNetwork(id: String, updates: Map<String, String>): Result<DhcpNetwork> = runCatching {
        val result = api.editDhcpNetwork(id, updates)
        val map = result.firstOrNull() ?: emptyMap()
        DhcpNetwork(
            id = map[".id"] ?: id,
            address = map["address"] ?: "",
            gateway = map["gateway"] ?: updates["gateway"] ?: "",
            dnsServer = map["dns-server"] ?: updates["dns-server"] ?: "",
            comment = map["comment"] ?: updates["comment"] ?: ""
        )
    }

    // ===== DNS =====
    suspend fun getIpDnsSettings(props: List<String>? = null): Result<IpDnsSettings> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        val map = api.getIpDnsSettings(request).firstOrNull().orEmpty()
        IpDnsSettings(
            servers = map["servers"] ?: "",
            dynamicServers = map["dynamic-servers"] ?: "",
            allowRemoteRequests = map["allow-remote-requests"] ?: "false",
            cacheSize = map["cache-size"] ?: "",
            cacheUsed = map["cache-used"] ?: "",
            maxConcurrentQueries = map["max-concurrent-queries"] ?: "",
            maxConcurrentTcpSessions = map["max-concurrent-tcp-sessions"] ?: ""
        )
    }

    suspend fun getDnsRecords(props: List<String>? = null): Result<List<DnsRecord>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getDnsRecords(request).map { map ->
            DnsRecord(
                id = map[".id"] ?: "",
                name = map["name"] ?: "",
                address = map["address"] ?: "",
                ttl = map["ttl"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }

    suspend fun addDnsRecord(name: String, address: String, ttl: String? = null, comment: String? = null): Result<DnsRecord> = runCatching {
        val req = DnsRecordRequest(name, address, ttl, comment)
        api.addDnsRecord(req.toMap())
        DnsRecord(
            id = "",
            name = name,
            address = address,
            ttl = ttl ?: "",
            disabled = "false",
            comment = comment ?: ""
        )
    }

    suspend fun editDnsRecord(id: String, updates: Map<String, String>): Result<DnsRecord> = runCatching {
        api.editDnsRecord(id, updates)
        DnsRecord(
            id = id,
            name = updates["name"] ?: "",
            address = updates["address"] ?: "",
            ttl = updates["ttl"] ?: "",
            disabled = updates["disabled"] ?: "false",
            comment = updates["comment"] ?: ""
        )
    }

    suspend fun deleteDnsRecord(id: String): Result<Unit> = runCatching { api.deleteDnsRecord(id) }

    // ===== NAT =====
    suspend fun getNatRules(props: List<String>? = null): Result<List<NatRule>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getNatRules(request).map { map ->
            NatRule(
                id = map[".id"] ?: "",
                chain = map["chain"] ?: "",
                action = map["action"] ?: "",
                srcAddress = map["src-address"] ?: "",
                dstAddress = map["dst-address"] ?: "",
                protocol = map["protocol"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: "",
                srcnatAddress = map["srcnat-address"] ?: "",
                dstnatAddress = map["dstnat-address"] ?: ""
            )
        }
    }

    suspend fun disableNatRule(id: String): Result<Unit> = runCatching { api.disableNatRule(IdRequest(id)) }
    suspend fun enableNatRule(id: String): Result<Unit> = runCatching { api.enableNatRule(IdRequest(id)) }

    suspend fun addNatRule(request: NatRuleRequest): Result<NatRule> = runCatching {
        val result = api.addNatRule(request.toMap())
        NatRule(
            id = result.firstOrNull()?.get(".id") ?: "",
            chain = request.chain,
            action = request.action,
            srcAddress = request.srcAddress ?: "",
            dstAddress = request.dstAddress ?: "",
            protocol = request.protocol ?: "",
            disabled = "false",
            comment = request.comment ?: "",
            srcnatAddress = request.srcnatAddress ?: "",
            dstnatAddress = request.dstnatAddress ?: ""
        )
    }

    suspend fun editNatRule(id: String, updates: Map<String, String>): Result<NatRule> = runCatching {
        api.editNatRule(id, updates)
        NatRule(
            id = id,
            chain = updates["chain"] ?: "",
            action = updates["action"] ?: "",
            srcAddress = updates["src-address"] ?: "",
            dstAddress = updates["dst-address"] ?: "",
            protocol = updates["protocol"] ?: "",
            disabled = updates["disabled"] ?: "false",
            comment = updates["comment"] ?: "",
            srcnatAddress = updates["srcnat-address"] ?: "",
            dstnatAddress = updates["dstnat-address"] ?: ""
        )
    }

    suspend fun deleteNatRule(id: String): Result<Unit> = runCatching { api.deleteNatRule(id) }

    // ===== 过滤规则 =====
    suspend fun getFirewallFilters(props: List<String>? = null): Result<List<FirewallFilter>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getFirewallFilters(request).map { map ->
            FirewallFilter(
                id = map[".id"] ?: "",
                chain = map["chain"] ?: "",
                action = map["action"] ?: "",
                protocol = map["protocol"] ?: "",
                srcAddress = map["src-address"] ?: "",
                dstAddress = map["dst-address"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }

    suspend fun disableFirewallFilter(id: String): Result<Unit> = runCatching { api.disableFirewallFilter(IdRequest(id)) }
    suspend fun enableFirewallFilter(id: String): Result<Unit> = runCatching { api.enableFirewallFilter(IdRequest(id)) }

    suspend fun addFirewallFilter(request: FilterRuleRequest): Result<FirewallFilter> = runCatching {
        val result = api.addFirewallFilter(request.toMap())
        FirewallFilter(
            id = result.firstOrNull()?.get(".id") ?: "",
            chain = request.chain,
            action = request.action,
            protocol = request.protocol ?: "",
            srcAddress = request.srcAddress ?: "",
            dstAddress = request.dstAddress ?: "",
            disabled = "false",
            comment = request.comment ?: ""
        )
    }

    suspend fun editFirewallFilter(id: String, updates: Map<String, String>): Result<FirewallFilter> = runCatching {
        api.editFirewallFilter(id, updates)
        FirewallFilter(
            id = id,
            chain = updates["chain"] ?: "",
            action = updates["action"] ?: "",
            protocol = updates["protocol"] ?: "",
            srcAddress = updates["src-address"] ?: "",
            dstAddress = updates["dst-address"] ?: "",
            disabled = updates["disabled"] ?: "false",
            comment = updates["comment"] ?: ""
        )
    }

    suspend fun deleteFirewallFilter(id: String): Result<Unit> = runCatching { api.deleteFirewallFilter(id) }

    suspend fun getFirewallAddressLists(props: List<String>? = null): Result<List<FirewallAddressList>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getFirewallAddressLists(request).map { map ->
            FirewallAddressList(
                id = map[".id"] ?: "",
                list = map["list"] ?: "",
                address = map["address"] ?: "",
                disabled = map["disabled"] ?: "false",
                dynamic = map["dynamic"] ?: "false",
                creationTime = map["creation-time"] ?: "",
                timeout = map["timeout"] ?: "",
                comment = map["comment"] ?: ""
            )
        }
    }

    suspend fun disableFirewallAddressList(id: String): Result<Unit> = runCatching { api.disableFirewallAddressList(IdRequest(id)) }
    suspend fun enableFirewallAddressList(id: String): Result<Unit> = runCatching { api.enableFirewallAddressList(IdRequest(id)) }

    suspend fun addFirewallAddressList(request: FirewallAddressListRequest): Result<FirewallAddressList> = runCatching {
        val result = api.addFirewallAddressList(request.toMap())
        FirewallAddressList(
            id = result.firstOrNull()?.get(".id") ?: "",
            list = request.list,
            address = request.address,
            disabled = "false",
            dynamic = "false",
            creationTime = "",
            timeout = request.timeout ?: "",
            comment = request.comment ?: ""
        )
    }

    suspend fun editFirewallAddressList(id: String, updates: Map<String, String>): Result<FirewallAddressList> = runCatching {
        api.editFirewallAddressList(id, updates)
        FirewallAddressList(
            id = id,
            list = updates["list"] ?: "",
            address = updates["address"] ?: "",
            disabled = updates["disabled"] ?: "false",
            dynamic = updates["dynamic"] ?: "false",
            creationTime = updates["creation-time"] ?: "",
            timeout = updates["timeout"] ?: "",
            comment = updates["comment"] ?: ""
        )
    }

    suspend fun deleteFirewallAddressList(id: String): Result<Unit> = runCatching { api.deleteFirewallAddressList(id) }

    suspend fun getFirewallConnections(
        props: List<String>? = null,
        query: List<String>? = null
    ): Result<List<FirewallConnection>> = runCatching {
        val request = PrintRequest(proplist = props, query = query, withoutPaging = "")
        api.getFirewallConnections(request).map { map ->
            FirewallConnection(
                id = map[".id"] ?: "",
                protocol = map["protocol"] ?: "",
                srcAddress = map["src-address"] ?: "",
                dstAddress = map["dst-address"] ?: "",
                srcPort = map["src-port"] ?: "",
                dstPort = map["dst-port"] ?: "",
                replySrcAddress = map["reply-src-address"] ?: "",
                replyDstAddress = map["reply-dst-address"] ?: "",
                replySrcPort = map["reply-src-port"] ?: "",
                replyDstPort = map["reply-dst-port"] ?: "",
                tcpState = map["tcp-state"] ?: "",
                timeout = map["timeout"] ?: "",
                origBytes = map["orig-bytes"] ?: "",
                replBytes = map["repl-bytes"] ?: "",
                origPackets = map["orig-packets"] ?: "",
                replPackets = map["repl-packets"] ?: "",
                origRate = map["orig-rate"] ?: "",
                replRate = map["repl-rate"] ?: "",
                assured = map["assured"] ?: "false",
                seenReply = map["seen-reply"] ?: "false",
                fasttrack = map["fasttrack"] ?: "false",
                connectionMark = map["connection-mark"] ?: ""
            )
        }
    }

    // ===== IPv6 =====
    suspend fun getIpv6Addresses(props: List<String>? = null): Result<List<Ipv6Address>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpv6Addresses(request).map { Ipv6Address.fromMap(it) }
    }

    suspend fun getIpv6Neighbors(props: List<String>? = null): Result<List<Ipv6Neighbor>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpv6Neighbors(request).map { Ipv6Neighbor.fromMap(it) }
    }

    suspend fun addIpv6Address(
        address: String,
        interface_: String,
        advertise: String = "false",
        comment: String? = null
    ): Result<Ipv6Address> = runCatching {
        val req = AddIpv6AddressRequest(address, interface_, advertise, comment)
        val result = api.addIpv6Address(req.toMap())
        Ipv6Address.fromMap(result.firstOrNull() ?: emptyMap())
    }

    suspend fun editIpv6Address(id: String, updates: Map<String, String>): Result<Ipv6Address> = runCatching {
        val result = api.editIpv6Address(id, updates)
        Ipv6Address.fromMap(result.firstOrNull() ?: emptyMap())
    }

    suspend fun deleteIpv6Address(id: String): Result<Unit> = runCatching { api.deleteIpv6Address(id) }

    // IPv6 DHCP Client
    suspend fun getIpv6DhcpClients(props: List<String>? = null): Result<List<Ipv6DhcpClient>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpv6DhcpClients(request).map { Ipv6DhcpClient.fromMap(it) }
    }

    suspend fun disableIpv6DhcpClient(id: String): Result<Unit> = runCatching { api.disableIpv6DhcpClient(IdRequest(id)) }
    suspend fun enableIpv6DhcpClient(id: String): Result<Unit> = runCatching { api.enableIpv6DhcpClient(IdRequest(id)) }

    // IPv6 DHCP Server
    suspend fun getIpv6DhcpServers(props: List<String>? = null): Result<List<Ipv6DhcpServer>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpv6DhcpServers(request).map { Ipv6DhcpServer.fromMap(it) }
    }

    suspend fun disableIpv6DhcpServer(id: String): Result<Unit> = runCatching { api.disableIpv6DhcpServer(IdRequest(id)) }
    suspend fun enableIpv6DhcpServer(id: String): Result<Unit> = runCatching { api.enableIpv6DhcpServer(IdRequest(id)) }

    // IPv6 防火墙
    suspend fun getIpv6FirewallFilters(props: List<String>? = null): Result<List<Ipv6FirewallFilter>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpv6FirewallFilters(request).map { map ->
            Ipv6FirewallFilter(
                id = map[".id"] ?: "",
                chain = map["chain"] ?: "",
                action = map["action"] ?: "",
                srcAddress = map["src-address"] ?: "",
                dstAddress = map["dst-address"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }

    suspend fun disableIpv6FirewallFilter(id: String): Result<Unit> = runCatching { api.disableIpv6FirewallFilter(IdRequest(id)) }
    suspend fun enableIpv6FirewallFilter(id: String): Result<Unit> = runCatching { api.enableIpv6FirewallFilter(IdRequest(id)) }

    suspend fun addIpv6FirewallFilter(request: FilterRuleRequest): Result<Ipv6FirewallFilter> = runCatching {
        val result = api.addIpv6FirewallFilter(request.toMap())
        Ipv6FirewallFilter(
            id = result.firstOrNull()?.get(".id") ?: "",
            chain = request.chain,
            action = request.action,
            srcAddress = request.srcAddress ?: "",
            dstAddress = request.dstAddress ?: "",
            disabled = "false",
            comment = request.comment ?: ""
        )
    }

    suspend fun editIpv6FirewallFilter(id: String, updates: Map<String, String>): Result<Ipv6FirewallFilter> = runCatching {
        api.editIpv6FirewallFilter(id, updates)
        Ipv6FirewallFilter(
            id = id,
            chain = updates["chain"] ?: "",
            action = updates["action"] ?: "",
            srcAddress = updates["src-address"] ?: "",
            dstAddress = updates["dst-address"] ?: "",
            disabled = updates["disabled"] ?: "false",
            comment = updates["comment"] ?: ""
        )
    }

    suspend fun deleteIpv6FirewallFilter(id: String): Result<Unit> = runCatching { api.deleteIpv6FirewallFilter(id) }

    suspend fun getIpv6FirewallAddressLists(props: List<String>? = null): Result<List<Ipv6FirewallAddressList>> = runCatching {
        val request = PrintRequest(proplist = props, withoutPaging = "")
        api.getIpv6FirewallAddressLists(request).map { map ->
            Ipv6FirewallAddressList(
                id = map[".id"] ?: "",
                list = map["list"] ?: "",
                address = map["address"] ?: "",
                disabled = map["disabled"] ?: "false",
                dynamic = map["dynamic"] ?: "false",
                creationTime = map["creation-time"] ?: "",
                timeout = map["timeout"] ?: "",
                comment = map["comment"] ?: ""
            )
        }
    }

    suspend fun disableIpv6FirewallAddressList(id: String): Result<Unit> = runCatching { api.disableIpv6FirewallAddressList(IdRequest(id)) }
    suspend fun enableIpv6FirewallAddressList(id: String): Result<Unit> = runCatching { api.enableIpv6FirewallAddressList(IdRequest(id)) }

    suspend fun addIpv6FirewallAddressList(request: FirewallAddressListRequest): Result<Ipv6FirewallAddressList> = runCatching {
        val result = api.addIpv6FirewallAddressList(request.toMap())
        Ipv6FirewallAddressList(
            id = result.firstOrNull()?.get(".id") ?: "",
            list = request.list,
            address = request.address,
            disabled = "false",
            dynamic = "false",
            creationTime = "",
            timeout = request.timeout ?: "",
            comment = request.comment ?: ""
        )
    }

    suspend fun editIpv6FirewallAddressList(id: String, updates: Map<String, String>): Result<Ipv6FirewallAddressList> = runCatching {
        api.editIpv6FirewallAddressList(id, updates)
        Ipv6FirewallAddressList(
            id = id,
            list = updates["list"] ?: "",
            address = updates["address"] ?: "",
            disabled = updates["disabled"] ?: "false",
            dynamic = updates["dynamic"] ?: "false",
            creationTime = updates["creation-time"] ?: "",
            timeout = updates["timeout"] ?: "",
            comment = updates["comment"] ?: ""
        )
    }

    suspend fun deleteIpv6FirewallAddressList(id: String): Result<Unit> = runCatching { api.deleteIpv6FirewallAddressList(id) }

    suspend fun getIpv6FirewallConnections(
        props: List<String>? = null,
        query: List<String>? = null
    ): Result<List<FirewallConnection>> = runCatching {
        val request = PrintRequest(proplist = props, query = query, withoutPaging = "")
        api.getIpv6FirewallConnections(request).map { map ->
            FirewallConnection(
                id = map[".id"] ?: "",
                protocol = map["protocol"] ?: "",
                srcAddress = map["src-address"] ?: "",
                dstAddress = map["dst-address"] ?: "",
                srcPort = map["src-port"] ?: "",
                dstPort = map["dst-port"] ?: "",
                replySrcAddress = map["reply-src-address"] ?: "",
                replyDstAddress = map["reply-dst-address"] ?: "",
                replySrcPort = map["reply-src-port"] ?: "",
                replyDstPort = map["reply-dst-port"] ?: "",
                tcpState = map["tcp-state"] ?: "",
                timeout = map["timeout"] ?: "",
                origBytes = map["orig-bytes"] ?: "",
                replBytes = map["repl-bytes"] ?: "",
                origPackets = map["orig-packets"] ?: "",
                replPackets = map["repl-packets"] ?: "",
                origRate = map["orig-rate"] ?: "",
                replRate = map["repl-rate"] ?: "",
                assured = map["assured"] ?: "false",
                seenReply = map["seen-reply"] ?: "false",
                fasttrack = map["fasttrack"] ?: "false",
                connectionMark = map["connection-mark"] ?: ""
            )
        }
    }

    // ===== 终端设备（合并）=====
    suspend fun getNetworkDevices(): Result<List<NetworkDevice>> = runCatching {
        val leases = getDhcpLeases().getOrDefault(emptyList())
        val arpEntries = getArpEntries().getOrDefault(emptyList())
        val interfaces = getInterfaces().getOrDefault(emptyList())
        val dhcpServers = getDhcpServers().getOrDefault(emptyList())
        val ipv6Neighbors = getIpv6Neighbors().getOrDefault(emptyList())

        data class DeviceAccumulator(
            val key: String,
            val ipv4Addresses: MutableSet<String> = linkedSetOf(),
            val ipv6Addresses: MutableSet<String> = linkedSetOf(),
            val hostnames: MutableSet<String> = linkedSetOf(),
            val macAddresses: MutableSet<String> = linkedSetOf(),
            val interfaces: MutableSet<String> = linkedSetOf(),
            val statuses: MutableSet<String> = linkedSetOf(),
            val comments: MutableSet<String> = linkedSetOf(),
            val sources: MutableSet<String> = linkedSetOf(),
            var expires: String = "",
            var lastSeen: String = ""
        )

        val interfaceByName = interfaces.associateBy { it.name }
        val dhcpServerInterfaceByName = dhcpServers.associate { it.name to it.interface_ }
        val deviceMap = linkedMapOf<String, DeviceAccumulator>()

        fun normalizeAddress(value: String): String = value.substringBefore("/").trim()

        fun normalizeMac(value: String): String = value.trim().uppercase()

        fun sanitize(value: String): String = value.trim().takeIf { it.isNotEmpty() } ?: ""

        fun inferNameFromMac(mac: String): String {
            val normalized = sanitize(mac).replace("-", ":").uppercase()
            if (normalized.isBlank()) return ""
            val firstOctet = normalized.substringBefore(":").toIntOrNull(16) ?: return ""
            return if ((firstOctet and 0x02) != 0) "随机 MAC 设备" else ""
        }

        fun resolveKey(
            ipv4: String? = null,
            ipv6: String? = null,
            mac: String? = null,
            hostname: String? = null
        ): String {
            val normalizedMac = sanitize(mac.orEmpty()).let(::normalizeMac)
            if (normalizedMac.isNotEmpty()) return "mac:$normalizedMac"
            val normalizedIpv4 = sanitize(ipv4.orEmpty())
            if (normalizedIpv4.isNotEmpty()) return "ipv4:$normalizedIpv4"
            val normalizedIpv6 = sanitize(ipv6.orEmpty())
            if (normalizedIpv6.isNotEmpty()) return "ipv6:$normalizedIpv6"
            val normalizedHostname = sanitize(hostname.orEmpty())
            if (normalizedHostname.isNotEmpty()) return "host:${normalizedHostname.lowercase()}"
            return "unknown:${deviceMap.size}"
        }

        fun accumulatorFor(
            ipv4: String? = null,
            ipv6: String? = null,
            mac: String? = null,
            hostname: String? = null
        ): DeviceAccumulator {
            val candidates = listOfNotNull(
                sanitize(mac.orEmpty()).takeIf { it.isNotEmpty() }?.let { "mac:${normalizeMac(it)}" },
                sanitize(ipv4.orEmpty()).takeIf { it.isNotEmpty() }?.let { "ipv4:$it" },
                sanitize(ipv6.orEmpty()).takeIf { it.isNotEmpty() }?.let { "ipv6:$it" },
                sanitize(hostname.orEmpty()).takeIf { it.isNotEmpty() }?.let { "host:${it.lowercase()}" }
            )
            val existingKey = candidates.firstNotNullOfOrNull { candidate -> deviceMap[candidate]?.key }
            val finalKey = existingKey ?: resolveKey(ipv4 = ipv4, ipv6 = ipv6, mac = mac, hostname = hostname)
            return deviceMap.getOrPut(finalKey) { DeviceAccumulator(key = finalKey) }
        }

        leases.forEach { lease ->
            val ipv4 = sanitize(lease.address)
            val preferredHostname = sanitize(lease.activeHostName).ifEmpty { sanitize(lease.hostname) }
            val device = accumulatorFor(ipv4 = ipv4, mac = lease.macAddress, hostname = preferredHostname)
            if (ipv4.isNotEmpty()) device.ipv4Addresses += ipv4
            sanitize(lease.activeHostName).takeIf { it.isNotEmpty() }?.let(device.hostnames::add)
            sanitize(lease.hostname).takeIf { it.isNotEmpty() }?.let(device.hostnames::add)
            sanitize(lease.macAddress).takeIf { it.isNotEmpty() }?.let { device.macAddresses += normalizeMac(it) }
            sanitize(lease.server).takeIf { it.isNotEmpty() }?.let { serverName ->
                device.interfaces += dhcpServerInterfaceByName[serverName] ?: serverName
            }
            sanitize(lease.status).takeIf { it.isNotEmpty() }?.let(device.statuses::add)
            sanitize(lease.comment).takeIf { it.isNotEmpty() }?.let(device.comments::add)
            sanitize(lease.expires).takeIf { it.isNotEmpty() }?.let { device.expires = it }
            sanitize(lease.lastSeen).takeIf { it.isNotEmpty() }?.let { device.lastSeen = it }
            device.sources += "DHCP"
        }

        arpEntries.forEach { arp ->
            val ipv4 = sanitize(arp.address)
            val device = accumulatorFor(ipv4 = ipv4, mac = arp.macAddress)
            if (ipv4.isNotEmpty()) device.ipv4Addresses += ipv4
            sanitize(arp.macAddress).takeIf { it.isNotEmpty() }?.let { device.macAddresses += normalizeMac(it) }
            sanitize(arp.interface_).takeIf { it.isNotEmpty() }?.let(device.interfaces::add)
            sanitize(arp.comment).takeIf { it.isNotEmpty() }?.let(device.comments::add)
            device.statuses += if (arp.complete == "true") "complete" else "incomplete"
            device.sources += "ARP"
        }

        ipv6Neighbors.forEach { neighbor ->
            val ipv6 = sanitize(normalizeAddress(neighbor.address))
            val device = accumulatorFor(ipv6 = ipv6, mac = neighbor.macAddress)
            if (ipv6.isNotEmpty()) device.ipv6Addresses += ipv6
            sanitize(neighbor.macAddress).takeIf { it.isNotEmpty() }?.let { device.macAddresses += normalizeMac(it) }
            sanitize(neighbor.interface_).takeIf { it.isNotEmpty() }?.let(device.interfaces::add)
            sanitize(neighbor.status).takeIf { it.isNotEmpty() }?.let(device.statuses::add)
            sanitize(neighbor.comment).takeIf { it.isNotEmpty() }?.let(device.comments::add)
            device.sources += "IPv6 邻居"
        }

        deviceMap.values.map { device ->
            val chosenInterface = device.interfaces.firstOrNull().orEmpty()
            val interfaceInfo = interfaceByName[chosenInterface]
            val primaryAddress = device.ipv4Addresses.firstOrNull()
                ?: device.ipv6Addresses.firstOrNull()
                ?: device.hostnames.firstOrNull()
                ?: device.key
            val hostname = device.hostnames.firstOrNull().orEmpty()
            val macAddress = device.macAddresses.firstOrNull().orEmpty()
            val inferredName = inferNameFromMac(macAddress)
            val displayName = hostname.ifEmpty {
                inferredName.ifEmpty {
                    primaryAddress
                }
            }
            NetworkDevice(
                key = device.key,
                displayName = displayName,
                primaryAddress = primaryAddress,
                ipv4Addresses = device.ipv4Addresses.toList(),
                ipv6Addresses = device.ipv6Addresses.toList(),
                macAddress = macAddress,
                hostname = hostname,
                inferredName = inferredName,
                interface_ = chosenInterface,
                interfaceType = interfaceInfo?.type.orEmpty(),
                interfaceComment = interfaceInfo?.comment.orEmpty(),
                status = device.statuses.joinToString(" / "),
                expires = device.expires,
                lastSeen = device.lastSeen,
                comment = device.comments.joinToString(" / "),
                sources = device.sources.toList()
            )
        }.sortedWith(
            compareByDescending<NetworkDevice> { max(it.ipv4Addresses.size, it.ipv6Addresses.size) }
                .thenBy { it.displayName.lowercase() }
                .thenBy { it.primaryAddress.lowercase() }
        )
    }

    // ===== 连接配置更新 =====
    fun updateConnection(host: String, port: Int, username: String, password: String) {
        networkClient.updateConfig(host, port, username, password)
    }
}
