package com.divinixx.zenflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.extended.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.divinixx.zenflow.ui.theme.ZenFlowTheme
import com.divinixx.zenflow.ui.viewmodel.TouchpadViewModel

/**
 * ConnectionScreen - Handles WebSocket connection setup and testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    navController: NavController,
    viewModel: TouchpadViewModel = hiltViewModel()
) {
    // Use single UI state for better performance - avoids multiple recompositions
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var ipAddress by remember { mutableStateOf("") }
    var showLogs by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Title
        Text(
            text = "Zenflow Remote",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Connection Card
        ConnectionCard(
            isConnected = uiState.isConnected,
            connectionState = uiState.connectionState,
            ipAddress = ipAddress,
            onIpChanged = { ipAddress = it },
            onConnect = { viewModel.connect(ipAddress) },
            onDisconnect = { viewModel.disconnect() }
        )
        
        // Test Actions
        if (uiState.isConnected) {
            TestActionsCard(
                onSendTest = { viewModel.sendTestMessageAsync() }
            )
        }
        
        // Connection Info
        ConnectionInfoCard()
        
        // Logs
        LogsCard(
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
    ipAddress: String,
    onIpChanged: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2d2d2d)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "PC Connection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (!isConnected) {
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = onIpChanged,
                    label = { Text("PC IP Address") },
                    placeholder = { Text("Enter your PC's IP address...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
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
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Connection Status",
                        tint = if (isConnected) Color.Green else Color.Red
                    )
                    Text(
                        text = connectionState,
                        color = if (isConnected) Color.Green else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = if (isConnected) onDisconnect else onConnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) Color.Red else Color.Green
                    )
                ) {
                    Text(if (isConnected) "Disconnect" else "Connect")
                }
            }
        }
    }
}

@Composable
private fun TestActionsCard(
    onSendTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2d2d2d)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Test Connection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = onSendTest,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Test",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Send Test Message")
            }
        }
    }
}

@Composable
private fun ConnectionInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2d2d2d)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "How to Connect",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.Computer,
                    text = "Start the WebSocket server on your PC"
                )
                InfoItem(
                    icon = Icons.Default.Wifi,
                    text = "Ensure both devices are on the same WiFi network"
                )
                InfoItem(
                    icon = Icons.Default.Language,
                    text = "Enter your PC's IP address above"
                )
                InfoItem(
                    icon = Icons.Default.PlayArrow,
                    text = "Tap Connect to establish connection"
                )
            }
            
            Text(
                text = "After connecting, use the Touchpad tab for mouse control",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Cyan,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun InfoItem(
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
            tint = Color.Cyan,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
private fun LogsCard(
    logs: List<String>,
    onShowLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2d2d2d)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connection Logs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                TextButton(onClick = onShowLogs) {
                    Text("View All")
                }
            }
            
            Column(
                modifier = Modifier.heightIn(max = 120.dp)
            ) {
                logs.takeLast(3).forEach { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                if (logs.isEmpty()) {
                    Text(
                        text = "No logs yet...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
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
        containerColor = Color(0xFF2d2d2d)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "Connection Logs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                logs.forEach { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                if (logs.isEmpty()) {
                    Text(
                        text = "No logs available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1a1a1a)
@Composable
private fun ConnectionScreenPreview() {
    ZenFlowTheme {
        ConnectionScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2d2d2d)
@Composable
private fun ConnectionCardPreview() {
    ZenFlowTheme {
        ConnectionCard(
            isConnected = false,
            connectionState = "Disconnected",
            ipAddress = "192.168.1.100",
            onIpChanged = {},
            onConnect = {},
            onDisconnect = {}
        )
    }
}
