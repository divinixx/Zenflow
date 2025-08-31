package com.divinixx.zenflow.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.divinixx.zenflow.ui.navigation.ZenFlowDestinations
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
    ) {
        // Header Section - Fixed at top
        KeyboardHeader(
            isConnected = uiState.isConnected,
            connectionState = uiState.connectionState,
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
                        navController.navigate(ZenFlowDestinations.HOME)
                    }
                )
            }
        }
    }

}

@Composable
private fun KeyboardHeader(
    isConnected: Boolean,
    connectionState: String,
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
    }
}

@Composable
private fun ConnectedKeyboardContent(
    viewModel: TouchpadViewModel
) {
    var selectedCarousel by remember { mutableStateOf<String?>(null) }
    
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
            isConnected = true,
            viewModel = viewModel
        )

        // Carousels Section
        KeyboardCarouselsSection(
            viewModel = viewModel,
            selectedCarousel = selectedCarousel,
            onCarouselSelected = { selectedCarousel = it }
        )
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Connect to PC to use keyboard",
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
                imageVector = Icons.Default.Link,
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
private fun ResponsiveKeyboardButton(
    icon: ImageVector,
    label: String,
    onStartAction: () -> Unit,
    onStopAction: () -> Unit,
    onSingleTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }

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
                    if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.surfaceContainer,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { /* Handled by interaction source */ },
                interactionSource = interactionSource,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isPressed) MaterialTheme.colorScheme.onPrimary 
                          else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isPressed) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SimpleKeyboardButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Simple immediate response button
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    onClick() // Immediate response
                }
                is PressInteraction.Release -> {
                    isPressed = false
                }
                is PressInteraction.Cancel -> {
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
                    if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.surfaceContainer,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { /* Handled by interaction source */ },
                interactionSource = interactionSource,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isPressed) MaterialTheme.colorScheme.onPrimary 
                          else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isPressed) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SpecialKeysContent(
    viewModel: TouchpadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Special Keys",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                    icon = Icons.AutoMirrored.Filled.LastPage,
                    label = "End",
                    onStartAction = { viewModel.startKeyRepeat("END") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("END", "press") }
                )
            }

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
                    icon = Icons.AutoMirrored.Filled.KeyboardReturn,
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
 fun QuickShortcutsSection(
    viewModel: TouchpadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Shortcuts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // First row - Basic shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+c") },
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
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+z") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+z") }
                )

                ResponsiveKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    label = "Redo",
                    onStartAction = { viewModel.startShortcutRepeat("ctrl+y") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyCombo("ctrl+y") }
                )
            }

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

@Composable
private fun KeyboardCarouselsSection(
    viewModel: TouchpadViewModel,
    selectedCarousel: String?,
    onCarouselSelected: (String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Control Panels",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        // Horizontal carousel of control categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            
            CarouselCard(
                title = "Media Controls",
                icon = Icons.Default.PlayArrow,
                isSelected = selectedCarousel == "media",
                onClick = { 
                    onCarouselSelected(if (selectedCarousel == "media") null else "media")
                }
            )
            
            CarouselCard(
                title = "Special Keys",
                icon = Icons.Default.Keyboard,
                isSelected = selectedCarousel == "special",
                onClick = { 
                    onCarouselSelected(if (selectedCarousel == "special") null else "special")
                }
            )
            
            CarouselCard(
                title = "Quick Shortcuts",
                icon = Icons.Default.Speed,
                isSelected = selectedCarousel == "shortcuts",
                onClick = { 
                    onCarouselSelected(if (selectedCarousel == "shortcuts") null else "shortcuts")
                }
            )
            
            Spacer(modifier = Modifier.width(4.dp))
        }
        
        // Expanded content based on selection
        selectedCarousel?.let { carousel ->
            when (carousel) {
                "media" -> MediaControlsContent(viewModel = viewModel)
                "special" -> SpecialKeysContent(viewModel = viewModel)
                "shortcuts" -> QuickShortcutsSection(viewModel = viewModel)
            }
        }
    }
}


@Composable
private fun CarouselCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else 
                    MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary
                else 
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun MediaControlsContent(
    viewModel: TouchpadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Media Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Volume Controls (Press and hold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.VolumeDown,
                    label = "Vol-",
                    onStartAction = { viewModel.startVolumeDown() },
                    onStopAction = { viewModel.stopVolumeDown() },
                    onSingleTap = { viewModel.sendKeyCombo("volume_down") }
                )

                SimpleKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.VolumeOff,
                    label = "Mute",
                    onClick = { viewModel.mediaMute() }
                )

                ResponsiveKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    label = "Vol+",
                    onStartAction = { viewModel.startVolumeUp() },
                    onStopAction = { viewModel.stopVolumeUp() },
                    onSingleTap = { viewModel.sendKeyCombo("volume_up") }
                )
            }

            // Playback Controls (Single tap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SimpleKeyboardButton(
                    icon = Icons.Default.SkipPrevious,
                    label = "Previous",
                    onClick = { viewModel.mediaPrevious() }
                )

                SimpleKeyboardButton(
                    icon = Icons.Default.PlayArrow,
                    label = "Play/Pause",
                    onClick = { viewModel.mediaPlayPause() }
                )

                SimpleKeyboardButton(
                    icon = Icons.Default.SkipNext,
                    label = "Next",
                    onClick = { viewModel.mediaNext() }
                )
            }

            // Video Navigation Controls (Left/Right arrows for seeking)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResponsiveKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    label = "Seek Back",
                    onStartAction = { viewModel.startKeyRepeat("LEFT") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("LEFT", "press") }
                )

                SimpleKeyboardButton(
                    icon = Icons.Default.Fullscreen,
                    label = "Fullscreen",
                    onClick = { viewModel.sendKeyboardInput("F11", "press") }
                )

                ResponsiveKeyboardButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    label = "Seek Forward",
                    onStartAction = { viewModel.startKeyRepeat("RIGHT") },
                    onStopAction = { viewModel.stopKeyRepeat() },
                    onSingleTap = { viewModel.sendKeyboardInput("RIGHT", "press") }
                )
            }
        }
    }
}





