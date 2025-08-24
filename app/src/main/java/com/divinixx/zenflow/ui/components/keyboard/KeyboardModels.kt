package com.divinixx.zenflow.ui.components.keyboard

/**
 * Definition of a keyboard key with all its properties
 */
data class KeyDefinition(
    val label: String,
    val keyCode: String,
    val width: Float = 1.0f,
    val height: Float = 1.0f,
    val isModifier: Boolean = false,
    val shiftLabel: String? = null,
    val altLabel: String? = null,
    val keyType: KeyType = KeyType.NORMAL,
    val backgroundColor: String? = null,
    val textColor: String? = null
)

/**
 * Types of keyboard keys for different styling and behavior
 */
enum class KeyType {
    NORMAL,      // Regular alphanumeric keys
    MODIFIER,    // Ctrl, Alt, Shift, etc.
    FUNCTION,    // F1-F12 keys
    SPECIAL,     // Enter, Tab, Escape, etc.
    SPACE,       // Space bar
    BACKSPACE,   // Backspace key
    DELETE,      // Delete key
    ARROW        // Arrow keys
}

/**
 * Keyboard layout containing all key definitions and arrangements
 */
data class KeyboardLayout(
    val name: String,
    val mainKeys: List<List<KeyDefinition>>,
    val functionKeys: List<KeyDefinition>,
    val modifierKeys: List<KeyDefinition>,
    val numpadKeys: List<List<KeyDefinition>>? = null,
    val isCompact: Boolean = false
)

/**
 * Current state of modifier keys
 */
data class ModifierState(
    val shift: Boolean = false,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val win: Boolean = false,
    val fn: Boolean = false,
    val capsLock: Boolean = false
)

/**
 * Keyboard settings and preferences
 */
data class KeyboardSettings(
    val layout: String = "QWERTY",
    val showFunctionKeys: Boolean = true,
    val showNumpad: Boolean = false,
    val keyHeight: Float = 48f,
    val keySpacing: Float = 4f,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val autoRepeat: Boolean = true,
    val repeatDelay: Long = 500L,
    val repeatInterval: Long = 50L,
    val theme: KeyboardTheme = KeyboardTheme.DARK
)

/**
 * Keyboard visual themes
 */
enum class KeyboardTheme {
    DARK,
    LIGHT,
    SYSTEM,
    CUSTOM
}
