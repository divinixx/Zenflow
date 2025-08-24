package com.divinixx.zenflow.ui.components.keyboard

/**
 * Interface for handling virtual keyboard events
 * Provides callbacks for key presses, text input, and modifier changes
 */
interface VirtualKeyboardListener {
    
    /**
     * Called when a key is pressed
     * @param key The key code or character
     * @param isModifier Whether this is a modifier key (Ctrl, Alt, etc.)
     */
    fun onKeyPressed(key: String, isModifier: Boolean = false)
    
    /**
     * Called when a key is released
     * @param key The key code or character
     * @param isModifier Whether this is a modifier key
     */
    fun onKeyReleased(key: String, isModifier: Boolean = false)
    
    /**
     * Called for direct text input
     * @param text The text to be typed
     */
    fun onTextInput(text: String)
    
    /**
     * Called when modifier key state changes
     * @param modifier The modifier key (ctrl, alt, shift, win)
     * @param pressed Whether the modifier is pressed or released
     */
    fun onModifierChanged(modifier: String, pressed: Boolean)
    
    /**
     * Called for special key combinations
     * @param combination The key combination (e.g., "ctrl+c", "alt+tab")
     */
    fun onKeyCombo(combination: String)
}
