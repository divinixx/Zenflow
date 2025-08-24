package com.divinixx.zenflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
//import com.divinixx.zenflow.ui.components.
import com.divinixx.zenflow.ui.components.keyboard.SystemKeyboardView
import com.divinixx.zenflow.ui.components.keyboard.VirtualKeyboardListener
import com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel

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

@Composable
private fun QuickKeyboardActions(
    viewModel: TouchpadViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.ContentCopy,
            label = "Ctrl+C",
            onClick = { viewModel.sendKeyCombo("ctrl+c") }
        )
        
        QuickActionButton(
            icon = Icons.Default.ContentPaste,
            label = "Ctrl+V",
            onClick = { viewModel.sendKeyCombo("ctrl+v") }
        )
        
        QuickActionButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = "Ctrl+Z",
            onClick = { viewModel.sendKeyCombo("ctrl+z") }
        )
        
        QuickActionButton(
            icon = Icons.Default.Tab,
            label = "Alt+Tab",
            onClick = { viewModel.sendKeyCombo("alt+tab") }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(
                    Color(0xFF3d3d3d),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
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
