package com.divinixx.zenflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.divinixx.zenflow.ui.navigation.ZenFlowDestinations
import com.divinixx.zenflow.ui.components.touchpad.TouchpadView
import com.divinixx.zenflow.ui.theme.ZenFlowTheme
import com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        TouchpadHeader(
            isConnected = uiState.isConnected,
            connectionState = uiState.connectionState,
            onShowSettings = { showSettings = true }
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
                    navController.navigate(ZenFlowDestinations.HOME)
                }
            )
        }
    }

    // Settings Bottom Sheet
    if (showSettings) {
        TouchpadSettingsSheet(
            settings = uiState.touchpadSettings,
            onDismiss = { showSettings = false },
            onUpdateSensitivity = viewModel::updateSensitivity
        )
    }
}

@Composable
private fun TouchpadHeader(
    isConnected: Boolean,
    connectionState: String,
    onShowSettings: () -> Unit
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
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = "Connection Status",
                    tint = if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = connectionState,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
        }

        IconButton(onClick = onShowSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
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
                containerColor = MaterialTheme.colorScheme.surfaceContainer
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Connect to PC to use touchpad",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToConnection,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Connect",
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
            )
            Text(
                text = "Go to Connection",
                style = MaterialTheme.typography.labelLarge
            )
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
        ResponsiveActionButton(
            icon = Icons.Default.Mouse,
            label = "Left Click",
            onStartAction = { viewModel.startLeftClick() },
            onStopAction = { viewModel.stopLeftClick() }
        )

        // Scroll Up Button
        ResponsiveActionButton(
            icon = Icons.Default.KeyboardArrowUp,
            label = "Scroll Up",
            onStartAction = { viewModel.startScrollUp() },
            onStopAction = { viewModel.stopScrollUp() }
        )

        // Scroll Down Button  
        ResponsiveActionButton(
            icon = Icons.Default.KeyboardArrowDown,
            label = "Scroll Down",
            onStartAction = { viewModel.startScrollDown() },
            onStopAction = { viewModel.stopScrollDown() }
        )

        ResponsiveActionButton(
            icon = Icons.Default.TouchApp,
            label = "Right Click",
            onStartAction = { viewModel.startRightClick() },
            onStopAction = { viewModel.stopRightClick() }
        )
    }
}



@Composable
private fun ResponsiveActionButton(
    icon: ImageVector,
    label: String,
    onStartAction: () -> Unit,
    onStopAction: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    // Track press state to handle continuous action
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    onStartAction()
                }
                is PressInteraction.Release -> {
                    isPressed = false
                    onStopAction()
                }
                is PressInteraction.Cancel -> {
                    isPressed = false
                    onStopAction()
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
                    if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
                    else MaterialTheme.colorScheme.surfaceContainer,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { /* Handled by interaction source */ },
                interactionSource = interactionSource,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isPressed) MaterialTheme.colorScheme.onPrimary 
                          else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isPressed) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TouchpadSettingsSheet(
    settings: com.divinixx.zenflow.ui.viewmodel.TouchpadSettings,
    onDismiss: () -> Unit,
    onUpdateSensitivity: (Float) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sensitivity Slider
            Text(
                text = "Sensitivity: ${(settings.sensitivity * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = settings.sensitivity,
                onValueChange = onUpdateSensitivity,
                valueRange = 0.1f..2.0f,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
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
