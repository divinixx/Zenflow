package com.divinixx.zenflow.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a discovered Zenflow PC service
 */
data class DiscoveredService(
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val isResolved: Boolean = false
)

/**
 * Network Service Discovery Manager for automatic PC detection
 * Discovers Zenflow servers on the local network using mDNS/Bonjour
 */
@Singleton
class NetworkDiscoveryManager @Inject constructor(
    private val context: Context
) {
    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }
    
    // Service type for Zenflow WebSocket servers - try multiple variations
    private val serviceTypes = listOf(
        "_zenflow-ws._tcp",
        "_zenflow-ws._tcp.",
        "_zenflow-ws._tcp.local.",
        "_zenflow._tcp",
        "_zenflow._tcp.",
        "_zenflow._tcp.local.",
        "_http._tcp",
        "_http._tcp."
    )
    
    // Current service type being discovered
    private var currentServiceTypeIndex = 0
    
    // StateFlow for discovered services
    private val _discoveredServices = MutableStateFlow<List<DiscoveredService>>(emptyList())
    val discoveredServices: StateFlow<List<DiscoveredService>> = _discoveredServices.asStateFlow()
    
    // StateFlow for discovery state
    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()
    
    // Keep track of services being resolved
    private val pendingResolutions = mutableMapOf<String, NsdServiceInfo>()
    
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    
    /**
     * Start discovering Zenflow services on the network
     * Try multiple service types to find services
     */
    fun startDiscovery() {
        if (_isDiscovering.value) {
            return
        }
        
        try {
            _isDiscovering.value = true
            _discoveredServices.value = emptyList() // Clear previous results
            pendingResolutions.clear() // Clear pending resolutions
            
            // Start discovery for multiple service types
            currentServiceTypeIndex = 0
            startDiscoveryForCurrentType()
            
        } catch (e: Exception) {
            _isDiscovering.value = false
        }
    }
    
    /**
     * Start discovery for the current service type and cycle through others
     */
    private fun startDiscoveryForCurrentType() {
        if (currentServiceTypeIndex < serviceTypes.size) {
            val serviceType = serviceTypes[currentServiceTypeIndex]
            
            discoveryListener = createDiscoveryListener(serviceType)
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            
            currentServiceTypeIndex++
        }
    }
    
    /**
     * Stop discovering services
     */
    fun stopDiscovery() {
        try {
            discoveryListener?.let { listener ->
                nsdManager.stopServiceDiscovery(listener)
                discoveryListener = null
            }
            _isDiscovering.value = false
        } catch (e: Exception) {
            // Silently handle stop errors
        }
    }
    
    /**
     * Create the discovery listener for handling NSD events
     */
    private fun createDiscoveryListener(serviceType: String): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                // Check if this is a Zenflow service or similar WebSocket service
                val isZenflowService = serviceInfo.serviceName.contains("zenflow", ignoreCase = true) ||
                    serviceInfo.serviceName.contains("divinixx", ignoreCase = true) ||
                    serviceInfo.serviceType.contains("zenflow", ignoreCase = true) ||
                    serviceInfo.serviceType.contains("http", ignoreCase = true) ||
                    serviceInfo.serviceType.contains("ws", ignoreCase = true) ||
                    serviceInfo.port == 8080 ||
                    serviceInfo.port == 3000 || 
                    serviceInfo.port == 8000 ||
                    serviceInfo.port == 9000 ||
                    serviceInfo.serviceName.contains("server", ignoreCase = true)
                
                if (isZenflowService) {
                    // Add unresolved service to list first
                    val unresolved = DiscoveredService(
                        deviceName = serviceInfo.serviceName,
                        ipAddress = "Resolving...",
                        port = 0,
                        isResolved = false
                    )
                    addOrUpdateService(unresolved)
                    
                    // Store for resolution
                    pendingResolutions[serviceInfo.serviceName] = serviceInfo
                    
                    // Use the basic resolve API to avoid deprecation
                    try {
                        nsdManager.resolveService(serviceInfo, createResolveListener())
                    } catch (e: Exception) {
                        // Remove from pending resolutions if resolution fails
                        pendingResolutions.remove(serviceInfo.serviceName)
                    }
                }
            }
            
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                removeService(serviceInfo.serviceName)
            }
            
            override fun onDiscoveryStarted(serviceType: String) {
                _isDiscovering.value = true
            }
            
            override fun onDiscoveryStopped(serviceType: String) {
                _isDiscovering.value = false
            }
            
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                _isDiscovering.value = false
            }
            
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                // Discovery stop failed but consider it stopped anyway
            }
        }
    }
    
    /**
     * Create resolve listener for getting IP address and port
     */
    private fun createResolveListener(): NsdManager.ResolveListener {
        return object : NsdManager.ResolveListener {
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val hostAddress = try {
                    @Suppress("DEPRECATION")
                    serviceInfo.host?.hostAddress
                } catch (e: Exception) {
                    null
                }
                
                if (hostAddress != null && serviceInfo.port > 0) {
                    val resolved = DiscoveredService(
                        deviceName = serviceInfo.serviceName,
                        ipAddress = hostAddress,
                        port = serviceInfo.port,
                        isResolved = true
                    )
                    
                    addOrUpdateService(resolved)
                }
                
                pendingResolutions.remove(serviceInfo.serviceName)
            }
            
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                val failed = DiscoveredService(
                    deviceName = serviceInfo.serviceName,
                    ipAddress = "Failed to resolve",
                    port = 0,
                    isResolved = false
                )
                
                addOrUpdateService(failed)
                pendingResolutions.remove(serviceInfo.serviceName)
            }
        }
    }
    
    /**
     * Add or update a service in the discovered services list
     */
    private fun addOrUpdateService(service: DiscoveredService) {
        val currentList = _discoveredServices.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.deviceName == service.deviceName }
        
        if (existingIndex >= 0) {
            currentList[existingIndex] = service
        } else {
            currentList.add(service)
        }
        
        _discoveredServices.value = currentList
    }
    
    /**
     * Remove a service from the discovered services list
     */
    private fun removeService(serviceName: String) {
        val currentList = _discoveredServices.value.toMutableList()
        currentList.removeAll { it.deviceName == serviceName }
        _discoveredServices.value = currentList
        pendingResolutions.remove(serviceName)
    }
    
    /**
     * Get the connection URL for a discovered service
     */
    fun getConnectionUrl(service: DiscoveredService): String {
        return if (service.isResolved && service.ipAddress != "Resolving..." && !service.ipAddress.startsWith("Failed")) {
            "ws://${service.ipAddress}:${service.port}"
        } else {
            ""
        }
    }
    
    /**
     * Add a test service for debugging purposes
     * This can be used when the actual NSD discovery isn't working
     */
    fun addTestService(deviceName: String = "Test-Zenflow-PC", ipAddress: String = "192.168.1.100", port: Int = 8080) {
        val testService = DiscoveredService(
            deviceName = deviceName,
            ipAddress = ipAddress,
            port = port,
            isResolved = true
        )
        addOrUpdateService(testService)
    }
    
    /**
     * Get current discovery status
     */
    fun getDiscoveryStatus(): String {
        return """
            Discovery Status:
            - Is Discovering: ${_isDiscovering.value}
            - Services Found: ${_discoveredServices.value.size}
            - Pending Resolutions: ${pendingResolutions.size}
            - Service Types: ${serviceTypes.joinToString(", ")}
            
            Found Services:
            ${_discoveredServices.value.joinToString("\n") { "  - ${it.deviceName}: ${it.ipAddress}:${it.port} (resolved: ${it.isResolved})" }}
        """.trimIndent()
    }
}
