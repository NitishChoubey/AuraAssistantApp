package com.nitish.auraassistant.presentation.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nitish.auraassistant.domain.model.ChatMessage
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraGlow
import com.nitish.auraassistant.presentation.theme.AuraOnSurface
import com.nitish.auraassistant.presentation.theme.AuraOnSurfaceDim
import com.nitish.auraassistant.presentation.theme.AuraSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryList(
    messages: List<ChatMessage>,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3 && hasMorePages && !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    if (messages.isEmpty() && !isLoadingMore) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✦", fontSize = 36.sp, color = AuraAccent.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No conversations yet",
                    style = MaterialTheme.typography.bodyMedium.copy(color = AuraOnSurfaceDim)
                )
                Text(
                    "Say something to Aura",
                    style = MaterialTheme.typography.bodySmall.copy(color = AuraOnSurfaceDim.copy(0.6f))
                )
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true
    ) {
        items(messages, key = { it.id }) { message ->
            ChatBubble(message = message)
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AuraAccent,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(AuraAccent, AuraGlow))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", fontSize = 14.sp, color = AuraDeepBg)
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isUser) AuraAccent.copy(alpha = 0.2f) else AuraSurfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) AuraAccentBright else AuraOnSurface,
                        lineHeight = 20.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AuraOnSurfaceDim.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            )
        }

        if (isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AuraSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("U", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AuraAccentBright)
            }
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(ts))
}

