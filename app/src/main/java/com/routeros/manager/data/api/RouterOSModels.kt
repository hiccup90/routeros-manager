package com.routeros.manager.data.api

import com.google.gson.annotations.SerializedName

// ===== 数据模型 =====

// 系统信息
data class SystemIdentity(
    val name: String,
    @SerializedName(".id") val id: String = ""
)
data class SystemResource(
    val uptime: String = "",
    val version: String = "",
    @SerializedName("cpu-load") val cpuLoad: String = "",
    @SerializedName("free-memory") val freeMemory: String = "",
    @SerializedName("total-memory") val totalMemory: String = "",
    @SerializedName("free-hdd-space") val freeHddSpace: String = "",
    @SerializedName("total-hdd-space") val totalHddSpace: String = "",
    @SerializedName("board-name") val boardName: String = "",
    val model: String = "",
    @SerializedName("serial-number") val serialNumber: String = ""
)

// 接口
data class InterfaceItem(
    @SerializedName(".id") val id: String = "",
    val name: String = "",
    val type: String = "",
    val disabled: String = "false",
    @SerializedName("rx-byte") val rxByte: String = "0",
    @SerializedName("tx-byte") val txByte: String = "0",
    @SerializedName("rx-packet") val rxPacket: String = "0",
    @SerializedName("tx-packet") val txPacket: String = "0",
    val speed: String = "",
    @SerializedName("mac-address") val macAddress: String = "",
    val comment: String = ""
)

data class MonitorTraffic(
    @SerializedName("rx-bits-per-second") val rxBitsPerSecond: String = "0",
    @SerializedName("tx-bits-per-second") val txBitsPerSecond: String = "0",
    @SerializedName("rx-bytes") val rxBytes: String = "0",
    @SerializedName("tx-bytes") val txBytes: String = "0"
)

// IP 地址
data class IpAddress(
    @SerializedName(".id") val id: String = "",
    val address: String = "",
    @SerializedName("interface") val interface_: String = "",
    val network: String = "",
    val disabled: String = "false",
    val dynamic: String = "false",
    val invalid: String = "false",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): IpAddress {
            return IpAddress(
                id = map[".id"] ?: "",
                address = map["address"] ?: "",
                interface_ = map["interface"] ?: "",
                network = map["network"] ?: "",
                disabled = map["disabled"] ?: "false",
                dynamic = map["dynamic"] ?: "false",
                invalid = map["invalid"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }
}

// NAT/防火墙规则
data class NatRule(
    @SerializedName(".id") val id: String = "",
    val chain: String = "",
    val action: String = "",
    @SerializedName("src-address") val srcAddress: String = "",
    @SerializedName("dst-address") val dstAddress: String = "",
    val protocol: String = "",
    val disabled: String = "false",
    val comment: String = "",
    @SerializedName("srcnat-address") val srcnatAddress: String = "",
    @SerializedName("dstnat-address") val dstnatAddress: String = ""
)

data class FirewallFilter(
    @SerializedName(".id") val id: String = "",
    val chain: String = "",
    val action: String = "",
    val protocol: String = "",
    @SerializedName("src-address") val srcAddress: String = "",
    @SerializedName("dst-address") val dstAddress: String = "",
    val disabled: String = "false",
    val comment: String = ""
)

// DHCP
data class DhcpLease(
    @SerializedName(".id") val id: String = "",
    val address: String = "",
    @SerializedName("mac-address") val macAddress: String = "",
    @SerializedName("active-host-name") val activeHostName: String = "",
    val hostname: String = "",
    val status: String = "",
    val server: String = "",
    val expires: String = "",
    @SerializedName("last-seen") val lastSeen: String = "",
    val comment: String = "",
    @SerializedName("client-id") val clientId: String = "",
    val radius: String = "",
    val dynamic: String = "false"
)

data class DhcpLeaseMakeStaticRequest(val numbers: String)

data class DhcpClient(
    @SerializedName(".id") val id: String = "",
    @SerializedName("interface") val interface_: String = "",
    val status: String = "",
    val address: String = "",
    @SerializedName("dhcp-server") val dhcpServer: String = "",
    val disabled: String = "false",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): DhcpClient {
            return DhcpClient(
                id = map[".id"] ?: "",
                interface_ = map["interface"] ?: "",
                status = map["status"] ?: "",
                address = map["address"] ?: "",
                dhcpServer = map["dhcp-server"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }
}

data class DhcpServer(
    @SerializedName(".id") val id: String = "",
    val name: String = "",
    @SerializedName("interface") val interface_: String = "",
    val disabled: String = "false",
    @SerializedName("address-pool") val addressPool: String = "",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): DhcpServer {
            return DhcpServer(
                id = map[".id"] ?: "",
                name = map["name"] ?: "",
                interface_ = map["interface"] ?: "",
                disabled = map["disabled"] ?: "false",
                addressPool = map["address-pool"] ?: "",
                comment = map["comment"] ?: ""
            )
        }
    }
}

data class DhcpNetwork(
    @SerializedName(".id") val id: String = "",
    val address: String = "",
    val gateway: String = "",
    @SerializedName("dns-server") val dnsServer: String = "",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): DhcpNetwork {
            return DhcpNetwork(
                id = map[".id"] ?: "",
                address = map["address"] ?: "",
                gateway = map["gateway"] ?: "",
                dnsServer = map["dns-server"] ?: "",
                comment = map["comment"] ?: ""
            )
        }
    }
}

// DNS
data class DnsRecord(
    @SerializedName(".id") val id: String = "",
    val name: String = "",
    val address: String = "",
    val ttl: String = "",
    val disabled: String = "false",
    val comment: String = ""
)

// ARP
data class ArpEntry(
    @SerializedName(".id") val id: String = "",
    val address: String = "",
    @SerializedName("mac-address") val macAddress: String = "",
    @SerializedName("interface") val interface_: String = "",
    val complete: String = "false",
    val invalid: String = "false",
    val dynamic: String = "false",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): ArpEntry {
            return ArpEntry(
                id = map[".id"] ?: "",
                address = map["address"] ?: "",
                macAddress = map["mac-address"] ?: "",
                interface_ = map["interface"] ?: "",
                complete = map["complete"] ?: "false",
                invalid = map["invalid"] ?: "false",
                dynamic = map["dynamic"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }
}

// IPv6
data class Ipv6Address(
    @SerializedName(".id") val id: String = "",
    val address: String = "",
    @SerializedName("interface") val interface_: String = "",
    val advertise: String = "false",
    val disabled: String = "false",
    @SerializedName("valid-lifetime") val validLifetime: String = "",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): Ipv6Address {
            return Ipv6Address(
                id = map[".id"] ?: "",
                address = map["address"] ?: "",
                interface_ = map["interface"] ?: "",
                advertise = map["advertise"] ?: "false",
                disabled = map["disabled"] ?: "false",
                validLifetime = map["valid-lifetime"] ?: "",
                comment = map["comment"] ?: ""
            )
        }
    }
}

data class Ipv6Neighbor(
    @SerializedName(".id") val id: String = "",
    val address: String = "",
    @SerializedName("mac-address") val macAddress: String = "",
    @SerializedName("interface") val interface_: String = "",
    val status: String = "",
    val dynamic: String = "false",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): Ipv6Neighbor {
            return Ipv6Neighbor(
                id = map[".id"] ?: "",
                address = map["address"] ?: "",
                macAddress = map["mac-address"] ?: "",
                interface_ = map["interface"] ?: "",
                status = map["status"] ?: "",
                dynamic = map["dynamic"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }
}

data class Ipv6DhcpClient(
    @SerializedName(".id") val id: String = "",
    @SerializedName("interface") val interface_: String = "",
    val status: String = "",
    val address: String = "",
    val prefix: String = "",
    val disabled: String = "false",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): Ipv6DhcpClient {
            return Ipv6DhcpClient(
                id = map[".id"] ?: "",
                interface_ = map["interface"] ?: "",
                status = map["status"] ?: "",
                address = map["address"] ?: "",
                prefix = map["prefix"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }
}

data class Ipv6DhcpServer(
    @SerializedName(".id") val id: String = "",
    val name: String = "",
    @SerializedName("interface") val interface_: String = "",
    @SerializedName("address-pool") val addressPool: String = "",
    val disabled: String = "false",
    val comment: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, String>): Ipv6DhcpServer {
            return Ipv6DhcpServer(
                id = map[".id"] ?: "",
                name = map["name"] ?: "",
                interface_ = map["interface"] ?: "",
                addressPool = map["address-pool"] ?: "",
                disabled = map["disabled"] ?: "false",
                comment = map["comment"] ?: ""
            )
        }
    }
}

data class Ipv6FirewallFilter(
    @SerializedName(".id") val id: String = "",
    val chain: String = "",
    val action: String = "",
    @SerializedName("src-address") val srcAddress: String = "",
    @SerializedName("dst-address") val dstAddress: String = "",
    val disabled: String = "false",
    val comment: String = ""
)

// 终端设备
data class NetworkDevice(
    val key: String,
    val displayName: String,
    val primaryAddress: String,
    val ipv4Addresses: List<String> = emptyList(),
    val ipv6Addresses: List<String> = emptyList(),
    val macAddress: String = "",
    val hostname: String = "",
    val inferredName: String = "",
    @SerializedName("interface") val interface_: String = "",
    val interfaceType: String = "",
    val interfaceComment: String = "",
    val status: String = "",
    val expires: String = "",
    val lastSeen: String = "",
    val comment: String = "",
    val sources: List<String> = emptyList()
)

// API 错误
data class ApiError(val error: Int, val message: String)

// ===== 请求模型 =====

// 查询请求
data class PrintRequest(
    @SerializedName(".proplist") val proplist: List<String>? = null,
    @SerializedName(".query") val query: List<String>? = null,
    @SerializedName("without-paging") val withoutPaging: String? = null
)

// 流量监控请求
data class MonitorTrafficRequest(
    val numbers: String,
    val interval: String = "00:00:01"
)

// ID 请求
data class IdRequest(@SerializedName(".id") val id: String)

// IP 地址操作
data class AddIpAddressRequest(
    val address: String,
    @SerializedName("interface") val interface_: String,
    val comment: String? = null
) {
    fun toMap(): Map<String, String> = buildMap {
        put("address", address)
        put("interface", interface_)
        comment?.let { put("comment", it) }
    }
}

// NAT 规则操作
data class NatRuleRequest(
    val chain: String,
    val action: String,
    @SerializedName("src-address") val srcAddress: String? = null,
    @SerializedName("dst-address") val dstAddress: String? = null,
    val protocol: String? = null,
    @SerializedName("srcnat-address") val srcnatAddress: String? = null,
    @SerializedName("dstnat-address") val dstnatAddress: String? = null,
    val comment: String? = null
) {
    fun toMap(): Map<String, String> = buildMap {
        put("chain", chain)
        put("action", action)
        srcAddress?.let { put("src-address", it) }
        dstAddress?.let { put("dst-address", it) }
        protocol?.let { put("protocol", it) }
        srcnatAddress?.let { put("srcnat-address", it) }
        dstnatAddress?.let { put("dstnat-address", it) }
        comment?.let { put("comment", it) }
    }
}

// 过滤规则操作
data class FilterRuleRequest(
    val chain: String,
    val action: String,
    val protocol: String? = null,
    @SerializedName("src-address") val srcAddress: String? = null,
    @SerializedName("dst-address") val dstAddress: String? = null,
    val comment: String? = null
) {
    fun toMap(): Map<String, String> = buildMap {
        put("chain", chain)
        put("action", action)
        protocol?.let { put("protocol", it) }
        srcAddress?.let { put("src-address", it) }
        dstAddress?.let { put("dst-address", it) }
        comment?.let { put("comment", it) }
    }
}

// DNS 记录
data class DnsRecordRequest(
    val name: String,
    val address: String,
    val ttl: String? = null,
    val comment: String? = null
) {
    fun toMap(): Map<String, String> = buildMap {
        put("name", name)
        put("address", address)
        ttl?.let { put("ttl", it) }
        comment?.let { put("comment", it) }
    }
}

// ===== 通用列表项（跨页面复用）=====
data class CommonListItem(
 val id: String,
 val title: String,
 val subtitle: String,
 val disabled: Boolean,
 val onToggle: () -> Unit,
 val onEdit: () -> Unit,
 val onDelete: () -> Unit
)

// IPv6 地址
data class AddIpv6AddressRequest(
    val address: String,
    @SerializedName("interface") val interface_: String,
    val advertise: String = "false",
    val comment: String? = null
) {
    fun toMap(): Map<String, String> = buildMap {
        put("address", address)
        put("interface", interface_)
        put("advertise", advertise)
        comment?.let { put("comment", it) }
    }
}
