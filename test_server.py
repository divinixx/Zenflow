#!/usr/bin/env python3
"""
Simple WebSocket Echo Server for Testing Zenflow Remote
This server accepts connections and responds to ping messages properly.
"""

import asyncio
import websockets
import json
import logging
from datetime import datetime

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class ZenflowTestServer:
    def __init__(self, host="0.0.0.0", port=8080):
        self.host = host
        self.port = port
        self.connected_clients = set()
    
    async def handle_client(self, websocket, path):
        """Handle incoming WebSocket connections"""
        client_address = websocket.remote_address
        logger.info(f"Client connected from {client_address}")
        self.connected_clients.add(websocket)
        
        try:
            async for message in websocket:
                await self.process_message(websocket, message)
                
        except websockets.exceptions.ConnectionClosed:
            logger.info(f"Client {client_address} disconnected")
        except Exception as e:
            logger.error(f"Error handling client {client_address}: {e}")
        finally:
            self.connected_clients.discard(websocket)
    
    async def process_message(self, websocket, message):
        """Process incoming messages and send appropriate responses"""
        try:
            data = json.loads(message)
            message_type = data.get('type', 'unknown')
            action = data.get('action', 'unknown')
            
            logger.info(f"Received: {message_type}/{action}")
            logger.debug(f"Full message: {data}")
            
            # Handle different message types
            if message_type == "test" and action == "ping":
                await self.handle_test_ping(websocket, data)
            elif message_type == "ping" and action == "keepalive":
                await self.handle_keepalive_ping(websocket, data)
            elif message_type == "mouse":
                await self.handle_mouse_input(websocket, data)
            elif message_type == "keyboard":
                await self.handle_keyboard_input(websocket, data)
            else:
                await self.handle_echo(websocket, data)
                
        except json.JSONDecodeError as e:
            logger.error(f"Invalid JSON received: {e}")
            await self.send_error(websocket, "Invalid JSON format")
        except Exception as e:
            logger.error(f"Error processing message: {e}")
            await self.send_error(websocket, f"Server error: {str(e)}")
    
    async def handle_mouse_input(self, websocket, data):
        """Handle mouse input from touchpad"""
        action = data.get('action', 'unknown')
        mouse_data = data.get('data', {})
        
        logger.info(f"Mouse {action}: {mouse_data}")
        
        response = {
            "type": "response",
            "action": "mouse_ack",
            "status": "success",
            "message": f"Mouse {action} processed",
            "timestamp": datetime.now().isoformat()
        }
        
        await websocket.send(json.dumps(response))
    
    async def handle_keyboard_input(self, websocket, data):
        """Handle keyboard input from virtual keyboard"""
        action = data.get('action', 'unknown')
        
        if action == "press" or action == "release":
            key = data.get('key', 'unknown')
            logger.info(f"Keyboard {action}: {key}")
            message = f"Key {action}: {key}"
        elif action == "type":
            text = data.get('text', '')
            logger.info(f"Text input: {text}")
            message = f"Text typed: {text}"
        elif action == "combo":
            combination = data.get('combination', '')
            logger.info(f"Key combination: {combination}")
            message = f"Key combo: {combination}"
        else:
            logger.warning(f"Unknown keyboard action: {action}")
            message = f"Unknown keyboard action: {action}"
        
        response = {
            "type": "response",
            "action": "keyboard_ack",
            "status": "success",
            "message": message,
            "timestamp": datetime.now().isoformat()
        }
        
        await websocket.send(json.dumps(response))
    
    async def handle_test_ping(self, websocket, data):
        """Handle test ping messages from Android app"""
        response = {
            "type": "pong",
            "action": "response", 
            "status": "success",
            "message": "pong",
            "data": {
                "server": "python-test-server",
                "version": "1.0",
                "echo_data": data.get("data", {}),
                "server_time": datetime.now().isoformat()
            },
            "timestamp": datetime.now().isoformat()
        }
        
        await websocket.send(json.dumps(response))
        logger.info("Sent pong response")
    
    async def handle_keepalive_ping(self, websocket, data):
        """Handle keep-alive ping messages"""
        response = {
            "type": "pong",
            "action": "keepalive",
            "timestamp": datetime.now().isoformat()
        }
        
        await websocket.send(json.dumps(response))
        logger.info("Sent keepalive pong")
    
    async def handle_echo(self, websocket, data):
        """Echo back unknown message types"""
        response = {
            "type": "response",
            "action": "echo",
            "status": "success",
            "data": data,
            "timestamp": datetime.now().isoformat()
        }
        
        await websocket.send(json.dumps(response))
        logger.info(f"Echoed message: {data.get('type', 'unknown')}")
    
    async def send_error(self, websocket, error_message):
        """Send error response to client"""
        error_response = {
            "type": "error",
            "message": error_message,
            "timestamp": datetime.now().isoformat()
        }
        
        try:
            await websocket.send(json.dumps(error_response))
            logger.warning(f"Sent error: {error_message}")
        except Exception as e:
            logger.error(f"Failed to send error response: {e}")
    
    async def start_server(self):
        """Start the WebSocket server"""
        logger.info(f"Starting WebSocket server on {self.host}:{self.port}")
        
        try:
            server = await websockets.serve(
                self.handle_client,
                self.host,
                self.port,
                ping_interval=30,  # Send ping every 30 seconds
                ping_timeout=10,   # Wait 10 seconds for pong
                close_timeout=10   # Wait 10 seconds for close
            )
            
            logger.info(f"✅ Server running on ws://{self.host}:{self.port}")
            logger.info("Ready to accept connections from Zenflow Remote app")
            logger.info("Press Ctrl+C to stop the server")
            
            # Keep server running
            await server.wait_closed()
            
        except Exception as e:
            logger.error(f"Failed to start server: {e}")
            raise

def main():
    """Main function to run the server"""
    import sys
    import signal
    
    # Default settings
    host = "0.0.0.0"  # Accept connections from any IP
    port = 8080
    
    # Parse command line arguments
    if len(sys.argv) >= 2:
        try:
            port = int(sys.argv[1])
        except ValueError:
            print(f"Invalid port number: {sys.argv[1]}")
            sys.exit(1)
    
    if len(sys.argv) >= 3:
        host = sys.argv[2]
    
    # Create and start server
    server = ZenflowTestServer(host, port)
    
    # Handle Ctrl+C gracefully
    def signal_handler(sig, frame):
        logger.info("Shutting down server...")
        sys.exit(0)
    
    signal.signal(signal.SIGINT, signal_handler)
    
    # Run server
    try:
        asyncio.run(server.start_server())
    except KeyboardInterrupt:
        logger.info("Server stopped by user")
    except Exception as e:
        logger.error(f"Server error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
