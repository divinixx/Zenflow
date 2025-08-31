"""
Network Service Discovery Module for Zenflow Server
Broadcasts mDNS/Bonjour service for automatic PC discovery by Android app
"""
import socket
import threading
import time
import logging
from zeroconf import ServiceInfo, Zeroconf
from zeroconf.const import InterfaceChoice

logger = logging.getLogger(__name__)

# Global variables for service management
zeroconf = None
service_info = None
discovery_thread = None
is_running = False

def get_local_ip():
    """Get the local IP address of this machine"""
    try:
        # Connect to a remote address to determine local IP
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))
            local_ip = s.getsockname()[0]
            return local_ip
    except Exception:
        # Fallback to localhost if network is unavailable
        return "127.0.0.1"

def start_discovery(port=8080):
    """
    Start broadcasting Zenflow service via mDNS/Bonjour
    This allows Android devices to automatically discover this PC
    """
    global zeroconf, service_info, discovery_thread, is_running
    
    if is_running:
        logger.info("Discovery service already running")
        return True
    
    try:
        # Get local IP address
        local_ip = get_local_ip()
        
        # Create Zeroconf instance
        zeroconf = Zeroconf(interfaces=InterfaceChoice.All)
        
        # Service type that matches what Android app is looking for
        service_type = "_zenflow-ws._tcp.local."
        service_name = f"Zenflow-PC.{service_type}"
        
        # Create service info
        service_info = ServiceInfo(
            service_type,
            service_name,
            addresses=[socket.inet_aton(local_ip)],
            port=port,
            properties={
                b"version": b"1.0",
                b"service": b"zenflow-remote",
                b"protocol": b"websocket",
                b"path": b"/",
            },
            server=f"zenflow-pc-{local_ip.replace('.', '-')}.local."
        )
        
        # Register the service
        zeroconf.register_service(service_info)
        
        is_running = True
        logger.info(f"Zenflow discovery service started on {local_ip}:{port}")
        logger.info(f"Service name: {service_name}")
        logger.info(f"Service type: {service_type}")
        
        return True
        
    except Exception as e:
        logger.error(f"Failed to start discovery service: {e}")
        return False

def stop_discovery():
    """Stop the mDNS/Bonjour service broadcasting"""
    global zeroconf, service_info, is_running
    
    if not is_running:
        return
    
    try:
        if zeroconf and service_info:
            zeroconf.unregister_service(service_info)
            zeroconf.close()
            
        is_running = False
        logger.info("Discovery service stopped")
        
    except Exception as e:
        logger.error(f"Error stopping discovery service: {e}")
    finally:
        zeroconf = None
        service_info = None

def get_discovery_status():
    """Get current discovery service status"""
    if is_running and service_info:
        return {
            "running": True,
            "service_name": service_info.name,
            "service_type": service_info.type,
            "port": service_info.port,
            "ip": get_local_ip()
        }
    else:
        return {"running": False}
