package com.divinixx.zenflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divinixx.zenflow.network.KtorWebSocketManager
import com.divinixx.zenflow.ui.components.touchpad.TouchpadListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val logMessages: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TouchpadViewModel @Inject constructor(
    private val webSocketManager: KtorWebSocketManager
) : ViewModel(), TouchpadListener {
    
    // Private state flows
    private val _touchpadSettings = MutableStateFlow(TouchpadSettings())
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    
    // Scroll jobs for continuous scrolling
    private var scrollUpJob: Job? = null
    private var scrollDownJob: Job? = null
    
    // Click jobs for continuous clicking
    private var leftClickJob: Job? = null
    private var rightClickJob: Job? = null
    
    // Keyboard jobs for continuous key pressing
    private var keyRepeatJob: Job? = null
    private var mediaControlJob: Job? = null
    
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
        
        // Use optimized mouse message sending for smoother movement
        viewModelScope.launch {
            webSocketManager.sendMouseMessage(
                mapOf(
                    "type" to "mouse",
                    "action" to "move",
                    "data" to data
                )
            )
        }
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

    // Scroll functions for continuous scrolling
    fun startScrollUp() {
        stopAllScrolling() // Stop any existing scroll
        scrollUpJob = viewModelScope.launch {
            addLogMessage("⬆️ Scroll up started")
            while (true) {
                if (_touchpadSettings.value.enableScrolling) {
                    sendMouseCommand("scroll", mapOf("deltaX" to 0.0, "deltaY" to -2.0))
                }
                delay(40) // Scroll every 40ms for smooth scrolling (25fps)
            }
        }
    }

    fun startScrollDown() {
        stopAllScrolling() // Stop any existing scroll
        scrollDownJob = viewModelScope.launch {
            addLogMessage("⬇️ Scroll down started")
            while (true) {
                if (_touchpadSettings.value.enableScrolling) {
                    sendMouseCommand("scroll", mapOf("deltaX" to 0.0, "deltaY" to 2.0))
                }
                delay(40) // Scroll every 40ms for smooth scrolling (25fps)
            }
        }
    }

    fun stopScrollUp() {
        scrollUpJob?.cancel()
        scrollUpJob = null
        addLogMessage("⬆️ Scroll up stopped")
    }

    fun stopScrollDown() {
        scrollDownJob?.cancel()
        scrollDownJob = null
        addLogMessage("⬇️ Scroll down stopped")
    }

    // Continuous click functions
    fun startLeftClick() {
        stopAllClicking() // Stop any existing clicking
        leftClickJob = viewModelScope.launch {
            addLogMessage("🖱️ Continuous left click started")
            while (true) {
                sendMouseCommand("left_click", mapOf("x" to 0, "y" to 0))
                delay(100) // Click every 100ms for rapid clicking
            }
        }
    }

    fun startRightClick() {
        stopAllClicking() // Stop any existing clicking
        if (_touchpadSettings.value.enableRightClick) {
            rightClickJob = viewModelScope.launch {
                addLogMessage("🖱️ Continuous right click started")
                while (true) {
                    sendMouseCommand("right_click", mapOf("x" to 0, "y" to 0))
                    delay(100) // Click every 100ms for rapid clicking
                }
            }
        }
    }

    fun stopLeftClick() {
        leftClickJob?.cancel()
        leftClickJob = null
        addLogMessage("🖱️ Left click stopped")
    }

    fun stopRightClick() {
        rightClickJob?.cancel()
        rightClickJob = null
        addLogMessage("🖱️ Right click stopped")
    }

    private fun stopAllClicking() {
        leftClickJob?.cancel()
        rightClickJob?.cancel()
        leftClickJob = null
        rightClickJob = null
    }

    // Media Control functions
    fun startVolumeUp() {
        stopMediaControls()
        mediaControlJob = viewModelScope.launch {
            addLogMessage("🔊 Volume up started")
            while (true) {
                sendKeyCombo("volume_up")
                delay(150) // Volume change every 150ms
            }
        }
    }

    fun startVolumeDown() {
        stopMediaControls()
        mediaControlJob = viewModelScope.launch {
            addLogMessage("🔉 Volume down started")
            while (true) {
                sendKeyCombo("volume_down")
                delay(150) // Volume change every 150ms
            }
        }
    }

    fun stopVolumeUp() {
        stopMediaControls()
        addLogMessage("🔊 Volume up stopped")
    }

    fun stopVolumeDown() {
        stopMediaControls()
        addLogMessage("🔉 Volume down stopped")
    }

    // Media playback controls (single press)
    fun mediaPlayPause() {
        addLogMessage("🎵 Media Play/Pause button pressed")
        sendKeyCombo("media_play")
        addLogMessage("⏯️ Play/Pause toggled")
    }

    fun mediaNext() {
        addLogMessage("🎵 Media Next button pressed")
        sendKeyCombo("media_next")
        addLogMessage("⏭️ Next track")
    }

    fun mediaPrevious() {
        addLogMessage("🎵 Media Previous button pressed")
        sendKeyCombo("media_previous")
        addLogMessage("⏮️ Previous track")
    }

    fun mediaMute() {
        sendKeyCombo("volume_mute")
        addLogMessage("🔇 Volume muted/unmuted")
    }

    // Backspace controls
    fun startBackspaceRepeat() {
        stopKeyRepeat()
        keyRepeatJob = viewModelScope.launch {
            addLogMessage("⌫ Backspace repeat started")
            while (true) {
                sendKeyboardInput("BACKSPACE", "press")
                delay(100) // Moderate repeat rate for backspace
            }
        }
    }

    fun stopBackspaceRepeat() {
        keyRepeatJob?.cancel()
        keyRepeatJob = null
        addLogMessage("⌫ Backspace repeat stopped")
    }

    // Continuous key pressing for special keys
    fun startKeyRepeat(key: String) {
        stopKeyRepeat()
        keyRepeatJob = viewModelScope.launch {
            addLogMessage("🔄 Key repeat started: $key")
            while (true) {
                sendKeyboardInput(key, "press")
                delay(50) // Fast repeat rate for navigation keys
            }
        }
    }

    fun stopKeyRepeat() {
        keyRepeatJob?.cancel()
        keyRepeatJob = null
        addLogMessage("🔄 Key repeat stopped")
    }

    // Continuous shortcut pressing
    fun startShortcutRepeat(combination: String) {
        stopKeyRepeat() // Stop any existing repeat
        keyRepeatJob = viewModelScope.launch {
            addLogMessage("🔄 Shortcut repeat started: $combination")
            while (true) {
                sendKeyCombo(combination)
                delay(200) // Slower repeat for shortcuts
            }
        }
    }

    private fun stopMediaControls() {
        mediaControlJob?.cancel()
        mediaControlJob = null
    }

    private fun stopAllScrolling() {
        scrollUpJob?.cancel()
        scrollDownJob?.cancel()
        scrollUpJob = null
        scrollDownJob = null
    }
    
    private fun stopAllActions() {
        stopAllScrolling()
        stopAllClicking()
        stopKeyRepeat()
        stopMediaControls()
    }

    override fun onCleared() {
        super.onCleared()
        stopAllActions()
        viewModelScope.launch {
            webSocketManager.cleanup()
        }
    }
    // Settings modification functions

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

    // Performance optimization functions
    fun updateSensitivity(sensitivity: Float) {
        val clampedSensitivity = sensitivity.coerceIn(0.1f, 5.0f)
        _touchpadSettings.value = _touchpadSettings.value.copy(sensitivity = clampedSensitivity)
        addLogMessage("🎯 Sensitivity updated to $clampedSensitivity")
    }
    
    fun toggleHighPerformanceMode(enabled: Boolean) {
        // This could be used to adjust various performance settings
        addLogMessage("⚡ High performance mode: ${if (enabled) "ON" else "OFF"}")
    }

    // Connection management functions
    fun connect(ipAddress: String) {
        addLogMessage("🔍 Connect called with IP: '$ipAddress'")
        if (ipAddress.isBlank()) {
            addLogMessage("❌ Cannot connect: IP address is empty")
            return
        }
        connect(ipAddress, 8080) // Default port
    }
    
    fun connect(ipAddress: String, port: Int) {
        addLogMessage("🔍 Connect called with IP: '$ipAddress', Port: $port")
        if (ipAddress.isBlank()) {
            addLogMessage("❌ Cannot connect: IP address is empty")
            return
        }
        viewModelScope.launch {
            addLogMessage("🔄 Connecting to $ipAddress:$port...")
            val success = webSocketManager.connect(ipAddress, port)
            if (success) {
                addLogMessage("✅ Connected to $ipAddress:$port")
            } else {
                addLogMessage("❌ Failed to connect to $ipAddress:$port")
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
        addLogMessage("⌨️ Sending keyboard input: $key ($action)")
        viewModelScope.launch {
            val keyboardData = mapOf(
                "type" to "keyboard",
                "action" to action,
                "key" to key,
                "timestamp" to System.currentTimeMillis()
            )
            
            addLogMessage("🔗 Keyboard message prepared: $keyboardData")
            try {
                val success = webSocketManager.sendMessage(keyboardData)
                if (success) {
                    addLogMessage("✅ Keyboard input sent successfully: $key ($action)")
                } else {
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
        addLogMessage("📤 Attempting to send key combo: $combination")
        viewModelScope.launch {
            val comboData = mapOf(
                "type" to "keyboard",
                "action" to "combo",
                "combination" to combination,
                "timestamp" to System.currentTimeMillis()
            )
            
            addLogMessage("🔗 WebSocket message prepared: $comboData")
            try {
                val success = webSocketManager.sendMessage(comboData)
                if (success) {
                    addLogMessage("✅ Key combination sent successfully: $combination")
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
}
