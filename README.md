# Zenflow Remote - WebSocket Communication

## Overview
Zenflow Remote is an Android app that provides WebSocket-based communication with remote servers (typically PCs). The app uses a robust WebSocketManager for reliable real-time communication.

## Features

### 🔗 Connection Management
- **Auto-reconnection**: Automatically attempts to reconnect on connection loss (max 5 attempts)
- **Connection timeout**: 30-second timeout for connection attempts
- **Keep-alive mechanism**: Automatic ping/pong to maintain connection
- **Thread-safe operations**: All network operations on background threads

### 📨 Message Handling
- **JSON message format**: Structured message protocol
- **Message queuing**: Offline messages are queued and sent when reconnected
- **Error handling**: Robust error handling for network and JSON issues
- **Message validation**: Incoming messages are validated for proper format

### 🎛️ User Interface
- **Dark theme**: Professional dark UI (#1a1a1a background)
- **Real-time status**: Connection status with color indicators
- **Live logging**: Scrollable log of all connection events
- **IP validation**: Input validation for IP addresses
- **Test functionality**: Send test messages when connected

## WebSocket Message Format

All messages follow this JSON structure:

```json
{
    "type": "test|ping|command|response|error",
    "action": "specific_action_name",
    "data": {
        // Message-specific data
    },
    "timestamp": "2025-08-23T00:27:00Z"
}
```

### Message Types
- **test**: Test messages (ping/pong)
- **ping**: Keep-alive messages
- **command**: Remote control commands
- **response**: Server responses
- **error**: Error messages

## Usage

### Basic Connection
1. Enter the PC's IP address (e.g., 192.168.1.100)
2. Tap "Connect" button
3. Monitor connection status and logs
4. Send test messages when connected

### WebSocketManager API

```kotlin
// Create manager and set callbacks
val webSocketManager = WebSocketManager()
webSocketManager.setCallbacks(object : WebSocketManager.ConnectionCallbacks {
    override fun onConnected() { /* Handle connection */ }
    override fun onDisconnected() { /* Handle disconnection */ }
    override fun onMessage(message: String) { /* Handle message */ }
    override fun onError(error: String) { /* Handle error */ }
})

// Connect to server
webSocketManager.connect("192.168.1.100", 8080)

// Send message
val data = mapOf(
    "type" to "command",
    "action" to "volume_up",
    "data" to mapOf("level" to 10)
)
webSocketManager.sendMessage(data)

// Disconnect
webSocketManager.disconnect()

// Cleanup
webSocketManager.cleanup()
```

## Technical Implementation

### Architecture
- **MVVM Pattern**: ViewModel manages WebSocket connection
- **LiveData**: Reactive UI updates
- **Dependency Injection**: Hilt for DI
- **ViewBinding**: Type-safe view access

### Threading
- **Main Thread**: UI updates and callbacks
- **Background Threads**: Network operations
- **Handler**: Message posting between threads

### Error Handling
- Network timeouts and failures
- Invalid JSON message formats
- Connection lost scenarios
- WebSocket exceptions

### Memory Management
- Weak references to prevent memory leaks
- Message queue size limiting (50 messages max)
- Proper cleanup on view model destruction

## Dependencies

```gradle
// WebSocket and JSON
implementation("org.java-websocket:Java-WebSocket:1.6.0")
implementation("com.google.code.gson:gson:2.13.1")

// Android Architecture Components
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")

// Material Design
implementation("com.google.android.material:material:1.12.0")

// Dependency Injection
implementation("com.google.dagger:hilt-android:2.48")
```

## Network Requirements
- **Internet Permission**: Required for network access
- **Network State Permission**: For connection monitoring
- **Cleartext Traffic**: Enabled for non-HTTPS WebSocket connections

## Default Configuration
- **WebSocket Port**: 8080
- **Connection Timeout**: 30 seconds
- **Reconnect Attempts**: 5 maximum
- **Reconnect Delay**: 5 seconds
- **Ping Interval**: 30 seconds

## Security Considerations
- Uses unencrypted WebSocket (ws://) - suitable for local networks
- For production use, implement WSS (wss://) for encrypted communication
- Consider authentication mechanisms for secure connections

## Future Enhancements
- SSL/TLS support (wss://)
- Authentication system
- Custom message protocols
- File transfer capabilities
- Multiple connection support
