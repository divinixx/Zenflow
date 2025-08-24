# Connection Timeout Troubleshooting Guide

## Common Causes and Solutions

### 1. 🔥 **Firewall Issues** (Most Common)
**Problem**: Windows/PC firewall is blocking incoming connections on port 8080

**Solutions**:
```bash
# Windows Firewall - Allow port 8080
netsh advfirewall firewall add rule name="WebSocket Server" dir=in action=allow protocol=TCP localport=8080

# Or disable Windows Firewall temporarily (for testing)
# Control Panel > System and Security > Windows Defender Firewall > Turn Windows Defender Firewall on or off
```

### 2. 📡 **No Server Running**
**Problem**: No WebSocket server is running on the target machine

**Quick Test**:
```bash
# Test if port 8080 is open (from Android device)
# Install a network scanner app or use computer to test:
telnet 192.168.1.100 8080
```

**Solution**: Start a test server using one of these methods:

#### Option A: Simple Python Server
```python
# Save as test_server.py
import asyncio
import websockets
import json
from datetime import datetime

async def echo(websocket, path):
    print(f"Client connected from {websocket.remote_address}")
    try:
        async for message in websocket:
            print(f"Received: {message}")
            response = {
                "type": "pong",
                "message": "Server received your message",
                "timestamp": datetime.now().isoformat()
            }
            await websocket.send(json.dumps(response))
    except websockets.exceptions.ConnectionClosed:
        print("Client disconnected")

print("Starting WebSocket server on port 8080...")
start_server = websockets.serve(echo, "0.0.0.0", 8080)
asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()
```

Run with: `python test_server.py`

#### Option B: Node.js Server
```javascript
// Save as server.js
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8080 });

console.log('WebSocket server started on port 8080');

wss.on('connection', function connection(ws, req) {
    console.log('Client connected from:', req.socket.remoteAddress);
    
    ws.on('message', function incoming(data) {
        console.log('Received:', data.toString());
        const response = {
            type: 'pong',
            message: 'Server received your message',
            timestamp: new Date().toISOString()
        };
        ws.send(JSON.stringify(response));
    });
    
    ws.on('close', function close() {
        console.log('Client disconnected');
    });
});
```

Run with: `node server.js` (after `npm install ws`)

### 3. 🌐 **Network Issues**

#### Wrong IP Address
**Problem**: Using incorrect IP address

**Solution**: Find correct IP address:
```bash
# Windows
ipconfig

# Look for "IPv4 Address" under your active network adapter
# Example: 192.168.1.100
```

#### Different Networks
**Problem**: Android device and PC are on different networks

**Solution**: 
- Connect both devices to the same WiFi network
- Or use mobile hotspot to connect PC to phone's network

### 4. 📱 **Android Network Permissions**

**Problem**: App doesn't have network permissions

**Solution**: Check AndroidManifest.xml has:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 5. 🔒 **Port Issues**

#### Port Already in Use
**Check if port 8080 is in use**:
```bash
# Windows
netstat -an | findstr :8080

# If port is in use, try different port or kill the process
```

#### Router/Network Configuration
- Some routers block certain ports
- Try different ports: 8081, 9000, 3000

### 6. 🏠 **Local Network Discovery**

#### Find Available Devices
```bash
# Scan network for devices
nmap -sn 192.168.1.0/24

# Or use mobile app like "Network Scanner"
```

## Debugging Steps

### Step 1: Verify Basic Connectivity
1. **Ping Test**: Can Android device ping the PC?
   ```bash
   ping 192.168.1.100
   ```

2. **Port Test**: Is port 8080 accessible?
   - Use network scanner app on Android
   - Try telnet from another computer

### Step 2: Test Simple Server
1. **Start minimal server** (use Python/Node.js examples above)
2. **Check server logs** for connection attempts
3. **Try connecting from browser**: `ws://192.168.1.100:8080`

### Step 3: Check App Logs
1. **Monitor Android logs** for specific error messages
2. **Look for network state** in the connection log
3. **Check IP format validation** messages

### Step 4: Network Diagnostics
1. **Use built-in diagnostics** in the app
2. **Check WiFi connection** quality
3. **Try mobile data** vs WiFi

## App Error Messages and Meanings

| Error Message | Meaning | Solution |
|---------------|---------|----------|
| "Connection timeout - Could not connect to server after 15 seconds" | No response from server | Check if server is running, firewall settings |
| "Connection refused - Server not running on port 8080" | Server actively refused connection | Start WebSocket server on port 8080 |
| "Network unreachable - Check IP address and network" | Network routing issue | Verify IP address, check same network |
| "Host is down - Server machine may be offline" | Target machine not responding | Check if PC is on and connected to network |
| "Socket timeout - Server took too long to respond" | Slow network or server | Try again, check network quality |
| "Unknown host - Invalid IP address" | DNS resolution failed | Use IP address instead of hostname |

## Quick Fix Checklist

- [ ] ✅ **Server running**: Start test server on PC
- [ ] ✅ **Correct IP**: Use `ipconfig` to get PC's IP
- [ ] ✅ **Same network**: Both devices on same WiFi
- [ ] ✅ **Firewall off**: Temporarily disable Windows firewall
- [ ] ✅ **Port 8080**: Not blocked by router/firewall
- [ ] ✅ **App permissions**: Internet permission granted
- [ ] ✅ **Network quality**: Strong WiFi signal

## Advanced Debugging

### Enable Network Debugging in Android
```kotlin
// Add to MainActivity for detailed network logs
System.setProperty("java.net.useSystemProxies", "true")
```

### Use Wireshark for Network Analysis
1. Install Wireshark on PC
2. Capture traffic on network interface
3. Filter by: `tcp.port == 8080`
4. Look for connection attempts from Android device

### Test with External Tools
```bash
# Test WebSocket connection from command line
wscat -c ws://192.168.1.100:8080

# Or use online WebSocket tester
# websocketking.com
```

## Success Indicators

✅ **Connection successful** when you see:
- App status: "Connected" (green)
- Server logs: "Client connected"
- Ping/Pong messages in app log
- Latency measurements displayed

If you're still experiencing issues, try the simplest test first: Start the Python test server and connect from the same machine using a browser WebSocket client.
