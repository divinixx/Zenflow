package com.divinixx.zenflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divinixx.zenflow.network.KtorWebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State data class to hold all screen state
 */
data class RemoteUiState(
    val isConnected: Boolean = false,
    val connectionState: String = "Disconnected",
    val logMessages: List<String> = emptyList()
)

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val webSocketManager: KtorWebSocketManager
) : ViewModel() {

    // Private state flows
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())

    // Combined UI state using combine operator for better performance
    val uiState: StateFlow<RemoteUiState> = combine(
        webSocketManager.isConnected,
        webSocketManager.connectionState,
        _logMessages
    ) { isConnected, connectionState, logMessages ->
        RemoteUiState(
            isConnected = isConnected,
            connectionState = connectionState,
            logMessages = logMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemoteUiState()
    )

    // Legacy individual flows for backward compatibility (if needed)
    val isConnected: StateFlow<Boolean> = webSocketManager.isConnected
    val connectionState: StateFlow<String> = webSocketManager.connectionState
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    init {
        setupWebSocketObservers()
    }

    private fun setupWebSocketObservers() {
        // Observe messages
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                addLogMessage("📨 Received: $message")
            }
        }
        
        // Observe errors
        viewModelScope.launch {
            webSocketManager.errors.collect { error ->
                addLogMessage("❌ Error: $error")
            }
        }
        
        // Observe connection state changes
        viewModelScope.launch {
            webSocketManager.isConnected.collect { isConnected ->
                if (isConnected) {
                    addLogMessage("✅ Successfully connected to server")
                } else {
                    addLogMessage("ℹ️ Disconnected from server")
                }
            }
        }
    }

    fun connect(ipAddress: String) {
        viewModelScope.launch {
            addLogMessage("🔗 Attempting to connect to $ipAddress:8080...")
            val success = webSocketManager.connect(ipAddress, 8080)
            if (!success) {
                addLogMessage("❌ Failed to establish connection")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            addLogMessage("🔌 Disconnecting from server...")
            webSocketManager.disconnect()
        }
    }

    suspend fun sendTestMessage() {
        val testData = mapOf(
            "type" to "test",
            "action" to "ping",
            "data" to mapOf(
                "client" to "android",
                "version" to "1.0",
                "timestamp" to System.currentTimeMillis()
            )
        )
        
        val success = webSocketManager.sendMessage(testData)
        if (success) {
            addLogMessage("📤 Test message sent successfully")
        } else {
            addLogMessage("❌ Failed to send test message")
        }
    }

    fun sendTestMessageAsync() {
        viewModelScope.launch {
            sendTestMessage()
        }
    }

    private fun addLogMessage(message: String) {
        viewModelScope.launch {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val formattedMessage = "[$timestamp] $message"
            
            _logMessages.value = _logMessages.value + formattedMessage
            
            // Keep only last 50 messages to prevent memory issues
            if (_logMessages.value.size > 50) {
                _logMessages.value = _logMessages.value.takeLast(50)
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            _logMessages.value = emptyList()
            addLogMessage("🗑️ Logs cleared")
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            webSocketManager.cleanup()
        }
    }
}
