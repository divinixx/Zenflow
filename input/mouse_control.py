"""
Mouse Control Module for Zenflow Server
Handles mouse movement, clicks, and scroll commands from Android app
"""
import pyautogui
import logging

logger = logging.getLogger(__name__)

# Configure PyAutoGUI
pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0.01  # Small pause between commands

def handle_mouse_command(data):
    """
    Handle mouse commands from Android app
    
    Expected data format:
    {
        "type": "mouse",
        "action": "move|click|scroll|drag",
        "data": {
            "x": 100,
            "y": 200,
            "button": "left|right|middle",
            "scroll_direction": "up|down",
            "scroll_amount": 3
        }
    }
    """
    try:
        action = data.get("action")
        mouse_data = data.get("data", {})
        
        if action == "move":
            x = mouse_data.get("x", 0)
            y = mouse_data.get("y", 0)
            pyautogui.moveTo(x, y, duration=0.1)
            return {"status": "success", "message": f"Mouse moved to ({x}, {y})"}
            
        elif action == "click":
            x = mouse_data.get("x", 0)
            y = mouse_data.get("y", 0)
            button = mouse_data.get("button", "left")
            
            if button in ["left", "right", "middle"]:
                pyautogui.click(x, y, button=button)
                return {"status": "success", "message": f"{button} click at ({x}, {y})"}
            else:
                return {"status": "error", "message": "Invalid button type"}
                
        elif action == "scroll":
            x = mouse_data.get("x", 0)
            y = mouse_data.get("y", 0)
            scroll_direction = mouse_data.get("scroll_direction", "up")
            scroll_amount = mouse_data.get("scroll_amount", 3)
            
            # Convert direction to scroll value
            scroll_value = scroll_amount if scroll_direction == "up" else -scroll_amount
            
            pyautogui.scroll(scroll_value, x=x, y=y)
            return {"status": "success", "message": f"Scrolled {scroll_direction} by {scroll_amount}"}
            
        elif action == "drag":
            start_x = mouse_data.get("start_x", 0)
            start_y = mouse_data.get("start_y", 0)
            end_x = mouse_data.get("end_x", 0)
            end_y = mouse_data.get("end_y", 0)
            
            pyautogui.drag(end_x - start_x, end_y - start_y, 
                          duration=0.3, button='left')
            return {"status": "success", "message": f"Dragged from ({start_x}, {start_y}) to ({end_x}, {end_y})"}
            
        else:
            return {"status": "error", "message": f"Unknown mouse action: {action}"}
            
    except Exception as e:
        logger.error(f"Mouse command error: {e}")
        return {"status": "error", "message": str(e)}
