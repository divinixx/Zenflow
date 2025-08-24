package com.divinixx.zenflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divinixx.zenflow.network.KtorWebSocketManager
import com.divinixx.zenflow.ui.components.touchpad.TouchpadListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Touchpad Settings data class
 */
data class TouchpadSettings(
    val sensitivity: Float = 1.0f,
    val enableScrolling: Boolean = true,
    val enableRightClick: Boolean = true,
    val enableDoubleClick: Boolean = true,
    val leftHandedMode: Boolean = false
)

/**
 * Combined UI State for TouchpadScreen
 */
data class TouchpadUiState(
    val isConnected: Boolean = false,
    val connectionState: String = "Disconnected",
    val touchpadSettings: TouchpadSettings = TouchpadSettings(),
    val logMessages: List<String> = emptyList()
)

@HiltViewModel
class TouchpadViewModel @Inject constructor(
    private val webSocketManager: KtorWebSocketManager
) : ViewModel(), TouchpadListener {
    
    // Private state flows
    private val _touchpadSettings = MutableStateFlow(TouchpadSettings())
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    
    // Combined UI state using combine operator for better performance
    val uiState: StateFlow<TouchpadUiState> = combine(
        webSocketManager.isConnected,
        webSocketManager.connectionState,
        _touchpadSettings,
        _logMessages
    ) { isConnected, connectionState, touchpadSettings, logMessages ->
        TouchpadUiState(
            isConnected = isConnected,
            connectionState = connectionState,
            touchpadSettings = touchpadSettings,
            logMessages = logMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TouchpadUiState()
    )

    init {
        // Collect WebSocket errors
        viewModelScope.launch {
            webSocketManager.errors.collect { error ->
                addLogMessage("❌ WebSocket Error: $error")
            }
        }
        
        // Collect WebSocket messages
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                addLogMessage("📨 Received: $message")
            }
        }
    }

    // TouchpadListener implementation for gesture handling
    override fun onMove(deltaX: Float, deltaY: Float) {
        val sensitivity = _touchpadSettings.value.sensitivity
        val adjustedDeltaX = deltaX * sensitivity
        val adjustedDeltaY = deltaY * sensitivity
        
        val data = mapOf(
            "deltaX" to adjustedDeltaX,
            "deltaY" to adjustedDeltaY
        )
        
        // Use async for high-frequency mouse movement (fire and forget)
        webSocketManager.sendMessageAsync(
            mapOf(
                "type" to "mouse",
                "action" to "move",
                "data" to data
            ),
            viewModelScope
        )
    }

    override fun onLeftClick(x: Float, y: Float) {
        sendMouseCommand("left_click", mapOf("x" to x, "y" to y))
    }

    override fun onRightClick(x: Float, y: Float) {
        if (_touchpadSettings.value.enableRightClick) {
            sendMouseCommand("right_click", mapOf("x" to x, "y" to y))
        }
    }

    override fun onDoubleClick(x: Float, y: Float) {
        if (_touchpadSettings.value.enableDoubleClick) {
            sendMouseCommand("double_click", mapOf("x" to x, "y" to y))
        }
    }

    override fun onScroll(deltaX: Float, deltaY: Float) {
        if (_touchpadSettings.value.enableScrolling) {
            val data = mapOf(
                "deltaX" to deltaX,
                "deltaY" to deltaY
            )
            sendMouseCommand("scroll", data)
        }
    }

    // Manual click functions for quick actions
    fun performLeftClick() {
        sendMouseCommand("left_click", mapOf("x" to 0, "y" to 0))
        addLogMessage("🖱️ Left click performed")
    }

    fun performRightClick() {
        if (_touchpadSettings.value.enableRightClick) {
            sendMouseCommand("right_click", mapOf("x" to 0, "y" to 0))
            addLogMessage("🖱️ Right click performed")
        }
    }

    fun performDoubleClick() {
        if (_touchpadSettings.value.enableDoubleClick) {
            sendMouseCommand("double_click", mapOf("x" to 0, "y" to 0))
            addLogMessage("🖱️ Double click performed")
        }
    }

    // Settings management functions
    fun updateSensitivity(sensitivity: Float) {
        val currentSettings = _touchpadSettings.value
        _touchpadSettings.value = currentSettings.copy(sensitivity = sensitivity)
        addLogMessage("⚙️ Sensitivity updated: ${(sensitivity * 100).toInt()}%")
    }

    fun toggleScrolling(enabled: Boolean) {
        val currentSettings = _touchpadSettings.value
        _touchpadSettings.value = currentSettings.copy(enableScrolling = enabled)
        addLogMessage("⚙️ Scrolling ${if (enabled) "enabled" else "disabled"}")
    }

    fun toggleRightClick(enabled: Boolean) {
        val currentSettings = _touchpadSettings.value
        _touchpadSettings.value = currentSettings.copy(enableRightClick = enabled)
        addLogMessage("⚙️ Right click ${if (enabled) "enabled" else "disabled"}")
    }

    fun toggleDoubleClick(enabled: Boolean) {
        val currentSettings = _touchpadSettings.value
        _touchpadSettings.value = currentSettings.copy(enableDoubleClick = enabled)
        addLogMessage("⚙️ Double click ${if (enabled) "enabled" else "disabled"}")
    }

    fun toggleLeftHanded(enabled: Boolean) {
        val currentSettings = _touchpadSettings.value
        _touchpadSettings.value = currentSettings.copy(leftHandedMode = enabled)
        addLogMessage("⚙️ Left-handed mode ${if (enabled) "enabled" else "disabled"}")
    }

    fun clearLogs() {
        viewModelScope.launch {
            _logMessages.value = emptyList()
            addLogMessage("🗑️ Logs cleared")
        }
    }

    // Connection management functions
    fun connect(ipAddress: String) {
        viewModelScope.launch {
            addLogMessage("🔄 Connecting to $ipAddress...")
            val success = webSocketManager.connect(ipAddress)
            if (success) {
                addLogMessage("✅ Connected to $ipAddress")
            } else {
                addLogMessage("❌ Failed to connect to $ipAddress")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            webSocketManager.disconnect()
            addLogMessage("🔌 Disconnected from server")
        }
    }

    fun sendTestMessageAsync() {
        viewModelScope.launch {
            val testMessage = mapOf(
                "type" to "test",
                "message" to "Hello from Android!",
                "timestamp" to System.currentTimeMillis()
            )
            
            val success = webSocketManager.sendMessage(testMessage)
            if (success) {
                addLogMessage("📤 Test message sent successfully")
            } else {
                addLogMessage("❌ Failed to send test message")
            }
        }
    }

    // Keyboard functionality
    fun sendKeyboardInput(key: String, action: String) {
        viewModelScope.launch {
            val keyboardData = mapOf(
                "type" to "keyboard",
                "action" to action,
                "key" to key,
                "timestamp" to System.currentTimeMillis()
            )
            
            try {
                val success = webSocketManager.sendMessage(keyboardData)
                if (!success) {
                    addLogMessage("❌ Failed to send keyboard input: $key")
                }
            } catch (e: Exception) {
                addLogMessage("❌ Error sending keyboard input: ${e.message}")
            }
        }
    }

    fun sendTextInput(text: String) {
        viewModelScope.launch {
            val textData = mapOf(
                "type" to "keyboard",
                "action" to "type",
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )
            
            try {
                val success = webSocketManager.sendMessage(textData)
                if (success) {
                    addLogMessage("📝 Text input sent: $text")
                } else {
                    addLogMessage("❌ Failed to send text input")
                }
            } catch (e: Exception) {
                addLogMessage("❌ Error sending text input: ${e.message}")
            }
        }
    }

    fun sendKeyCombo(combination: String) {
        viewModelScope.launch {
            val comboData = mapOf(
                "type" to "keyboard",
                "action" to "combo",
                "combination" to combination,
                "timestamp" to System.currentTimeMillis()
            )
            
            try {
                val success = webSocketManager.sendMessage(comboData)
                if (success) {
                    addLogMessage("🎹 Key combination sent: $combination")
                } else {
                    addLogMessage("❌ Failed to send key combination: $combination")
                }
            } catch (e: Exception) {
                addLogMessage("❌ Error sending key combination: ${e.message}")
            }
        }
    }

    // Helper function to send mouse commands with error handling
    private fun sendMouseCommand(action: String, data: Map<String, Any>) {
        viewModelScope.launch {
            val messageData = mapOf(
                "type" to "mouse",
                "action" to action,
                "data" to data
            )
            
            try {
                val success = webSocketManager.sendMessage(messageData)
                if (!success) {
                    addLogMessage("❌ Failed to send $action command")
                }
            } catch (e: Exception) {
                addLogMessage("❌ Error sending $action: ${e.message}")
            }
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            webSocketManager.cleanup()
        }
    }
}
