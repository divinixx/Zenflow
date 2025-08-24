package com.divinixx.zenflow.network

import android.util.Log
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern Ktor WebSocket Manager with native Kotlin coroutines support
 * Provides suspend functions, Flow-based messaging, and structured concurrency
 */
@Singleton
class KtorWebSocketManager @Inject constructor() {
    
    companion object {
        private const val TAG = "KtorWebSocketManager"
        private const val CONNECTION_TIMEOUT_MS = 15_000L
        private const val PING_INTERVAL_MS = 30_000L
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }
    
    private val gson = Gson()
    private var client: HttpClient? = null
    private var webSocketSession: DefaultWebSocketSession? = null
    private var connectionJob: Job? = null
    private var pingJob: Job? = null
    
    // Message throttling for smooth performance
    private var lastMouseMessageTime: Long = 0L
    private val mouseMessageThrottle: Long = 8L // ~125fps for mouse movement
    
    // Connection state flows
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()
    
    private val _messages = MutableSharedFlow<String>(replay = 0)
    val messages: SharedFlow<String> = _messages.asSharedFlow()
    
    private val _errors = MutableSharedFlow<String>(replay = 0)
    val errors: SharedFlow<String> = _errors.asSharedFlow()
    
    /**
     * Initialize Ktor client with WebSocket support
     */
    private fun createClient(): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = PING_INTERVAL_MS.milliseconds
                maxFrameSize = Long.MAX_VALUE
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }
    
    /**
     * Connect to WebSocket server using Kotlin coroutines
     */
    suspend fun connect(ipAddress: String, port: Int = 8080): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                disconnect() // Cleanup any existing connection
                
                _connectionState.value = "Connecting..."
                Log.i(TAG, "Connecting to ws://$ipAddress:$port")
                
                client = createClient()
                
                connectionJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        client?.webSocket(
                            host = ipAddress,
                            port = port,
                            path = "/"
                        ) {
                            webSocketSession = this
                            _isConnected.value = true
                            _connectionState.value = "Connected"
                            Log.i(TAG, "WebSocket connected successfully")
                            
                            // Start ping job
                            startPingJob()
                            
                            // Listen for incoming messages
                            for (frame in incoming) {
                                when (frame) {
                                    is Frame.Text -> {
                                        val message = frame.readText()
                                        Log.d(TAG, "Received: $message")
                                        _messages.emit(message)
                                    }
                                    is Frame.Binary -> {
                                        Log.d(TAG, "Received binary frame")
                                    }
                                    is Frame.Close -> {
                                        Log.i(TAG, "WebSocket closed by server")
                                        handleDisconnection()
                                        break
                                    }
                                    else -> {
                                        Log.d(TAG, "Received frame: ${frame.frameType}")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "WebSocket connection error", e)
                        handleConnectionError(e)
                    }
                }
                
                // Wait a bit to see if connection establishes
                delay(2000)
                _isConnected.value
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect", e)
                handleConnectionError(e)
                false
            }
        }
    }
    
    /**
     * Send message using suspend function - perfect for gesture sending
     */
    suspend fun sendMessage(data: Map<String, Any>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonMessage = gson.toJson(data)
                webSocketSession?.send(Frame.Text(jsonMessage))
                Log.d(TAG, "Sent: $jsonMessage")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message", e)
                _errors.emit("Failed to send message: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Optimized mouse movement sending with throttling
     */
    suspend fun sendMouseMessage(data: Map<String, Any>): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Throttle mouse messages to prevent overwhelming the network
        if (data["type"] == "mouse" && currentTime - lastMouseMessageTime < mouseMessageThrottle) {
            return false // Skip this message
        }
        
        lastMouseMessageTime = currentTime
        return sendMessage(data)
    }
    
    /**
     * Send message without waiting (fire and forget) - for high-frequency gestures
     */
    fun sendMessageAsync(data: Map<String, Any>, scope: CoroutineScope) {
        scope.launch {
            if (data["type"] == "mouse") {
                sendMouseMessage(data)
            } else {
                sendMessage(data)
            }
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Disconnecting WebSocket")
                
                pingJob?.cancel()
                pingJob = null
                
                webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnect"))
                webSocketSession = null
                
                connectionJob?.cancel()
                connectionJob = null
                
                client?.close()
                client = null
                
                _isConnected.value = false
                _connectionState.value = "Disconnected"
                
                Log.i(TAG, "WebSocket disconnected")
            } catch (e: Exception) {
                Log.e(TAG, "Error during disconnect", e)
            }
        }
    }
    
    /**
     * Start ping job to keep connection alive
     */
    private fun startPingJob() {
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _isConnected.value) {
                try {
                    delay(PING_INTERVAL_MS)
                    // Send ping frame using send() method
                    webSocketSession?.send(Frame.Ping(byteArrayOf()))
                    Log.d(TAG, "Ping sent")
                } catch (e: Exception) {
                    Log.e(TAG, "Ping failed", e)
                    break
                }
            }
        }
    }
    
    /**
     * Handle connection errors
     */
    private suspend fun handleConnectionError(error: Throwable) {
        _isConnected.value = false
        _connectionState.value = "Error"
        _errors.emit("Connection error: ${error.message}")
    }
    
    /**
     * Handle disconnection
     */
    private suspend fun handleDisconnection() {
        _isConnected.value = false
        _connectionState.value = "Disconnected"
        pingJob?.cancel()
    }
    
    /**
     * Cleanup resources
     */
    suspend fun cleanup() {
        disconnect()
    }
}
