package com.divinixx.zenflow.ui.components.keyboard

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * System Keyboard Input Component
 * Uses Android's native keyboard for text input and provides shortcuts for special keys
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemKeyboardView(
    modifier: Modifier = Modifier,
    listener: VirtualKeyboardListener? = null,
    isConnected: Boolean = false,
    viewModel: com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel? = null
) {
    var textInput by remember { mutableStateOf("") }
    var isKeyboardVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Text Input",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { newValue ->
                        // Auto-send each character as it's typed
                        if (newValue.length > textInput.length && isConnected) {
                            val newChar = newValue.last()
                            listener?.onTextInput(newChar.toString())
                        }
                        // Handle backspace
                        else if (newValue.length < textInput.length && isConnected) {
                            listener?.onKeyPressed("BackSpace")
                        }
                        textInput = newValue
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { 
                        Text(
                            "Type here",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Start typing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    enabled = isConnected,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            listener?.onKeyPressed("ENTER", false)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Backspace Button
                    ResponsiveBackspaceButton(
                        onStartPress = { 
                            viewModel?.startBackspaceRepeat()
                        },
                        onStopPress = { 
                            viewModel?.stopBackspaceRepeat()
                        },
                        onSingleTap = { 
                            if (isConnected) {
                                listener?.onKeyPressed("BACKSPACE", false)
                            }
                            // Also remove last character from local text field
                            if (textInput.isNotEmpty()) {
                                textInput = textInput.dropLast(1)
                            }
                        },
                        isConnected = isConnected,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Clear Button
                    FilledTonalButton(
                        onClick = { textInput = "" },
                        enabled = textInput.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear, 
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Clear",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Status Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "Connected - Type to send" else "Disconnected",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
    }
}

@Composable
private fun ShortcutButton(
    shortcut: KeyboardShortcut,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp), // Increased height for better visibility
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4a4a4a), // Lighter color for better visibility
            disabledContainerColor = Color(0xFF2a2a2a)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = shortcut.label,
                modifier = Modifier.size(24.dp), // Increased icon size
                tint = if (enabled) Color.White else Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = shortcut.label,
                fontSize = 12.sp, // Increased font size
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color.White else Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class KeyboardShortcut(
    val label: String,
    val combo: String,
    val icon: ImageVector
)

private val commonShortcuts = listOf(
    // Essential Text Operations
    KeyboardShortcut("Copy", "ctrl+c", Icons.Default.ContentCopy),
    KeyboardShortcut("Paste", "ctrl+v", Icons.Default.ContentPaste),
    KeyboardShortcut("Cut", "ctrl+x", Icons.Default.ContentCut),
    KeyboardShortcut("Select All", "ctrl+a", Icons.Default.SelectAll),
    
    // Navigation
    KeyboardShortcut("Alt+Tab", "alt+tab", Icons.AutoMirrored.Filled.KeyboardTab),
    KeyboardShortcut("Backspace", "backspace", Icons.AutoMirrored.Filled.Backspace)
)

/**
 * Responsive Backspace Button with continuous press support
 */
@Composable
private fun ResponsiveBackspaceButton(
    onStartPress: () -> Unit,
    onStopPress: () -> Unit,
    onSingleTap: () -> Unit,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }
    
    // Track press state for continuous vs single action
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    pressStartTime = System.currentTimeMillis()
                    delay(200) // Wait 200ms to determine if it's a long press
                    if (isPressed) {
                        onStartPress() // Start continuous press
                    }
                }
                is PressInteraction.Release -> {
                    val pressDuration = System.currentTimeMillis() - pressStartTime
                    if (isPressed) {
                        if (pressDuration < 200) {
                            onSingleTap() // Quick tap
                        } else {
                            onStopPress() // Stop continuous press
                        }
                    }
                    isPressed = false
                }
                is PressInteraction.Cancel -> {
                    if (isPressed) {
                        onStopPress()
                    }
                    isPressed = false
                }
            }
        }
    }
    
    FilledTonalButton(
        onClick = { /* Handled by interaction source */ },
        enabled = isConnected,
        modifier = modifier,
        interactionSource = interactionSource,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isPressed) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = if (isPressed)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onTertiaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Backspace",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
