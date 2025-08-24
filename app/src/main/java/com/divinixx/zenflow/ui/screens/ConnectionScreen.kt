package com.divinixx.zenflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.divinixx.zenflow.ui.theme.ZenFlowTheme
import com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel
import com.divinixx.zenflow.network.DiscoveredService
import com.divinixx.zenflow.network.NetworkDiscoveryManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry Point for accessing NetworkDiscoveryManager in Composables
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetworkDiscoveryManagerEntryPoint {
    fun networkDiscoveryManager(): NetworkDiscoveryManager
}

/**
 * ConnectionScreen - Handles WebSocket connection setup and testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    navController: NavController,
    viewModel: TouchpadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Get NetworkDiscoveryManager from Hilt singleton
    val networkDiscoveryManager = remember { 
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NetworkDiscoveryManagerEntryPoint::class.java
        ).networkDiscoveryManager()
    }
    
    // Use single UI state for better performance - avoids multiple recompositions
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val discoveredServices by networkDiscoveryManager.discoveredServices.collectAsStateWithLifecycle()
    val isDiscovering by networkDiscoveryManager.isDiscovering.collectAsStateWithLifecycle()
    
    var showLogs by remember { mutableStateOf(false) }
    
    // Always run auto-discovery when not connected
    LaunchedEffect(uiState.isConnected) {
        if (!uiState.isConnected) {
            networkDiscoveryManager.startDiscovery()
        } else {
            networkDiscoveryManager.stopDiscovery()
        }
    }
    
    // Stop discovery when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            networkDiscoveryManager.stopDiscovery()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1421),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Beautiful Header
        HeaderSection()
        
        // Connection Card
        ConnectionCard(
            isConnected = uiState.isConnected,
            connectionState = uiState.connectionState,
            onDisconnect = { viewModel.disconnect() },
            isDiscovering = isDiscovering,
            discoveredServices = discoveredServices,
            onServiceSelected = { service ->
                viewModel.connect(service.ipAddress, service.port)
            },
            onRefreshDiscovery = { 
                networkDiscoveryManager.stopDiscovery()
                networkDiscoveryManager.startDiscovery()
            },
            networkDiscoveryManager = networkDiscoveryManager
        )
        
        // Connection Guide - Only show when not connected
        if (!uiState.isConnected) {
            ConnectionGuideCard()
        }
        
        // Activity Logs - Compact version
        ActivityLogsCard(
            logs = uiState.logMessages,
            onShowLogs = { showLogs = true }
        )
    }
    
    // Logs Bottom Sheet
    if (showLogs) {
        LogsBottomSheet(
            logs = uiState.logMessages,
            onDismiss = { showLogs = false }
        )
    }
}

@Composable
private fun ConnectionCard(
    isConnected: Boolean,
    connectionState: String,
    onDisconnect: () -> Unit,
    isDiscovering: Boolean,
    discoveredServices: List<DiscoveredService>,
    onServiceSelected: (DiscoveredService) -> Unit,
    onRefreshDiscovery: () -> Unit,
    networkDiscoveryManager: NetworkDiscoveryManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2A3D).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Connection Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) Color(0xFF4CAF50) 
                                else Color(0xFFFF5722)
                            )
                    )
                    
                    Text(
                        text = if (isConnected) "Connected" else "Searching...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                
                // Disconnect button when connected
                if (isConnected) {
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Disconnect",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Disconnect", fontSize = 12.sp)
                    }
                }
            }
            
            if (!isConnected) {
                // Auto-discovery section
                AutoDiscoverySection(
                    isDiscovering = isDiscovering,
                    discoveredServices = discoveredServices,
                    onServiceSelected = onServiceSelected,
                    onRefreshDiscovery = onRefreshDiscovery
                )
            } else {
                // Connected State
                ConnectedStateSection()
            }
        }
    }
}

@Composable
private fun AutoDiscoverySection(
    isDiscovering: Boolean,
    discoveredServices: List<DiscoveredService>,
    onServiceSelected: (DiscoveredService) -> Unit,
    onRefreshDiscovery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF263238).copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Servers",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Available Servers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                IconButton(
                    onClick = onRefreshDiscovery,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            when {
                discoveredServices.isNotEmpty() -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        discoveredServices.forEach { service ->
                            DiscoveredServiceItem(
                                service = service,
                                onServiceSelected = { onServiceSelected(service) }
                            )
                        }
                    }
                }
                
                isDiscovering -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF64B5F6),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Discovering servers...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No servers",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No servers found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Make sure your PC server is running\nand both devices are on the same network",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF616161),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedStateSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Connected",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp)
        )
        
        Text(
            text = "Ready to Control",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "Switch to Touchpad tab to start controlling your PC",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB0BEC5),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DiscoveredServiceItem(
    service: DiscoveredService,
    onServiceSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onServiceSelected() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF37474F).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Icon with background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF64B5F6).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = "PC Server",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = service.deviceName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "${service.ipAddress}:${service.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0BEC5)
                )
            }
            
            // Connect Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Connect",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectionGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2A3D).copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Quick Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickGuideItem(
                    icon = Icons.Default.Computer,
                    text = "Start WebSocket server on your PC"
                )
                QuickGuideItem(
                    icon = Icons.Default.Wifi,
                    text = "Connect both devices to same WiFi"
                )
                QuickGuideItem(
                    icon = Icons.Default.PlayArrow,
                    text = "Tap discovered server to connect"
                )
            }
        }
    }
}

@Composable
private fun QuickGuideItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF90CAF9),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCFD8DC)
        )
    }
}

@Composable
private fun ActivityLogsCard(
    logs: List<String>,
    onShowLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2A3D).copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Activity",
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Activity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                if (logs.isNotEmpty()) {
                    TextButton(
                        onClick = onShowLogs,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }
            }
            
            if (logs.isNotEmpty()) {
                Column(
                    modifier = Modifier.heightIn(max = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    logs.takeLast(2).forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "No activity yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsBottomSheet(
    logs: List<String>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2A3D),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Activity Logs",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Activity Logs",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFB0BEC5)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (logs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    logs.forEach { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF263238).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCFD8DC),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No logs",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No activity logs yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Connection activities will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1421)
@Composable
private fun ConnectionScreenPreview() {
    ZenFlowTheme {
        ConnectionScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E2A3D)
@Composable
private fun ConnectionCardPreview() {
    ZenFlowTheme {
        ConnectionCard(
            isConnected = false,
            connectionState = "Disconnected",
            onDisconnect = {},
            isDiscovering = true,
            discoveredServices = emptyList(),
            onServiceSelected = {},
            onRefreshDiscovery = {},
            networkDiscoveryManager = NetworkDiscoveryManager(LocalContext.current)
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Icon with glow effect
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF64B5F6),
                            Color(0xFF1976D2),
                            Color(0xFF0D47A1)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Zenflow",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Title with gradient
        Text(
            text = "Zenflow",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        
        Text(
            text = "Remote Control Made Simple",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF90CAF9),
            fontWeight = FontWeight.Light
        )
    }
}
