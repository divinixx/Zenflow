package com.divinixx.zenflow.ui.components.touchpad

/**
 * Callback interface for touchpad events
 * Provides mouse movement, click, and scroll events to the listener
 */
interface TouchpadListener {
    
    /**
     * Called when mouse movement is detected
     * @param deltaX Horizontal movement delta
     * @param deltaY Vertical movement delta
     */
    fun onMove(deltaX: Float, deltaY: Float)
    
    /**
     * Called when left click is detected
     * @param x Touch X coordinate
     * @param y Touch Y coordinate
     */
    fun onLeftClick(x: Float, y: Float)
    
    /**
     * Called when right click is detected
     * @param x Touch X coordinate
     * @param y Touch Y coordinate
     */
    fun onRightClick(x: Float, y: Float)
    
    /**
     * Called when double click is detected
     * @param x Touch X coordinate
     * @param y Touch Y coordinate
     */
    fun onDoubleClick(x: Float, y: Float)
    
    /**
     * Called when scroll is detected
     * @param deltaX Horizontal scroll delta
     * @param deltaY Vertical scroll delta
     */
    fun onScroll(deltaX: Float, deltaY: Float)
}
