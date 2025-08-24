package com.divinixx.zenflow.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.divinixx.zenflow.ui.components.keyboard.SystemKeyboardView
import com.divinixx.zenflow.ui.components.keyboard.VirtualKeyboardListener
import com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel
import kotlinx.coroutines.delay

/**
 * Keyboard Screen with virtual keyboard for remote PC control
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardScreen(
    navController: NavController,
    viewModel: TouchpadViewModel = hiltViewModel()
) {
    // Single UI state collection for optimal performance
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Local state for UI controls
    var showSettings by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
    ) {
        // Header Section - Fixed at top
        KeyboardHeader(
            isConnected = uiState.isConnected,
            connectionState = uiState.connectionState,
            onShowSettings = { showSettings = true },
            onShowLogs = { showLogs = true },
            modifier = Modifier.padding(16.dp)
        )
        
        // Main Content - Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isConnected) {
                ConnectedKeyboardContent(
                    viewModel = viewModel
                )
            } else {
                DisconnectedContent(
                    onNavigateToConnection = {
                        navController.navigate("connection")
                    }
                )
            }
        }
    }
    
    // Settings Bottom Sheet
    if (showSettings) {
        KeyboardSettingsSheet(
            onDismiss = { showSettings = false }
        )
    }
    
    // Logs Bottom Sheet
    if (showLogs) {
        LogsBottomSheet(
            logs = uiState.logMessages,
            onDismiss = { showLogs = false },
            onClearLogs = viewModel::clearLogs
        )
    }
}

@Composable
private fun KeyboardHeader(
    isConnected: Boolean,
    connectionState: String,
    onShowSettings: () -> Unit,
    onShowLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Virtual Keyboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = "Connection Status",
                    tint = if (isConnected) Color.Green else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = connectionState,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isConnected) Color.Green else Color.Red
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onShowLogs) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Show Logs",
                    tint = Color.White
                )
            }
            
            IconButton(onClick = onShowSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ConnectedKeyboardContent(
    viewModel: TouchpadViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Keyboard
        SystemKeyboardView(
            modifier = Modifier.fillMaxWidth(),
            listener = object : VirtualKeyboardListener {
                override fun onKeyPressed(key: String, isModifier: Boolean) {
                    viewModel.sendKeyboardInput(key, "press")
                }
                
                override fun onKeyReleased(key: String, isModifier: Boolean) {
                    viewModel.sendKeyboardInput(key, "release")
                }
                
                override fun onTextInput(text: String) {
                    viewModel.sendTextInput(text)
                }
                
                override fun onModifierChanged(modifier: String, pressed: Boolean) {
                    // Modifier state is handled internally by SystemKeyboardView
                }
                
                override fun onKeyCombo(combination: String) {
                    viewModel.sendKeyCombo(combination)
                }
            },
            isConnected = true
        )
        
        // Media Controls Section
        MediaControlsSection(viewModel = viewModel)
        
        // Special Keys Section
        SpecialKeysSection(viewModel = viewModel)
        
        // Quick Shortcuts Section
        QuickShortcutsSection(viewModel = viewModel)
    }
}

@Composable
private fun DisconnectedContent(
    onNavigateToConnection: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Keyboard,
            contentDescription = "Keyboard",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Connect to PC to use keyboard",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateToConnection,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Connect",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Go to Connection")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyboardSettingsSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2d2d2d)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Keyboard Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "System Keyboard Integration",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "• Use the text input field to open your device's keyboard\n" +
                      "• Type normally and tap 'Send Text' to send to PC\n" +
                      "• Use shortcut buttons for common key combinations\n" +
                      "• Special keys are available for navigation and functions",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResponsiveKeyboardButton(
    icon: ImageVector,
    label: String,
    onStartAction: () -> Unit,
    onStopAction: () -> Unit,
    onSingleTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }
    
    // Track press state to handle continuous action vs single tap
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    pressStartTime = System.currentTimeMillis()
                    kotlinx.coroutines.delay(300) // Wait 300ms to determine long press
                    if (isPressed) {
                        onStartAction() // Long press detected
                    }
                }
                is PressInteraction.Release -> {
                    val pressDuration = System.currentTimeMillis() - pressStartTime
                    if (isPressed && pressDuration < 300) {
                        onSingleTap() // Short tap
                    } else if (isPressed) {
                        onStopAction() // Long press release
                    }
                    isPressed = false
                }
                is PressInteraction.Cancel -> {
                    if (isPressed) {
                        onStopAction()
                    }
                    isPressed = false
                }
            }
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isPressed) Color(0xFF5d5d5d) else Color(0xFF3d3d3d),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { /* Handled by interaction source */ },
                interactionSource = interactionSource,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isPressed) Color.White else Color.Gray
        )
    }
}

@Composable
private fun MediaControlsSection(
    viewModel: TouchpadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Media Controls",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Volume Controls (Press and hold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.Default.VolumeDown,
                    label = "Vol-",
                    onStartAction = { viewModel.startVolumeDown() },
                    onStopAction = { viewModel.stopVolumeDown() },
                    onSingleTap = { viewModel.sendKeyCombo("volume_down") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.VolumeOff,
                    label = "Mute",
                    onStartAction = { },
                    onStopAction = { },
                    onSingleTap = { viewModel.mediaMute() }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Vol+",
                    onStartAction = { viewModel.startVolumeUp() },
                    onStopAction = { viewModel.stopVolumeUp() },
                    onSingleTap = { viewModel.sendKeyCombo("volume_up") }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Playback Controls (Single tap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.Default.SkipPrevious,
                    label = "Previous",
                    onStartAction = { },
                    onStopAction = { },
                    onSingleTap = { viewModel.mediaPrevious() }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.PlayArrow,
                    label = "Play/Pause",
                    onStartAction = { },
                    onStopAction = { },
                    onSingleTap = { viewModel.mediaPlayPause() }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.SkipNext,
                    label = "Next",
                    onStartAction = { },
                    onStopAction = { },
                    onSingleTap = { viewModel.mediaNext() }
                )
            }
        }
    }
}

@Composable
private fun SpecialKeysSection(
    viewModel: TouchpadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Special Keys",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Navigation Keys
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onStartAction = { viewModel.startKeyRepeat("HOME") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("HOME", "press") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "Page Up",
                    onStartAction = { viewModel.startKeyRepeat("PAGE_UP") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("PAGE_UP", "press") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "Page Dn",
                    onStartAction = { viewModel.startKeyRepeat("PAGE_DOWN") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("PAGE_DOWN", "press") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.LastPage,
                    label = "End",
                    onStartAction = { viewModel.startKeyRepeat("END") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("END", "press") }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Function Keys
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "Esc",
                    onStartAction = { viewModel.startKeyRepeat("ESCAPE") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("ESCAPE", "press") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Delete,
                    label = "Del",
                    onStartAction = { viewModel.startKeyRepeat("DELETE") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("DELETE", "press") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Tab,
                    label = "Tab",
                    onStartAction = { viewModel.startKeyRepeat("TAB") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("TAB", "press") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.KeyboardReturn,
                    label = "Enter",
                    onStartAction = { viewModel.startKeyRepeat("ENTER") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("ENTER", "press") }
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutsSection(
    viewModel: TouchpadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Shortcuts",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // First row - Basic shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    onStartAction = { viewModel.

                    startShortcutRepeat("ctrl+c") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+c") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.ContentPaste,
                    label = "Paste",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+v") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+v") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Undo,
                    label = "Undo",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+z") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+z") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Redo,
                    label = "Redo",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+y") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+y") }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Second row - System shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Tab,
                    label = "Alt+Tab",
                    onStartAction = { viewModel.startShortcutRepeat("alt+tab") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("alt+tab") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.FindReplace,
                    label = "Find",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+f") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+f") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.SelectAll,
                    label = "Select All",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+a") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+a") }
                )
                
                ResponsiveKeyboardButton(
                    icon = Icons.Default.Save,
                    label = "Save",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+s") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+s") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsBottomSheet(
    logs: List<String>,
    onDismiss: () -> Unit,
    onClearLogs: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2d2d2d)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keyboard Logs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                TextButton(onClick = onClearLogs) {
                    Text("Clear", color = Color.Red)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                logs.forEach { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                if (logs.isEmpty()) {
                    Text(
                        text = "No logs available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
