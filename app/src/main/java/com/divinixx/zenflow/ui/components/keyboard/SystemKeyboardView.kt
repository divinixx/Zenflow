package com.divinixx.zenflow.ui.components.keyboard

import androidx.compose.foundation.*
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

/**
 * System Keyboard Input Component
 * Uses Android's native keyboard for text input and provides shortcuts for special keys
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemKeyboardView(
    modifier: Modifier = Modifier,
    listener: VirtualKeyboardListener? = null,
    isConnected: Boolean = false
) {
    var textInput by remember { mutableStateOf("") }
    var isKeyboardVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1a1a1a))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Text Input",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Type here (auto-sends to PC)") },
                    placeholder = { Text("Start typing...") },
                    enabled = isConnected,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Cyan,
                        focusedLabelColor = Color.Cyan,
                        cursorColor = Color.Cyan
                    )
                )
                
                // Clear button (still useful for clearing the input field)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { textInput = "" },
                        enabled = textInput.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.LightGray
                        )
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        
        // Quick Shortcuts Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Shortcuts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Common shortcuts - more responsive layout
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Fixed height to work with parent scroll
                ) {
                    items(commonShortcuts.size) { index ->
                        val shortcut = commonShortcuts[index]
                        ShortcutButton(
                            shortcut = shortcut,
                            enabled = isConnected,
                            onClick = { listener?.onKeyCombo(shortcut.combo) }
                        )
                    }
                }
            }
        }
        
        // Media Controls Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Media Controls",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Media control buttons
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp) // Fixed height to work with parent scroll
                ) {
                    items(mediaControls.size) { index ->
                        val mediaControl = mediaControls[index]
                        ShortcutButton(
                            shortcut = mediaControl,
                            enabled = isConnected,
                            onClick = { listener?.onKeyCombo(mediaControl.combo) }
                        )
                    }
                }
            }
        }
        
        // Special Keys Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2d2d2d)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Special Keys",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp) // Fixed height to work with parent scroll
                ) {
                    items(specialKeys.size) { index ->
                        val key = specialKeys[index]
                        SpecialKeyButton(
                            key = key,
                            enabled = isConnected,
                            onClick = { listener?.onKeyPressed(key.keyCode) }
                        )
                    }
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

@Composable
private fun SpecialKeyButton(
    key: SpecialKey,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Increased height for better visibility
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF505050), // Lighter color for better visibility
            disabledContainerColor = Color(0xFF2a2a2a)
        ),
        shape = RoundedCornerShape(8.dp), // More rounded corners
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = key.label,
            fontSize = 14.sp, // Increased font size
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.White else Color.Gray
        )
    }
}

data class KeyboardShortcut(
    val label: String,
    val combo: String,
    val icon: ImageVector
)

data class SpecialKey(
    val label: String,
    val keyCode: String
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

private val mediaControls = listOf(
    KeyboardShortcut("Play/Pause", "media_play_pause", Icons.Default.PlayArrow),
    KeyboardShortcut("Next Track", "media_next", Icons.Default.SkipNext),
    KeyboardShortcut("Previous", "media_previous", Icons.Default.SkipPrevious),
    KeyboardShortcut("Volume Up", "volume_up", Icons.AutoMirrored.Filled.VolumeUp),
    KeyboardShortcut("Volume Down", "volume_down", Icons.AutoMirrored.Filled.VolumeDown),
    KeyboardShortcut("Mute", "volume_mute", Icons.AutoMirrored.Filled.VolumeOff)
)

private val specialKeys = listOf(
    SpecialKey("Enter", "ENTER"),
    SpecialKey("Tab", "TAB"),
    SpecialKey("Escape", "ESCAPE"),
    SpecialKey("Delete", "DELETE"),
    SpecialKey("Space", "SPACE"),
    SpecialKey("Home", "HOME"),
    SpecialKey("End", "END"),
    SpecialKey("Page Up", "PAGE_UP"),
    SpecialKey("Page Down", "PAGE_DOWN")
)
