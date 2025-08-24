#!/usr/bin/env python3
"""
Simple WebSocket test server for debugging Zenflow Android app communication
Listens on port 8080 and logs all incoming messages
"""

import asyncio
import websockets
import json
import logging
from datetime import datetime

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ZenflowTestServer:
    def __init__(self):
        self.connected_clients = set()
        
    async def handle_client(self, websocket, path):
        """Handle incoming WebSocket connections"""
        client_addr = f"{websocket.remote_address[0]}:{websocket.remote_address[1]}"
        logger.info(f"📱 New client connected: {client_addr}")
        self.connected_clients.add(websocket)
        
        try:
            async for message in websocket:
                await self.process_message(message, client_addr)
        except websockets.exceptions.ConnectionClosed:
            logger.info(f"📱 Client disconnected: {client_addr}")
        except Exception as e:
            logger.error(f"❌ Error handling client {client_addr}: {e}")
        finally:
            self.connected_clients.discard(websocket)
    
    async def process_message(self, message, client_addr):
        """Process incoming messages and log them"""
        try:
            data = json.loads(message)
            timestamp = datetime.now().strftime("%H:%M:%S.%f")[:-3]
            
            msg_type = data.get('type', 'unknown')
            action = data.get('action', 'unknown')
            
            if msg_type == 'keyboard':
                if action == 'combo':
                    combination = data.get('combination', 'unknown')
                    logger.info(f"🎹 [{timestamp}] {client_addr} -> COMBO: {combination}")
                    
                    # Special handling for media controls
                    if 'media' in combination:
                        logger.info(f"🎵 [{timestamp}] MEDIA CONTROL DETECTED: {combination}")
                        if combination == 'media_play_pause':
                            logger.info(f"⏯️ [{timestamp}] PLAY/PAUSE command received!")
                        elif combination == 'media_next':
                            logger.info(f"⏭️ [{timestamp}] NEXT track command received!")
                        elif combination == 'media_previous':
                            logger.info(f"⏮️ [{timestamp}] PREVIOUS track command received!")
                        elif combination == 'volume_mute':
                            logger.info(f"🔇 [{timestamp}] MUTE command received!")
                    
                elif action in ['press', 'release']:
                    key = data.get('key', 'unknown')
                    logger.info(f"⌨️ [{timestamp}] {client_addr} -> KEY {action.upper()}: {key}")
                    
                    if key == 'ENTER':
                        logger.info(f"↩️ [{timestamp}] ENTER KEY DETECTED: {action}")
                        
                elif action == 'type':
                    text = data.get('text', 'unknown')
                    logger.info(f"📝 [{timestamp}] {client_addr} -> TYPE: '{text}'")
                    
            elif msg_type == 'mouse':
                if action == 'move':
                    dx = data.get('dx', 0)
                    dy = data.get('dy', 0)
                    logger.info(f"🖱️ [{timestamp}] {client_addr} -> MOUSE MOVE: dx={dx}, dy={dy}")
                elif action in ['left_click', 'right_click', 'scroll']:
                    logger.info(f"🖱️ [{timestamp}] {client_addr} -> MOUSE: {action}")
                    
            else:
                logger.info(f"❓ [{timestamp}] {client_addr} -> UNKNOWN: {message}")
                
            # Echo back success response
            response = {"status": "received", "timestamp": timestamp}
            # Note: In a real server, you might want to send responses back
            
        except json.JSONDecodeError:
            logger.error(f"❌ Invalid JSON from {client_addr}: {message}")
        except Exception as e:
            logger.error(f"❌ Error processing message from {client_addr}: {e}")

    async def start_server(self, host='localhost', port=8080):
        """Start the WebSocket server"""
        logger.info(f"🚀 Starting Zenflow Test Server on {host}:{port}")
        logger.info(f"📡 Connect your Android app to: ws://{host}:{port}")
        logger.info(f"🔍 Monitoring for media control commands...")
        logger.info(f"━" * 60)
        
        start_server = websockets.serve(self.handle_client, host, port)
        await start_server
        
        # Keep the server running
        await asyncio.Future()  # Run forever

if __name__ == "__main__":
    server = ZenflowTestServer()
    try:
        asyncio.run(server.start_server())
    except KeyboardInterrupt:
        logger.info("\n🛑 Server stopped by user")
    except Exception as e:
        logger.error(f"❌ Server error: {e}")
