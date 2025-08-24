# ZenFlow Remote - Virtual Keyboard Documentation

## Overview
The ZenFlow Remote app now includes a comprehensive virtual keyboard feature that allows you to control your PC's keyboard remotely from your Android device.

## Features

### 🎹 Virtual Keyboard Components

#### 1. **Complete Keyboard Layouts**
- **QWERTY Layout**: Full desktop-style keyboard with all keys
- **Compact Layout**: Optimized for smaller screens
- **Function Keys**: F1-F12 keys for shortcuts and special functions
- **Numpad**: Optional numeric keypad for data entry

#### 2. **Key Types and Functionality**
- **Regular Keys**: A-Z, 0-9, symbols, and punctuation
- **Modifier Keys**: Ctrl, Alt, Shift, Win key with state tracking
- **Special Keys**: Enter, Tab, Escape, Backspace, Delete
- **Arrow Keys**: Navigation keys (↑↓←→)
- **Function Keys**: F1-F12 for application shortcuts

#### 3. **Advanced Features**
- **Modifier State Tracking**: Visual indication of active modifiers (Shift, Ctrl, Alt)
- **Key Combinations**: Support for common shortcuts (Ctrl+C, Ctrl+V, Alt+Tab)
- **Auto-repeat**: Hold keys for continuous input
- **Haptic Feedback**: Tactile response for better typing experience
- **Visual Feedback**: Key press animations and states

#### 4. **Keyboard Settings**
- **Layout Selection**: Switch between QWERTY and Compact layouts
- **Function Keys Toggle**: Show/hide function key row
- **Numpad Toggle**: Show/hide numeric keypad
- **Haptic Feedback**: Enable/disable vibration on key press
- **Auto-repeat**: Configure key repeat behavior
- **Theme Support**: Dark/Light themes

## Usage

### 🚀 Getting Started

1. **Connect to PC**: Ensure your PC and Android device are on the same network
2. **Start Server**: Run the test server on your PC
3. **Connect**: Use the Connection tab to connect to your PC
4. **Navigate**: Switch to the Keyboard tab
5. **Type Away**: Use the virtual keyboard to control your PC

### 📱 Navigation

The app has three main tabs:
- **Connection**: Setup and manage PC connection
- **Touchpad**: Mouse control and gestures
- **Keyboard**: Virtual keyboard for text input

### ⌨️ Keyboard Usage

#### Basic Typing
- Tap keys to type normally
- Long press for key repeat (where applicable)
- Visual feedback shows pressed keys

#### Modifier Keys
- **Shift**: Tap to toggle, affects next key(s)
- **Ctrl**: Hold for combinations like Ctrl+C
- **Alt**: Use for Alt+Tab and other shortcuts
- **Win**: Access Windows key functions

#### Quick Actions
Pre-configured buttons for common shortcuts:
- **Ctrl+C**: Copy
- **Ctrl+V**: Paste  
- **Ctrl+Z**: Undo
- **Alt+Tab**: Switch windows

#### Special Features
- **Caps Lock**: Toggle for persistent capital letters
- **Function Keys**: Access F1-F12 for application shortcuts
- **Arrow Keys**: Navigate through text and interfaces
- **Numpad**: Numeric data entry (when enabled)

## Technical Implementation

### 🏗️ Architecture

#### Components
1. **VirtualKeyboardView**: Main keyboard UI component
2. **KeyboardLayouts**: Predefined key arrangements
3. **VirtualKeyboardListener**: Event handling interface
4. **KeyboardModels**: Data classes for keys and settings
5. **KeyboardScreen**: Screen container with settings and status

#### Key Data Flow
1. User presses virtual key
2. KeyboardView generates event
3. VirtualKeyboardListener processes event
4. TouchpadViewModel sends to WebSocket
5. PC receives and processes keyboard input

#### WebSocket Protocol
Keyboard messages use the following format:
```json
{
  "type": "keyboard",
  "action": "press|release|type|combo",
  "key": "A|ENTER|CTRL|...",
  "text": "hello world",
  "combination": "ctrl+c",
  "timestamp": 1234567890
}
```

### 🔧 Customization

#### Adding New Layouts
Create new layouts in `KeyboardLayouts.kt`:
```kotlin
val CUSTOM_LAYOUT = KeyboardLayout(
    name = "Custom",
    mainKeys = listOf(/* key rows */),
    functionKeys = listOf(/* F keys */),
    modifierKeys = listOf(/* modifiers */)
)
```

#### Custom Key Definitions
```kotlin
KeyDefinition(
    label = "A",           // Display text
    keyCode = "A",         // Sent key code
    width = 1.0f,          // Relative width
    isModifier = false,    // Modifier key flag
    shiftLabel = "A",      // Shift variant
    keyType = KeyType.NORMAL
)
```

#### Themes and Styling
Modify colors in `VirtualKeyboardView.kt`:
```kotlin
private fun getKeyColors(keyType: KeyType, theme: KeyboardTheme): KeyColors {
    // Custom color schemes
}
```

## Server Integration

### 🖥️ PC Server Requirements

The PC server should handle these keyboard message types:

1. **Key Press/Release**
   - `action`: "press" or "release"
   - `key`: Key code (e.g., "A", "ENTER", "CTRL")

2. **Text Input**
   - `action`: "type"
   - `text`: String to type

3. **Key Combinations**
   - `action`: "combo"
   - `combination`: Combo string (e.g., "ctrl+c")

### Example Server Response
```json
{
  "type": "response",
  "action": "keyboard_ack",
  "status": "success",
  "message": "Key press processed",
  "timestamp": "2025-08-23T10:30:00"
}
```

## Performance Considerations

### 🚀 Optimization Features

1. **Efficient Rendering**: Minimal recomposition using Jetpack Compose
2. **Event Batching**: Grouped key events for better performance
3. **Memory Management**: Optimized for keyboard layouts and states
4. **Touch Response**: Low-latency key press detection
5. **Battery Efficiency**: Optimized haptic feedback and animations

### Best Practices

- Use compact layout on smaller screens
- Disable haptic feedback to save battery
- Adjust key repeat settings for your typing style
- Use quick actions for frequently used shortcuts

## Troubleshooting

### Common Issues

1. **Keys Not Responding**
   - Check PC connection status
   - Verify server is running and receiving messages
   - Test with Connection tab first

2. **Modifier Keys Stuck**
   - Tap modifier keys again to reset state
   - Check status bar for current modifier states

3. **Layout Issues**
   - Switch to compact layout on small screens
   - Adjust device orientation
   - Check keyboard settings

4. **Performance Issues**
   - Disable haptic feedback
   - Reduce animation settings
   - Close other apps to free memory

### Debug Information

- Use the Logs section to see keyboard events
- Check connection status in header
- Monitor server responses for troubleshooting

## Future Enhancements

### 🔮 Planned Features

1. **International Keyboards**: Support for different language layouts
2. **Custom Shortcuts**: User-defined key combinations
3. **Gesture Typing**: Swipe-to-type functionality
4. **Voice Input**: Speech-to-text integration
5. **Predictive Text**: Auto-complete and suggestions
6. **Macro Recording**: Record and replay key sequences

### Community Contributions

Contributions are welcome! Areas for improvement:
- Additional keyboard layouts
- Accessibility features
- Performance optimizations
- UI/UX enhancements
- Documentation updates

---

## Support

For issues, feature requests, or contributions, please refer to the main project documentation or create an issue in the repository.

**Happy Typing! 🎉**
