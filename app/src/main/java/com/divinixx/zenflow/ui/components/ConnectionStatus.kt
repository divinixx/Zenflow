package com.divinixx.zenflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Connection Status Component
 * Shows the current connection state with appropriate visual indicators
 */
@Composable
fun ConnectionStatus(
    isConnected: Boolean,
    connectionState: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2d2d2d)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = "Connection Status",
                tint = if (isConnected) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = if (isConnected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) Color.Green else Color.Red
                )
                
                Text(
                    text = connectionState,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}
