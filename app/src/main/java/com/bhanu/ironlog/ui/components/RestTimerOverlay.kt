package com.bhanu.ironlog.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhanu.ironlog.data.local.pojo.RestTimerInfo
import com.bhanu.ironlog.data.model.RestTimerState
import java.util.Locale

@Composable
fun RestTimerOverlay(
    timerInfo: RestTimerInfo,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAdjust: (Int) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    if (timerInfo.state == RestTimerState.IDLE || timerInfo.state == RestTimerState.DISMISSED) return

    val isCompleted = timerInfo.state == RestTimerState.COMPLETED
    val containerColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp), // Height above bottom nav
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCompleted) "✓ Rest Complete" else "Resting...",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val timeText = if (isCompleted) {
                        String.format(Locale.getDefault(), "+%02d:%02d", timerInfo.elapsedGraceSeconds / 60, timerInfo.elapsedGraceSeconds % 60)
                    } else {
                        String.format(Locale.getDefault(), "%02d:%02d", timerInfo.remainingSeconds / 60, timerInfo.remainingSeconds % 60)
                    }

                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isCompleted) {
                        IconButton(onClick = { onAdjust(15) }) {
                            Icon(Icons.Default.Add, contentDescription = "+15s", modifier = Modifier.size(20.dp))
                            Text("+15", fontSize = 8.sp, modifier = Modifier.padding(top = 16.dp))
                        }

                        IconButton(onClick = { onAdjust(30) }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "+30s", modifier = Modifier.size(20.dp))
                            Text("+30", fontSize = 8.sp, modifier = Modifier.padding(top = 16.dp))
                        }
                        
                        if (timerInfo.state == RestTimerState.RUNNING) {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause")
                            }
                        } else {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            }
                        }
                    }

                    TextButton(
                        onClick = if (isCompleted) onDismiss else onSkip,
                        colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
                    ) {
                        Text(if (isCompleted) "DISMISS" else "SKIP")
                    }
                }
            }
        }
    }
}
