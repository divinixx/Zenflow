<div align="center">

# 🌊 Zenflow Remote

<img src="https://img.shields.io/badge/Platform-Android-brightgreen?s### **📋 Prerequisites**
- Android device (API 24+)
- PC on the same network **OR** PC connected to phone's mobile hotspot
- Python 3.7+ (for test server)

### 🔧 **Setup Instructions**

#### **🌐 Option 1: Same WiFi Network**
```bash
# Both devices connected to same WiFi router
📱 Phone: 192.168.1.5  (WiFi)
💻 PC:    192.168.1.100 (WiFi)
🔌 Connect to: PC's IP (192.168.1.100:8080)
```

#### **🔥 Option 2: Mobile Hotspot (Recommended)**
```bash
# PC connected to phone's hotspot
📱 Phone: 192.168.43.1  (Hotspot host)
💻 PC:    192.168.43.2  (Hotspot client)
🔌 Connect to: Phone's IP (192.168.43.1:8080)
```or-the-badge&logo=android" alt="Platform">
<img src="https://img.shields.io/badge/Language-Kotlin-7c4dff?style=for-the-badge&logo=kotlin" alt="Language">
<img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285f4?style=for-the-badge&logo=jetpackcompose" alt="UI Framework">
<img src="https://img.shields.io/badge/Architecture-MVVM-ff6b35?style=for-the-badge" alt="Architecture">
<img src="https://img.shields.io/badge/WebSocket-Ktor-087cfa?style=for-the-badge" alt="WebSocket">

### ✨ Transform your phone into a wireless touchpad and keyboard for your PC ✨

*Experience seamless remote control with modern Android architecture and real-time WebSocket communication*

</div>

---

## 🚀 What is Zenflow?

**Zenflow Remote** is a sophisticated Android application that transforms your smartphone into a wireless touchpad and keyboard for your PC. Built with cutting-edge Android technologies, it provides low-latency, reliable remote control capabilities through WebSocket communication.

## ✨ Features Overview

### 🎯 **Core Functionality**
- 🖱️ **Advanced Touchpad Control** - Multi-touch gestures, precise cursor movement
- ⌨️ **Virtual Keyboard** - Full keyboard input with special keys support
- 🔗 **Real-time Connection** - WebSocket-based communication with auto-reconnect
- 📱 **Native Android UI** - Modern Material 3 design with dark theme

### �️ **Technical Excellence**
- 🚀 **Jetpack Compose UI** - Modern declarative UI framework
- 🧭 **MVVM Architecture** - Clean, maintainable code structure
- 💉 **Dependency Injection** - Hilt for robust dependency management
- ⚡ **Kotlin Coroutines** - Efficient asynchronous programming
- 🌐 **Ktor WebSocket** - Professional-grade networking

## 📱 What We've Built So Far

### 🏠 **Core Architecture**
```
📦 Zenflow Remote
├── 🎨 UI Layer (Jetpack Compose)
│   ├── 📺 Touchpad Screen - Advanced multi-touch control
│   ├── ⌨️ Keyboard Screen - Virtual keyboard interface
│   ├── 🔌 Connection Screen - Network setup and management
│   └── 🧭 Navigation - Seamless screen transitions
├── 🧠 ViewModel Layer (MVVM)
│   ├── TouchpadViewModel - Gesture and input processing
│   └── State Management - Reactive UI updates
├── 🌐 Network Layer
│   ├── KtorWebSocketManager - Modern coroutine-based networking
│   └── Connection Management - Auto-reconnect & error handling
└── 💉 Dependency Injection (Hilt)
    ├── NetworkModule - WebSocket dependencies
    └── DatabaseModule - Local storage setup
```

### 🎨 **User Interface Components**

#### **🖱️ Touchpad Interface**
- **Multi-touch Support** - Smooth cursor movement and gestures
- **Right-click Detection** - Context menu support
- **Scroll Gestures** - Two-finger scrolling
- **Drag & Drop** - File manipulation support
- **Connection Status** - Real-time connectivity indicators

#### **⌨️ Virtual Keyboard**
- **System Keyboard Integration** - Native Android keyboard support
- **Custom Virtual Keys** - Function keys (F1-F12), Ctrl, Alt, Win
- **Special Characters** - Full ASCII character set
- **Modifier Keys** - Shift, Ctrl, Alt combinations
- **Responsive Layout** - Adaptive to different screen sizes

#### **🔌 Connection Management**
- **IP Address Input** - Validation and connection setup
- **Connection Status** - Visual indicators and logging
- **Auto-discovery** - Network scanning capabilities
- **Settings Panel** - Connection preferences and debugging

### 🛠️ **Technical Implementation**

#### **📡 Network Architecture**
```kotlin
🌐 KtorWebSocketManager
├── 🔄 Auto-reconnection (3 attempts)
├── 💓 Keep-alive mechanism (30s ping)
├── ⚡ Coroutine-based async operations
├── 📊 Connection state flow
└── 🛡️ Error handling & recovery
```

#### **🎯 Message Protocol**
```json
{
  "type": "touchpad|keyboard|command",
  "action": "move|click|key_press",
  "data": {
    "x": 100, "y": 200,
    "button": "left|right|middle",
    "key": "a", "modifiers": ["ctrl"]
  },
  "timestamp": "2025-08-24T12:00:00Z"
}
```

#### **🏗️ Modern Android Stack**
- **🎨 Jetpack Compose** - Declarative UI with Material 3
- **🧭 Navigation Compose** - Type-safe navigation
- **📱 ViewBinding** - Safe view access
- **🔄 StateFlow/LiveData** - Reactive state management
- **⚡ Kotlin Coroutines** - Structured concurrency
- **💉 Hilt** - Dependency injection framework

## 🚀 Quick Start Guide

### 📋 **Prerequisites**
- Android device (API 24+)
- PC on the same network
- Python 3.7+ (for test server)

### 🔧 **Setup Instructions**

#### **1️⃣ Run the Test Server**
```bash
# Start the included test server
./start_test_server.bat

# Or manually with Python
pip install websockets
python test_server.py
```

#### **2️⃣ Connect Your Device**
1. 📱 Open Zenflow Remote app
2. 🔍 Navigate to Connection screen
3. 📝 Enter your PC's IP address (e.g., `192.168.1.100`)
4. 🔌 Tap "Connect" button
5. ✅ Verify connection status

#### **3️⃣ Start Controlling**
- 🖱️ **Touchpad**: Swipe to move cursor, tap to click
- ⌨️ **Keyboard**: Type text, use virtual function keys
- 📊 **Monitor**: Check connection logs and status

### 🔥 **Mobile Hotspot Setup (No WiFi Required!)**

**Perfect for situations without WiFi access - use your phone as both controller and router!**

#### **📱 Setup Steps:**
1. **Enable Hotspot** on your phone (Settings → Hotspot & Tethering)
2. **Connect PC** to your phone's WiFi hotspot
3. **Find Phone's IP** - Usually `192.168.43.1` or `192.168.137.1`
4. **Run Server** on PC: `python test_server.py`
5. **Connect in App** using phone's hotspot IP address

#### **🌐 Network Configuration:**
```
📱 Phone (Hotspot):     192.168.43.1 (Router + Controller)
💻 PC (Connected):      192.168.43.2 (Server)
🔌 WebSocket Address:   ws://192.168.43.1:8080
🎯 Connection Type:     Direct local network
```

#### **✅ Advantages of Hotspot Mode:**
- 🚫 **No WiFi needed** - Works anywhere with cellular data
- ⚡ **Lower latency** - Direct device-to-device communication
- 🔒 **More secure** - Controlled network environment
- 🎯 **Predictable IPs** - Consistent IP address assignment

### 💻 **Development Setup**

```bash
# Clone the repository
git clone https://github.com/divinixx/zenflow.git
cd zenflow

# Open in Android Studio
# Build and run on device/emulator
./gradlew assembleDebug
```

## 🔧 Configuration & API

### **⚙️ WebSocket Manager API**
```kotlin
@Inject
class TouchpadViewModel(
    private val webSocketManager: KtorWebSocketManager
) : ViewModel() {
    
    // Connect to server
    suspend fun connect(ip: String, port: Int = 8080) {
        webSocketManager.connect(ip, port)
    }
    
    // Send touchpad movement
    fun sendMouseMove(deltaX: Float, deltaY: Float) {
        val message = MouseMessage(
            type = "touchpad",
            action = "move",
            data = MouseData(deltaX, deltaY)
        )
        webSocketManager.sendMessage(message)
    }
    
    // Send keyboard input
    fun sendKeyPress(key: String, modifiers: List<String> = emptyList()) {
        val message = KeyboardMessage(
            type = "keyboard", 
            action = "key_press",
            data = KeyData(key, modifiers)
        )
        webSocketManager.sendMessage(message)
    }
}
```

### **🔗 Connection States**
```kotlin
// Observe connection state
webSocketManager.connectionState.collect { state ->
    when (state) {
        "Connected" -> showConnectedUI()
        "Connecting" -> showLoadingUI()
        "Disconnected" -> showDisconnectedUI()
        "Error" -> showErrorUI()
    }
}
```

## 📚 Technical Deep Dive

### **🏛️ Architecture Overview**

```
🎨 Presentation Layer
├── 📱 Jetpack Compose UI
├── 🎭 Material 3 Theme
└── 🧭 Navigation Compose

🧠 Domain Layer  
├── 📊 ViewModels (MVVM)
├── 🔄 Use Cases
└── 📋 State Management

💾 Data Layer
├── 🌐 WebSocket Repository
├── 💉 Dependency Injection
└── 🏪 Local Storage (DataStore)
```

### **⚡ Performance Optimizations**

#### **🔄 State Management**
- **StateFlow**: Reactive UI updates with lifecycle awareness
- **collectAsStateWithLifecycle**: Automatic subscription management
- **Remember**: Efficient recomposition with state preservation

#### **🌐 Network Efficiency**
- **Ktor Coroutines**: Non-blocking async operations
- **Connection Pooling**: Efficient resource management
- **Automatic Reconnection**: Resilient network handling
- **Message Queuing**: Offline message handling

#### **🎨 UI Performance**
- **LazyColumn**: Efficient list rendering
- **State Hoisting**: Optimized component recomposition
- **ViewInterop**: AndroidView for custom components

### **🔒 Security Features**
- 🛡️ **Input Validation**: IP address and message validation
- 🔐 **Local Network Only**: Designed for trusted networks
- 📊 **Connection Logging**: Comprehensive activity tracking
- 🚫 **Error Boundaries**: Graceful error handling

### **📦 Dependencies & Libraries**

#### **🎯 Core Android**
```gradle
implementation("androidx.core:core-ktx:1.15.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
implementation("androidx.activity:activity-compose:1.9.3")
```

#### **🎨 UI Framework**
```gradle
implementation("androidx.compose.ui:ui:1.7.8")
implementation("androidx.material3:material3:1.3.1")
implementation("androidx.navigation:navigation-compose:2.8.5")
```

#### **🌐 Networking**
```gradle
implementation("io.ktor:ktor-client-core:3.2.3")
implementation("io.ktor:ktor-client-websockets:3.2.3")
implementation("com.google.code.gson:gson:2.13.1")
```

#### **💉 Dependency Injection**
```gradle
implementation("com.google.dagger:hilt-android:2.48")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```

## 🛣️ Roadmap & Future Features

### 🎯 **Next Phase Development**

#### **🔒 Enhanced Security**
- [ ] 🔐 **SSL/TLS Support** - Encrypted WebSocket connections (WSS)
- [ ] 🔑 **Authentication System** - User login and device pairing
- [ ] 🛡️ **Certificate Validation** - Secure connection verification

#### **🎮 Advanced Controls**
- [ ] 🎯 **Gesture Recognition** - Custom touchpad gestures
- [ ] 🔊 **Media Controls** - Volume, play/pause, skip
- [ ] 📺 **Display Controls** - Monitor switching, resolution changes
- [ ] 🖥️ **Multi-Monitor Support** - Extended desktop control

#### **📱 Enhanced UX**
- [ ] 🌈 **Custom Themes** - Personalized color schemes
- [ ] 🔧 **Advanced Settings** - Sensitivity, gesture configuration
- [ ] 📊 **Analytics Dashboard** - Usage statistics and insights
- [ ] 🔔 **Notifications** - Connection alerts and status updates

#### **🌐 Network Features**
- [ ] 🔍 **Auto-Discovery** - Automatic PC detection on network
- [ ] 📡 **Multiple Connections** - Support for multiple PCs
- [ ] ☁️ **Cloud Sync** - Settings backup and restoration
- [ ] 📱 **Hotspot Mode** - Direct device-to-device connection

### 📈 **Performance Goals**
- ⚡ **< 10ms Latency** - Ultra-responsive control
- 🔋 **Battery Optimization** - Extended usage time
- 📱 **Memory Efficiency** - < 50MB RAM usage
- 🌐 **Network Optimization** - Minimal bandwidth usage

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### **🐛 Bug Reports**
- Use GitHub Issues with detailed reproduction steps
- Include device info, Android version, and logs
- Provide network configuration details

### **✨ Feature Requests**
- Open GitHub Discussions for new ideas
- Provide use cases and implementation suggestions
- Consider backward compatibility

### **💻 Code Contributions**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow Kotlin coding standards
4. Add comprehensive tests
5. Submit a pull request

### **📝 Development Guidelines**
- Follow MVVM architecture patterns
- Use Jetpack Compose best practices
- Implement proper error handling
- Add documentation for public APIs
- Maintain 80%+ test coverage

## 📄 License

```
MIT License

Copyright (c) 2025 Zenflow Remote

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<div align="center">

## 🌟 **Zenflow Remote - Where Innovation Meets Simplicity** 🌟

### 📱 Turn your smartphone into the ultimate PC companion

**Built with ❤️ using modern Android architecture**

[![⭐ Star us on GitHub](https://img.shields.io/badge/⭐-Star_on_GitHub-yellow?style=for-the-badge)](https://github.com/divinixx/zenflow)
[![🐛 Report Issues](https://img.shields.io/badge/🐛-Report_Issues-red?style=for-the-badge)](https://github.com/divinixx/zenflow/issues)
[![💡 Suggest Features](https://img.shields.io/badge/💡-Suggest_Features-blue?style=for-the-badge)](https://github.com/divinixx/zenflow/discussions)

**Made by [@divinixx](https://github.com/divinixx)** | **Powered by Kotlin & Jetpack Compose**

</div>
