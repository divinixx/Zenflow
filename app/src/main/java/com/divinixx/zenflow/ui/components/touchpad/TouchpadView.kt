package com.divinixx.zenflow.ui.components.touchpad

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.divinixx.zenflow.R
import kotlin.math.*
import androidx.core.graphics.toColorInt

/**
 * Custom TouchpadView for mouse control
 * Handles touch events and converts them to mouse movements, clicks, and scrolls
 */
class TouchpadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX_FPS = 120  // Increased for smoother movement
        private const val MIN_FRAME_TIME = 1000L / MAX_FPS
        private const val MIN_MOUSE_FRAME_TIME = 8L  // ~125fps for mouse movement
        private const val DOUBLE_TAP_TIMEOUT = 300L
        private const val LONG_PRESS_TIMEOUT = 500L
        private const val SCROLL_THRESHOLD = 10f
        private const val MOVEMENT_THRESHOLD = 1f  // Reduced for more responsiveness
        private const val MAX_VELOCITY = 50
        private const val ACCELERATION_FACTOR = 2.0f  // Increased for better acceleration
        private const val SMOOTHING_FACTOR = 0.8f  // For movement smoothing
    }

    // Configuration attributes
    private var mouseSensitivity: Float = 1.0f
    private var scrollSensitivity: Float = 1.0f
    private var enableGestures: Boolean = true
    private var leftHandedMode: Boolean = false
    private var enableRippleEffect: Boolean = true
    private var showCursorIndicator: Boolean = false
    
    // Colors
    private var touchpadBackgroundColor: Int = Color.TRANSPARENT
    private var touchHighlightColor: Int = "#33FFFFFF".toColorInt()
    private var rippleColor: Int = "#44FFFFFF".toColorInt()

    // Touch tracking
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var lastEventTime: Long = 0L
    private var lastMouseEventTime: Long = 0L
    private var isFirstTouch: Boolean = true
    private var touchCount: Int = 0
    private var isScrolling: Boolean = false
    private var lastTapTime: Long = 0L
    private var tapCount: Int = 0

    // Movement smoothing
    private var smoothedDeltaX: Float = 0f
    private var smoothedDeltaY: Float = 0f
    private var lastVelocityX: Float = 0f
    private var lastVelocityY: Float = 0f

    // Multi-touch tracking
    private val pointerPositions = mutableMapOf<Int, PointF>()
    private val initialPointerPositions = mutableMapOf<Int, PointF>()
    private var initialDistance: Float = 0f
    private var lastScrollDistance: Float = 0f

    // Visual feedback
    private val touchCircles = mutableListOf<TouchCircle>()
    private val rippleAnimations = mutableListOf<RippleAnimation>()
    private var cursorX: Float = 0f
    private var cursorY: Float = 0f

    // Paint objects
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val touchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Gesture detection
    private val gestureDetector: GestureDetector
    private var longPressRunnable: Runnable? = null

    // Listener
    private var touchpadListener: TouchpadListener? = null

    // Performance optimization
    private var lastFrameTime: Long = 0L

    init {
        // Load custom attributes
        loadAttributes(attrs)
        
        // Initialize paints
        setupPaints()
        
        // Setup gesture detector
        gestureDetector = GestureDetector(context, TouchpadGestureListener())
        
        // Enable touch events
        isClickable = true
        isFocusable = true
    }

    private fun loadAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.TouchpadView)
            try {
                mouseSensitivity = typedArray.getFloat(R.styleable.TouchpadView_mouseSensitivity, 1.0f)
                scrollSensitivity = typedArray.getFloat(R.styleable.TouchpadView_scrollSensitivity, 1.0f)
                enableGestures = typedArray.getBoolean(R.styleable.TouchpadView_enableGestures, true)
                leftHandedMode = typedArray.getBoolean(R.styleable.TouchpadView_leftHandedMode, false)
                enableRippleEffect = typedArray.getBoolean(R.styleable.TouchpadView_enableRippleEffect, true)
                showCursorIndicator = typedArray.getBoolean(R.styleable.TouchpadView_showCursorIndicator, false)
                
                touchpadBackgroundColor = typedArray.getColor(
                    R.styleable.TouchpadView_touchpadBackground, 
                    Color.TRANSPARENT
                )
                touchHighlightColor = typedArray.getColor(
                    R.styleable.TouchpadView_touchHighlightColor,
                    "#33FFFFFF".toColorInt()
                )
                rippleColor = typedArray.getColor(
                    R.styleable.TouchpadView_rippleColor,
                    "#44FFFFFF".toColorInt()
                )
            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun setupPaints() {
        backgroundPaint.apply {
            color = touchpadBackgroundColor
            style = Paint.Style.FILL
        }

        touchPaint.apply {
            color = touchHighlightColor
            style = Paint.Style.FILL
        }

        ripplePaint.apply {
            color = rippleColor
            style = Paint.Style.FILL
        }

        cursorPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            strokeWidth = 4f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Different throttling for mouse movement vs visual updates
        val isMouseMovement = event.actionMasked == MotionEvent.ACTION_MOVE && touchCount == 1
        val minFrameTime = if (isMouseMovement) MIN_MOUSE_FRAME_TIME else MIN_FRAME_TIME
        
        if (isMouseMovement && currentTime - lastMouseEventTime < minFrameTime) {
            // Still process movement but skip visual updates
            if (touchCount == 1) {
                handleSingleFingerMove(event, currentTime, skipVisualUpdate = true)
            }
            return true
        }
        
        if (currentTime - lastFrameTime < minFrameTime && !isMouseMovement) {
            return true
        }
        
        lastFrameTime = currentTime
        if (isMouseMovement) {
            lastMouseEventTime = currentTime
        }

        // Let gesture detector handle the event first
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(event)
            MotionEvent.ACTION_POINTER_DOWN -> handlePointerDown(event)
            MotionEvent.ACTION_MOVE -> handleTouchMove(event)
            MotionEvent.ACTION_POINTER_UP -> handlePointerUp(event)
            MotionEvent.ACTION_UP -> handleTouchUp(event)
            MotionEvent.ACTION_CANCEL -> handleTouchCancel(event)
        }

        // Update visual feedback (skip if optimizing mouse movement)
        if (!isMouseMovement || currentTime - lastFrameTime >= MIN_FRAME_TIME) {
            updateTouchCircles(event)
            invalidate()
        }

        return true
    }

    private fun handleTouchDown(event: MotionEvent) {
        touchCount = 1
        isFirstTouch = true
        isScrolling = false
        
        lastTouchX = event.x
        lastTouchY = event.y
        lastEventTime = System.currentTimeMillis()
        
        // Store pointer position
        pointerPositions[event.getPointerId(0)] = PointF(event.x, event.y)
        initialPointerPositions[event.getPointerId(0)] = PointF(event.x, event.y)
        
        // Update cursor position if enabled
        if (showCursorIndicator) {
            cursorX = event.x
            cursorY = event.y
        }
        
        // Schedule long press detection
        scheduleLongPress(event.x, event.y)
    }

    private fun handlePointerDown(event: MotionEvent) {
        touchCount = event.pointerCount
        
        // Cancel long press when second finger touches
        cancelLongPressAction()
        
        // Store all pointer positions
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            pointerPositions[pointerId] = PointF(x, y)
            if (!initialPointerPositions.containsKey(pointerId)) {
                initialPointerPositions[pointerId] = PointF(x, y)
            }
        }
        
        // Calculate initial distance for two-finger gestures
        if (touchCount == 2) {
            initialDistance = calculateDistance(event, 0, 1)
            lastScrollDistance = initialDistance
        }
    }

    private fun handleTouchMove(event: MotionEvent) {
        val currentTime = System.currentTimeMillis()
        
        when (touchCount) {
            1 -> handleSingleFingerMove(event, currentTime, skipVisualUpdate = false)
            2 -> handleTwoFingerMove(event, currentTime)
        }
        
        lastEventTime = currentTime
    }

    private fun handleSingleFingerMove(event: MotionEvent, currentTime: Long, skipVisualUpdate: Boolean = false) {
        val currentX = event.x
        val currentY = event.y
        
        if (isFirstTouch) {
            // Check if movement threshold is exceeded
            val distance = sqrt((currentX - lastTouchX).pow(2) + (currentY - lastTouchY).pow(2))
            if (distance < MOVEMENT_THRESHOLD) {
                return
            }
            isFirstTouch = false
            cancelLongPressAction()
        }
        
        // Calculate raw mouse movement delta
        val rawDeltaX = currentX - lastTouchX
        val rawDeltaY = currentY - lastTouchY
        val timeDelta = currentTime - lastEventTime
        
        // Apply smoothing to reduce jitter
        val smoothedDelta = applySmoothingAndAcceleration(rawDeltaX, rawDeltaY, timeDelta)
        
        // Send mouse movement immediately for low latency
        touchpadListener?.onMove(
            smoothedDelta.x * mouseSensitivity,
            smoothedDelta.y * mouseSensitivity
        )
        
        // Update cursor position for visual feedback
        if (showCursorIndicator && !skipVisualUpdate) {
            cursorX = currentX
            cursorY = currentY
        }
        
        lastTouchX = currentX
        lastTouchY = currentY
    }

    private fun handleTwoFingerMove(event: MotionEvent, currentTime: Long) {
        if (event.pointerCount < 2) return
        
        // Calculate current distance between fingers
        val currentDistance = calculateDistance(event, 0, 1)
        
        // Calculate center point movement for scrolling
        val centerX = (event.getX(0) + event.getX(1)) / 2
        val centerY = (event.getY(0) + event.getY(1)) / 2
        
        // Get previous center point
        val prevCenterX = ((pointerPositions[event.getPointerId(0)]?.x ?: centerX) + 
                          (pointerPositions[event.getPointerId(1)]?.x ?: centerX)) / 2
        val prevCenterY = ((pointerPositions[event.getPointerId(0)]?.y ?: centerY) + 
                          (pointerPositions[event.getPointerId(1)]?.y ?: centerY)) / 2
        
        // Calculate scroll delta
        val scrollDeltaX = centerX - prevCenterX
        val scrollDeltaY = centerY - prevCenterY
        
        // Check if scrolling threshold is exceeded
        if (abs(scrollDeltaX) > SCROLL_THRESHOLD || abs(scrollDeltaY) > SCROLL_THRESHOLD) {
            isScrolling = true
            
            // Send scroll event
            touchpadListener?.onScroll(
                scrollDeltaX * scrollSensitivity,
                scrollDeltaY * scrollSensitivity
            )
        }
        
        // Update pointer positions
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            pointerPositions[pointerId] = PointF(event.getX(i), event.getY(i))
        }
    }

    private fun handlePointerUp(event: MotionEvent) {
        val pointerId = event.getPointerId(event.actionIndex)
        pointerPositions.remove(pointerId)
        touchCount = event.pointerCount - 1
        
        // Handle two-finger tap for right click
        if (touchCount == 0 && !isScrolling && enableGestures) {
            // Check if this was a tap (not a scroll)
            val wasQuickTap = System.currentTimeMillis() - lastEventTime < 200
            if (wasQuickTap) {
                performRightClick()
            }
        }
    }

    private fun handleTouchUp(event: MotionEvent) {
        val currentTime = System.currentTimeMillis()
        
        // Cancel long press
        cancelLongPressAction()
        
        // Handle single finger tap
        if (touchCount == 1 && !isScrolling && isFirstTouch && enableGestures) {
            handleTap(currentTime)
        }
        
        // Clean up
        pointerPositions.clear()
        initialPointerPositions.clear()
        touchCount = 0
        isScrolling = false
        isFirstTouch = true
        
        // Add ripple effect
        if (enableRippleEffect) {
            addRippleEffect(event.x, event.y)
        }
    }

    private fun handleTouchCancel(event: MotionEvent) {
        cancelLongPressAction()
        pointerPositions.clear()
        initialPointerPositions.clear()
        touchCount = 0
        isScrolling = false
        isFirstTouch = true
    }

    private fun handleTap(currentTime: Long) {
        val timeSinceLastTap = currentTime - lastTapTime
        
        if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT) {
            tapCount++
        } else {
            tapCount = 1
        }
        
        lastTapTime = currentTime
        
        // Handle tap based on count
        when (tapCount) {
            1 -> {
                // Schedule single click (wait for potential double tap)
                postDelayed({
                    if (tapCount == 1) {
                        performLeftClick()
                    }
                }, DOUBLE_TAP_TIMEOUT)
            }
            2 -> {
                // Double tap
                performDoubleClick()
                tapCount = 0
            }
        }
    }

    private fun performLeftClick() {
        touchpadListener?.onLeftClick(cursorX, cursorY)
    }

    private fun performRightClick() {
        touchpadListener?.onRightClick(cursorX, cursorY)
    }

    private fun performDoubleClick() {
        touchpadListener?.onDoubleClick(cursorX, cursorY)
    }

    private fun scheduleLongPress(x: Float, y: Float) {
        cancelLongPressAction()
        longPressRunnable = Runnable {
            // Long press could trigger right click or context menu
            performRightClick()
        }
        postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT)
    }

    private fun cancelLongPressAction() {
        longPressRunnable?.let {
            removeCallbacks(it)
            longPressRunnable = null
        }
    }

    private fun applySmoothingAndAcceleration(deltaX: Float, deltaY: Float, timeDelta: Long): PointF {
        // Calculate velocity
        val timeSeconds = (timeDelta.coerceAtLeast(1L)) / 1000f
        val velocityX = deltaX / timeSeconds
        val velocityY = deltaY / timeSeconds
        
        // Apply exponential smoothing to reduce jitter
        smoothedDeltaX = SMOOTHING_FACTOR * smoothedDeltaX + (1 - SMOOTHING_FACTOR) * deltaX
        smoothedDeltaY = SMOOTHING_FACTOR * smoothedDeltaY + (1 - SMOOTHING_FACTOR) * deltaY
        
        // Calculate speed for acceleration
        val speed = sqrt(velocityX.pow(2) + velocityY.pow(2))
        
        // Apply dynamic acceleration based on speed
        val acceleration = when {
            speed < 10f -> 0.5f  // Slow movement - precision mode
            speed < 50f -> 1.0f  // Normal movement
            speed < 200f -> 2.0f // Fast movement - acceleration
            else -> 3.0f         // Very fast movement - maximum acceleration
        }
        
        // Apply acceleration to smoothed deltas
        return PointF(
            smoothedDeltaX * acceleration,
            smoothedDeltaY * acceleration
        )
    }

    // Keep the old function for compatibility
    private fun applyAcceleration(deltaX: Float, deltaY: Float, timeDelta: Long): PointF {
        val velocity = sqrt(deltaX.pow(2) + deltaY.pow(2)) / (timeDelta + 1)
        val acceleration = min(1f + velocity * ACCELERATION_FACTOR, MAX_VELOCITY.toFloat())
        
        return PointF(
            deltaX * acceleration,
            deltaY * acceleration
        )
    }

    private fun calculateDistance(event: MotionEvent, pointer1: Int, pointer2: Int): Float {
        val x1 = event.getX(pointer1)
        val y1 = event.getY(pointer1)
        val x2 = event.getX(pointer2)
        val y2 = event.getY(pointer2)
        
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    private fun updateTouchCircles(event: MotionEvent) {
        touchCircles.clear()
        
        for (i in 0 until event.pointerCount) {
            touchCircles.add(
                TouchCircle(
                    event.getX(i),
                    event.getY(i),
                    30f + (i * 10f) // Different sizes for multiple touches
                )
            )
        }
    }

    private fun addRippleEffect(x: Float, y: Float) {
        if (!enableRippleEffect) return
        
        val ripple = RippleAnimation(x, y)
        rippleAnimations.add(ripple)
        
        // Animate ripple
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                ripple.progress = animation.animatedValue as Float
                invalidate()
            }
        }
        
        animator.start()
        
        // Remove ripple after animation
        postDelayed({
            rippleAnimations.remove(ripple)
        }, 300)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background
        if (touchpadBackgroundColor != Color.TRANSPARENT) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        }
        
        // Draw touch circles
        for (circle in touchCircles) {
            canvas.drawCircle(circle.x, circle.y, circle.radius, touchPaint)
        }
        
        // Draw ripple effects
        for (ripple in rippleAnimations) {
            ripplePaint.alpha = ((1f - ripple.progress) * 255).toInt()
            canvas.drawCircle(
                ripple.x,
                ripple.y,
                ripple.progress * 100f,
                ripplePaint
            )
        }
        
        // Draw cursor indicator
        if (showCursorIndicator && cursorX > 0 && cursorY > 0) {
            canvas.drawCircle(cursorX, cursorY, 8f, cursorPaint)
        }
    }

    // Public API methods
    fun setTouchpadListener(listener: TouchpadListener?) {
        this.touchpadListener = listener
    }

    fun setSensitivity(sensitivity: Float) {
        this.mouseSensitivity = sensitivity.coerceIn(0.1f, 5.0f)
    }

    fun setMouseSensitivity(sensitivity: Float) {
        this.mouseSensitivity = sensitivity.coerceIn(0.1f, 5.0f)
    }

    fun setScrollSensitivity(sensitivity: Float) {
        this.scrollSensitivity = sensitivity.coerceIn(0.1f, 3.0f)
    }

    fun setLeftHandedMode(enabled: Boolean) {
        this.leftHandedMode = enabled
    }

    fun setGesturesEnabled(enabled: Boolean) {
        this.enableGestures = enabled
    }

    fun setScrollingEnabled(enabled: Boolean) {
        this.enableGestures = enabled
    }

    fun setRightClickEnabled(enabled: Boolean) {
        // This affects gesture recognition for right clicks
        this.enableGestures = enabled
    }

    fun setDoubleClickEnabled(enabled: Boolean) {
        // This affects gesture recognition for double clicks  
        this.enableGestures = enabled
    }

    // Data classes for visual feedback
    private data class TouchCircle(val x: Float, val y: Float, val radius: Float)
    private data class RippleAnimation(val x: Float, val y: Float, var progress: Float = 0f)

    // Gesture detector implementation
    private inner class TouchpadGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Handled in touch events
            return false
        }
        
        override fun onLongPress(e: MotionEvent) {
            // Long press triggers right click
            performRightClick()
        }
    }
}
