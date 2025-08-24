package com.divinixx.zenflow.ui.components.keyboard

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Virtual Keyboard Compose Component
 * Provides a full keyboard interface for remote PC control
 */
@Composable
fun VirtualKeyboardView(
    modifier: Modifier = Modifier,
    layout: KeyboardLayout = KeyboardLayouts.QWERTY,
    settings: KeyboardSettings = KeyboardSettings(),
    listener: VirtualKeyboardListener? = null,
    modifierState: ModifierState = ModifierState()
) {
    var currentModifierState by remember { mutableStateOf(modifierState) }
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(getKeyboardBackgroundColor(settings.theme))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(settings.keySpacing.dp)
    ) {
        // Function keys row (if enabled)
        if (settings.showFunctionKeys && layout.functionKeys.isNotEmpty()) {
            FunctionKeysRow(
                keys = layout.functionKeys,
                settings = settings,
                onKeyPressed = { key ->
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    listener?.onKeyPressed(key)
                }
            )
        }
        
        // Main keyboard rows
        layout.mainKeys.forEach { row ->
            KeyRow(
                keys = row,
                settings = settings,
                modifierState = currentModifierState,
                onKeyPressed = { key, keyDef ->
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    
                    if (keyDef.isModifier) {
                        currentModifierState = handleModifierKey(currentModifierState, key)
                        listener?.onModifierChanged(key, getModifierState(currentModifierState, key))
                    } else {
                        val finalKey = if (currentModifierState.shift && !keyDef.shiftLabel.isNullOrEmpty()) {
                            keyDef.shiftLabel!!
                        } else {
                            key
                        }
                        listener?.onKeyPressed(finalKey, keyDef.isModifier)
                    }
                },
                onKeyReleased = { key, keyDef ->
                    if (!keyDef.isModifier) {
                        listener?.onKeyReleased(key, keyDef.isModifier)
                    }
                }
            )
        }
        
        // Numpad (if enabled)
        if (settings.showNumpad && layout.numpadKeys != null) {
            Spacer(modifier = Modifier.height(8.dp))
            NumpadSection(
                keys = layout.numpadKeys,
                settings = settings,
                onKeyPressed = { key ->
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    listener?.onKeyPressed(key)
                }
            )
        }
    }
}

@Composable
private fun FunctionKeysRow(
    keys: List<KeyDefinition>,
    settings: KeyboardSettings,
    onKeyPressed: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(keys.size) { index ->
            KeyButton(
                keyDef = keys[index],
                settings = settings,
                modifier = Modifier.width(60.dp),
                onPressed = { onKeyPressed(keys[index].keyCode) },
                onReleased = { }
            )
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<KeyDefinition>,
    settings: KeyboardSettings,
    modifierState: ModifierState,
    onKeyPressed: (String, KeyDefinition) -> Unit,
    onKeyReleased: (String, KeyDefinition) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(settings.keySpacing.dp)
    ) {
        keys.forEach { keyDef ->
            val weight = keyDef.width
            KeyButton(
                keyDef = keyDef,
                settings = settings,
                modifierState = modifierState,
                modifier = Modifier.weight(weight),
                onPressed = { onKeyPressed(keyDef.keyCode, keyDef) },
                onReleased = { onKeyReleased(keyDef.keyCode, keyDef) }
            )
        }
    }
}

@Composable
private fun NumpadSection(
    keys: List<List<KeyDefinition>>,
    settings: KeyboardSettings,
    onKeyPressed: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(settings.keySpacing.dp)
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(settings.keySpacing.dp)
            ) {
                row.forEach { keyDef ->
                    KeyButton(
                        keyDef = keyDef,
                        settings = settings,
                        modifier = Modifier
                            .width((50 * keyDef.width).dp)
                            .height((50 * keyDef.height).dp),
                        onPressed = { onKeyPressed(keyDef.keyCode) },
                        onReleased = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    keyDef: KeyDefinition,
    settings: KeyboardSettings,
    modifier: Modifier = Modifier,
    modifierState: ModifierState = ModifierState(),
    onPressed: () -> Unit,
    onReleased: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    
    // Handle modifier key states
    val isModifierActive = when (keyDef.keyCode) {
        "LEFT_SHIFT", "RIGHT_SHIFT", "SHIFT" -> modifierState.shift
        "LEFT_CTRL", "RIGHT_CTRL", "CTRL" -> modifierState.ctrl
        "LEFT_ALT", "RIGHT_ALT", "ALT" -> modifierState.alt
        "LEFT_WIN", "RIGHT_WIN", "WIN" -> modifierState.win
        "CAPS_LOCK" -> modifierState.capsLock
        else -> false
    }
    
    val keyColors = getKeyColors(keyDef.keyType, settings.theme, isPressed, isModifierActive)
    val displayText = if (modifierState.shift && !keyDef.shiftLabel.isNullOrEmpty()) {
        keyDef.shiftLabel!!
    } else {
        keyDef.label
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed && settings.autoRepeat && !keyDef.isModifier) {
            delay(settings.repeatDelay)
            while (isPressed) {
                onPressed()
                delay(settings.repeatInterval)
            }
        }
    }
    
    Box(
        modifier = modifier
            .height(settings.keyHeight.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(keyColors.background)
            .border(
                width = 1.dp,
                color = keyColors.border,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onPressed()
            }
            .background(
                if (isPressed) keyColors.pressed else Color.Transparent,
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = keyColors.text,
            fontSize = when {
                keyDef.label.length > 4 -> 10.sp
                keyDef.label.length > 2 -> 12.sp
                else -> 14.sp
            },
            fontWeight = if (keyDef.keyType == KeyType.MODIFIER) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
            onReleased()
        }
    }
}

private data class KeyColors(
    val background: Color,
    val text: Color,
    val border: Color,
    val pressed: Color
)

private fun getKeyColors(keyType: KeyType, theme: KeyboardTheme, isPressed: Boolean, isActive: Boolean): KeyColors {
    return when (theme) {
        KeyboardTheme.DARK -> when (keyType) {
            KeyType.NORMAL -> KeyColors(
                background = if (isActive) Color(0xFF4CAF50) else Color(0xFF2d2d2d),
                text = Color.White,
                border = Color(0xFF555555),
                pressed = Color(0xFF1976D2)
            )
            KeyType.MODIFIER -> KeyColors(
                background = if (isActive) Color(0xFF4CAF50) else Color(0xFF404040),
                text = Color.White,
                border = Color(0xFF666666),
                pressed = Color(0xFF1976D2)
            )
            KeyType.FUNCTION -> KeyColors(
                background = Color(0xFF1a1a1a),
                text = Color(0xFFBBBBBB),
                border = Color(0xFF333333),
                pressed = Color(0xFF1976D2)
            )
            KeyType.SPECIAL -> KeyColors(
                background = Color(0xFF3d3d3d),
                text = Color.White,
                border = Color(0xFF555555),
                pressed = Color(0xFF1976D2)
            )
            else -> KeyColors(
                background = Color(0xFF2d2d2d),
                text = Color.White,
                border = Color(0xFF555555),
                pressed = Color(0xFF1976D2)
            )
        }
        KeyboardTheme.LIGHT -> when (keyType) {
            KeyType.MODIFIER -> KeyColors(
                background = if (isActive) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                text = Color.Black,
                border = Color(0xFFBDBDBD),
                pressed = Color(0xFF2196F3)
            )
            else -> KeyColors(
                background = Color.White,
                text = Color.Black,
                border = Color(0xFFBDBDBD),
                pressed = Color(0xFF2196F3)
            )
        }
        else -> getKeyColors(keyType, KeyboardTheme.DARK, isPressed, isActive)
    }
}

private fun getKeyboardBackgroundColor(theme: KeyboardTheme): Color {
    return when (theme) {
        KeyboardTheme.DARK -> Color(0xFF1a1a1a)
        KeyboardTheme.LIGHT -> Color(0xFFF5F5F5)
        else -> Color(0xFF1a1a1a)
    }
}

private fun handleModifierKey(currentState: ModifierState, key: String): ModifierState {
    return when (key) {
        "LEFT_SHIFT", "RIGHT_SHIFT", "SHIFT" -> currentState.copy(shift = !currentState.shift)
        "LEFT_CTRL", "RIGHT_CTRL", "CTRL" -> currentState.copy(ctrl = !currentState.ctrl)
        "LEFT_ALT", "RIGHT_ALT", "ALT" -> currentState.copy(alt = !currentState.alt)
        "LEFT_WIN", "RIGHT_WIN", "WIN" -> currentState.copy(win = !currentState.win)
        "CAPS_LOCK" -> currentState.copy(capsLock = !currentState.capsLock)
        else -> currentState
    }
}

private fun getModifierState(modifierState: ModifierState, key: String): Boolean {
    return when (key) {
        "LEFT_SHIFT", "RIGHT_SHIFT", "SHIFT" -> modifierState.shift
        "LEFT_CTRL", "RIGHT_CTRL", "CTRL" -> modifierState.ctrl
        "LEFT_ALT", "RIGHT_ALT", "ALT" -> modifierState.alt
        "LEFT_WIN", "RIGHT_WIN", "WIN" -> modifierState.win
        "CAPS_LOCK" -> modifierState.capsLock
        else -> false
    }
}
