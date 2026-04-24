package com.routeros.manager.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RouterOSApi {
    // 系统
    @POST("/rest/system/identity/print")
    suspend fun getSystemIdentity(): List<Map<String, String>>

    @POST("/rest/system/resource/print")
    suspend fun getSystemResource(): List<Map<String, String>>

    // 接口
    @POST("/rest/interface/print")
    suspend fun getInterfaces(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/interface/monitor-traffic")
    suspend fun monitorTraffic(@Body request: MonitorTrafficRequest): List<Map<String, String>>

    @POST("/rest/interface/disable")
    suspend fun disableInterface(@Body request: IdRequest)

    @POST("/rest/interface/enable")
    suspend fun enableInterface(@Body request: IdRequest)

    // IP 地址
    @POST("/rest/ip/address/print")
    suspend fun getIpAddresses(@Body request: PrintRequest): List<Map<String, String>>

    @HTTP(method = "PUT", path = "/rest/ip/address", hasBody = true)
    suspend fun addIpAddress(@Body request: Map<String, String>): List<Map<String, String>>

    @PATCH("/rest/ip/address/{id}")
    suspend fun editIpAddress(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    @DELETE("/rest/ip/address/{id}")
    suspend fun deleteIpAddress(@Path("id") id: String)

    // ARP
    @POST("/rest/ip/arp/print")
    suspend fun getArpEntries(@Body request: PrintRequest): List<Map<String, String>>

    // DHCP Lease
    @POST("/rest/ip/dhcp-server/lease/print")
    suspend fun getDhcpLeases(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ip/dhcp-server/lease/make-static")
    suspend fun makeDhcpLeaseStatic(@Body request: DhcpLeaseMakeStaticRequest)

    @PATCH("/rest/ip/dhcp-server/lease/{id}")
    suspend fun editDhcpLease(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    // DHCP Client
    @POST("/rest/ip/dhcp-client/print")
    suspend fun getDhcpClients(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ip/dhcp-client/disable")
    suspend fun disableDhcpClient(@Body request: IdRequest)

    @POST("/rest/ip/dhcp-client/enable")
    suspend fun enableDhcpClient(@Body request: IdRequest)

    // DHCP Server
    @POST("/rest/ip/dhcp-server/print")
    suspend fun getDhcpServers(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ip/dhcp-server/disable")
    suspend fun disableDhcpServer(@Body request: IdRequest)

    @POST("/rest/ip/dhcp-server/enable")
    suspend fun enableDhcpServer(@Body request: IdRequest)

    // DNS
    @POST("/rest/ip/dns/static/print")
    suspend fun getDnsRecords(@Body request: PrintRequest): List<Map<String, String>>

    @HTTP(method = "PUT", path = "/rest/ip/dns/static", hasBody = true)
    suspend fun addDnsRecord(@Body request: Map<String, String>): List<Map<String, String>>

    @PATCH("/rest/ip/dns/static/{id}")
    suspend fun editDnsRecord(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    @DELETE("/rest/ip/dns/static/{id}")
    suspend fun deleteDnsRecord(@Path("id") id: String)

    // IPv4 防火墙 - NAT
    @POST("/rest/ip/firewall/nat/print")
    suspend fun getNatRules(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ip/firewall/nat/disable")
    suspend fun disableNatRule(@Body request: IdRequest)

    @POST("/rest/ip/firewall/nat/enable")
    suspend fun enableNatRule(@Body request: IdRequest)

    @HTTP(method = "PUT", path = "/rest/ip/firewall/nat", hasBody = true)
    suspend fun addNatRule(@Body request: Map<String, String>): List<Map<String, String>>

    @PATCH("/rest/ip/firewall/nat/{id}")
    suspend fun editNatRule(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    @DELETE("/rest/ip/firewall/nat/{id}")
    suspend fun deleteNatRule(@Path("id") id: String)

    // IPv4 防火墙 - Filter
    @POST("/rest/ip/firewall/filter/print")
    suspend fun getFirewallFilters(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ip/firewall/filter/disable")
    suspend fun disableFirewallFilter(@Body request: IdRequest)

    @POST("/rest/ip/firewall/filter/enable")
    suspend fun enableFirewallFilter(@Body request: IdRequest)

    @HTTP(method = "PUT", path = "/rest/ip/firewall/filter", hasBody = true)
    suspend fun addFirewallFilter(@Body request: Map<String, String>): List<Map<String, String>>

    @PATCH("/rest/ip/firewall/filter/{id}")
    suspend fun editFirewallFilter(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    @DELETE("/rest/ip/firewall/filter/{id}")
    suspend fun deleteFirewallFilter(@Path("id") id: String)

    // IPv6 地址
    @POST("/rest/ipv6/address/print")
    suspend fun getIpv6Addresses(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ipv6/neighbor/print")
    suspend fun getIpv6Neighbors(@Body request: PrintRequest): List<Map<String, String>>

    @HTTP(method = "PUT", path = "/rest/ipv6/address", hasBody = true)
    suspend fun addIpv6Address(@Body request: Map<String, String>): List<Map<String, String>>

    @PATCH("/rest/ipv6/address/{id}")
    suspend fun editIpv6Address(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    @DELETE("/rest/ipv6/address/{id}")
    suspend fun deleteIpv6Address(@Path("id") id: String)

    // IPv6 DHCP Client
    @POST("/rest/ipv6/dhcp-client/print")
    suspend fun getIpv6DhcpClients(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ipv6/dhcp-client/disable")
    suspend fun disableIpv6DhcpClient(@Body request: IdRequest)

    @POST("/rest/ipv6/dhcp-client/enable")
    suspend fun enableIpv6DhcpClient(@Body request: IdRequest)

    // IPv6 DHCP Server
    @POST("/rest/ipv6/dhcp-server/print")
    suspend fun getIpv6DhcpServers(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ipv6/dhcp-server/disable")
    suspend fun disableIpv6DhcpServer(@Body request: IdRequest)

    @POST("/rest/ipv6/dhcp-server/enable")
    suspend fun enableIpv6DhcpServer(@Body request: IdRequest)

    // IPv6 防火墙
    @POST("/rest/ipv6/firewall/filter/print")
    suspend fun getIpv6FirewallFilters(@Body request: PrintRequest): List<Map<String, String>>

    @POST("/rest/ipv6/firewall/filter/disable")
    suspend fun disableIpv6FirewallFilter(@Body request: IdRequest)

    @POST("/rest/ipv6/firewall/filter/enable")
    suspend fun enableIpv6FirewallFilter(@Body request: IdRequest)

    @HTTP(method = "PUT", path = "/rest/ipv6/firewall/filter", hasBody = true)
    suspend fun addIpv6FirewallFilter(@Body request: Map<String, String>): List<Map<String, String>>

    @PATCH("/rest/ipv6/firewall/filter/{id}")
    suspend fun editIpv6FirewallFilter(@Path("id") id: String, @Body request: Map<String, String>): List<Map<String, String>>

    @DELETE("/rest/ipv6/firewall/filter/{id}")
    suspend fun deleteIpv6FirewallFilter(@Path("id") id: String)
}
