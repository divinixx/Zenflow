package com.divinixx.zenflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.divinixx.zenflow.ui.components.touchpad.TouchpadView
import com.divinixx.zenflow.ui.theme.ZenFlowTheme
import com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel

/**
 * Professional TouchpadScreen with error-free ViewModel integration
 * Uses single UI state for optimal performance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchpadScreen(
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        TouchpadHeader(
            isConnected = uiState.isConnected,
            connectionState = uiState.connectionState,
            onShowSettings = { showSettings = true },
            onShowLogs = { showLogs = true }
        )
        
        // Main Content based on connection state
        if (uiState.isConnected) {
            ConnectedContent(
                viewModel = viewModel,
                settings = uiState.touchpadSettings
            )
        } else {
            DisconnectedContent(
                onNavigateToConnection = {
                    navController.navigate("connection")
                }
            )
        }
    }
    
    // Settings Bottom Sheet
    if (showSettings) {
        TouchpadSettingsSheet(
            settings = uiState.touchpadSettings,
            onDismiss = { showSettings = false },
            onUpdateSensitivity = viewModel::updateSensitivity,
            onToggleScrolling = viewModel::toggleScrolling,
            onToggleRightClick = viewModel::toggleRightClick,
            onToggleDoubleClick = viewModel::toggleDoubleClick,
            onToggleLeftHanded = viewModel::toggleLeftHanded
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
private fun TouchpadHeader(
    isConnected: Boolean,
    connectionState: String,
    onShowSettings: () -> Unit,
    onShowLogs: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Touchpad Control",
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
private fun ConnectedContent(
    viewModel: TouchpadViewModel,
    settings: com.divinixx.zenflow.ui.viewmodel.TouchpadSettings
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Touchpad Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2d2d2d)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        TouchpadView(context).apply {
                            setTouchpadListener(viewModel)
                            // Apply settings
                            setSensitivity(settings.sensitivity)
                            setScrollingEnabled(settings.enableScrolling)
                            setRightClickEnabled(settings.enableRightClick)
                            setDoubleClickEnabled(settings.enableDoubleClick)
                            setLeftHandedMode(settings.leftHandedMode)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Quick Actions
        QuickActionsRow(viewModel = viewModel)
        
        // Status Bar
        TouchpadStatusBar(settings = settings)
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
            imageVector = Icons.Default.Tablet,
            contentDescription = "Touchpad",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Connect to PC to use touchpad",
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
                imageVector = Icons.Default.Settings,
                contentDescription = "Connect",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Go to Connection")
        }
    }
}

@Composable
private fun QuickActionsRow(
    viewModel: TouchpadViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.Mouse,
            label = "Left Click",
            onClick = { viewModel.performLeftClick() }
        )
        
        QuickActionButton(
            icon = Icons.Default.TouchApp,
            label = "Right Click",
            onClick = { viewModel.performRightClick() }
        )
        
        QuickActionButton(
            icon = Icons.Default.DoubleArrow,
            label = "Double Click",
            onClick = { viewModel.performDoubleClick() }
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

@Composable
private fun TouchpadStatusBar(
    settings: com.divinixx.zenflow.ui.viewmodel.TouchpadSettings
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2d2d2d)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusItem("Sensitivity", "${(settings.sensitivity * 100).toInt()}%")
            StatusItem("Scroll", if (settings.enableScrolling) "ON" else "OFF")
            StatusItem("Right Click", if (settings.enableRightClick) "ON" else "OFF")
            StatusItem("Mode", if (settings.leftHandedMode) "Left" else "Right")
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TouchpadSettingsSheet(
    settings: com.divinixx.zenflow.ui.viewmodel.TouchpadSettings,
    onDismiss: () -> Unit,
    onUpdateSensitivity: (Float) -> Unit,
    onToggleScrolling: (Boolean) -> Unit,
    onToggleRightClick: (Boolean) -> Unit,
    onToggleDoubleClick: (Boolean) -> Unit,
    onToggleLeftHanded: (Boolean) -> Unit
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
                text = "Touchpad Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sensitivity Slider
            Text(
                text = "Sensitivity: ${(settings.sensitivity * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = settings.sensitivity,
                onValueChange = onUpdateSensitivity,
                valueRange = 0.1f..2.0f,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Toggle Settings
            SettingToggle(
                title = "Enable Scrolling",
                checked = settings.enableScrolling,
                onCheckedChange = onToggleScrolling
            )
            
            SettingToggle(
                title = "Enable Right Click",
                checked = settings.enableRightClick,
                onCheckedChange = onToggleRightClick
            )
            
            SettingToggle(
                title = "Enable Double Click",
                checked = settings.enableDoubleClick,
                onCheckedChange = onToggleDoubleClick
            )
            
            SettingToggle(
                title = "Left-handed Mode",
                checked = settings.leftHandedMode,
                onCheckedChange = onToggleLeftHanded
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
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
                    text = "Touchpad Logs",
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

@Preview(showBackground = true, backgroundColor = 0xFF1a1a1a)
@Composable
private fun TouchpadScreenConnectedPreview() {
    ZenFlowTheme {
        TouchpadScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1a1a1a)
@Composable
private fun TouchpadScreenDisconnectedPreview() {
    ZenFlowTheme {
        DisconnectedContent(
            onNavigateToConnection = {}
        )
    }
}
