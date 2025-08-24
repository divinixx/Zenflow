# Test WebSocket Server

A simple WebSocket server for testing Zenflow Remote app functionality.

## Server Requirements

### Basic WebSocket Server (Port 8080)
```javascript
// Node.js WebSocket Server Example
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8080 });

wss.on('connection', function connection(ws) {
    console.log('Client connected');
    
    ws.on('message', function incoming(data) {
        try {
            const message = JSON.parse(data);
            console.log('Received:', message);
            
            // Handle different message types
            switch(message.type) {
                case 'test':
                    if (message.action === 'ping') {
                        // Respond to ping with pong
                        const pongResponse = {
                            type: 'pong',
                            action: 'response',
                            status: 'success',
                            message: 'pong',
                            data: {
                                server: 'nodejs',
                                version: '1.0',
                                client_data: message.data
                            },
                            timestamp: new Date().toISOString()
                        };
                        ws.send(JSON.stringify(pongResponse));
                    }
                    break;
                    
                case 'ping':
                    // Keep-alive response
                    const keepAliveResponse = {
                        type: 'pong',
                        action: 'keepalive',
                        timestamp: new Date().toISOString()
                    };
                    ws.send(JSON.stringify(keepAliveResponse));
                    break;
                    
                default:
                    // Echo back unknown messages
                    const echoResponse = {
                        type: 'response',
                        action: 'echo',
                        data: message,
                        timestamp: new Date().toISOString()
                    };
                    ws.send(JSON.stringify(echoResponse));
            }
        } catch (error) {
            // Send error response for invalid JSON
            const errorResponse = {
                type: 'error',
                message: 'Invalid JSON format',
                timestamp: new Date().toISOString()
            };
            ws.send(JSON.stringify(errorResponse));
        }
    });
    
    ws.on('close', function close() {
        console.log('Client disconnected');
    });
    
    ws.on('error', function error(err) {
        console.error('WebSocket error:', err);
    });
});

console.log('WebSocket server running on port 8080');
```

## Python Alternative
```python
import asyncio
import websockets
import json
from datetime import datetime

async def handle_client(websocket, path):
    print("Client connected")
    try:
        async for message in websocket:
            try:
                data = json.loads(message)
                print(f"Received: {data}")
                
                if data.get('type') == 'test' and data.get('action') == 'ping':
                    response = {
                        'type': 'pong',
                        'action': 'response',
                        'status': 'success',
                        'message': 'pong',
                        'data': {
                            'server': 'python',
                            'version': '1.0',
                            'client_data': data.get('data', {})
                        },
                        'timestamp': datetime.now().isoformat()
                    }
                    await websocket.send(json.dumps(response))
                    
                elif data.get('type') == 'ping':
                    response = {
                        'type': 'pong',
                        'action': 'keepalive',
                        'timestamp': datetime.now().isoformat()
                    }
                    await websocket.send(json.dumps(response))
                    
            except json.JSONDecodeError:
                error_response = {
                    'type': 'error',
                    'message': 'Invalid JSON format',
                    'timestamp': datetime.now().isoformat()
                }
                await websocket.send(json.dumps(error_response))
                
    except websockets.exceptions.ConnectionClosed:
        print("Client disconnected")

start_server = websockets.serve(handle_client, "0.0.0.0", 8080)

print("WebSocket server running on port 8080")
asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()
```

## Expected Message Flow

### 1. Client Connects
- Android app connects to `ws://192.168.1.100:8080`
- Server logs: "Client connected"

### 2. Ping Test Message
**Client sends:**
```json
{
    "type": "test",
    "action": "ping",
    "data": {
        "client": "android",
        "version": "1.0",
        "timestamp": 1692745620000
    }
}
```

**Server responds:**
```json
{
    "type": "pong", 
    "action": "response",
    "status": "success",
    "message": "pong",
    "data": {
        "server": "nodejs",
        "version": "1.0",
        "client_data": {
            "client": "android",
            "version": "1.0",
            "timestamp": 1692745620000
        }
    },
    "timestamp": "2025-08-23T00:27:00.123Z"
}
```

### 3. Keep-Alive Messages
**Client sends (every 30s):**
```json
{
    "type": "ping",
    "action": "keepalive",
    "data": {},
    "timestamp": "2025-08-23T00:27:30.000Z"
}
```

**Server responds:**
```json
{
    "type": "pong",
    "action": "keepalive", 
    "timestamp": "2025-08-23T00:27:30.123Z"
}
```

## Setup Instructions

### Node.js Server
1. Install Node.js and npm
2. Create package.json: `npm init -y`
3. Install WebSocket: `npm install ws`
4. Save server code as `server.js`
5. Run: `node server.js`

### Python Server  
1. Install Python 3.7+
2. Install websockets: `pip install websockets`
3. Save server code as `server.py`
4. Run: `python server.py`

## Testing with Android App

1. Start the WebSocket server on your PC
2. Find your PC's IP address: `ipconfig` (Windows) or `ifconfig` (Linux/Mac)
3. Open Zenflow Remote app
4. Enter PC IP address (e.g., 192.168.1.100)
5. Tap "Connect" button
6. Observe connection logs
7. Tap "Send Test Message" to test ping/pong
8. Check server console for received messages

## Expected App Behavior

- **Connection**: App shows "Connected" with green status
- **Latency**: App displays round-trip time in milliseconds
- **Messages**: Received messages appear in log with timestamps
- **Errors**: Connection failures show appropriate error messages
- **Auto-reconnect**: App attempts to reconnect on connection loss

## Troubleshooting

- **Connection timeout**: Check firewall settings
- **Invalid IP**: Verify IP address format and network connectivity  
- **Port blocked**: Ensure port 8080 is not blocked by firewall
- **Network issues**: Try connecting on same WiFi network
