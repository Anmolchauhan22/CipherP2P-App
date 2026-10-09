package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.ConnectionStats
import com.example.model.DisappearingTimer
import com.example.model.MessageStatus
import com.example.ui.components.CryptoInspectorDialog
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.theme.MonoBackground
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderHigh
import com.example.ui.theme.MonoBorderMedium
import com.example.ui.theme.MonoBorderSubtle
import com.example.ui.theme.MonoSurface
import com.example.ui.theme.MonoSurfaceCard
import com.example.ui.theme.MonoSurfaceElevated
import com.example.ui.theme.MonoTextMuted
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    connectionStats: ConnectionStats,
    messages: List<ChatMessage>,
    isPeerTyping: Boolean,
    onSendMessage: (String, DisappearingTimer) -> Unit,
    onSendVoiceNote: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var textInput by remember { mutableStateOf("") }
    var selectedDisappearing by remember { mutableStateOf(DisappearingTimer.OFF) }
    var showMenu by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var inspectingMessage by remember { mutableStateOf<ChatMessage?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showSafetyDialog) {
        SafetyNumberDialog(
            safetyNumber = connectionStats.safetyNumber,
            peerName = connectionStats.peerName
        ) {
            showSafetyDialog = false
        }
    }

    inspectingMessage?.let { msg ->
        CryptoInspectorDialog(message = msg) {
            inspectingMessage = null
        }
    }

    Scaffold(
        containerColor = MonoBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MonoBackground,
                    titleContentColor = MonoWhite
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MonoWhite
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MonoSurfaceElevated)
                                .border(1.dp, MonoBorderMedium, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = connectionStats.peerName.firstOrNull()?.uppercase() ?: "P",
                                color = MonoWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = connectionStats.peerName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoWhite
                                )
                                // Monospaced Code Badge
                                Text(
                                    text = connectionStats.peerCode,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MonoWhite,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, MonoBorderMedium, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MonoTextSecondary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "ENCRYPTED • AES-256-GCM",
                                    fontSize = 10.sp,
                                    color = MonoTextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onStartVoiceCall,
                        modifier = Modifier.testTag("chat_voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = MonoWhite
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("chat_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MonoWhite
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(MonoSurfaceCard)
                                .border(1.dp, MonoBorderSubtle)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Verify Safety Number", color = MonoWhite) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MonoWhite
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showSafetyDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Disappearing: ${selectedDisappearing.label}",
                                        color = MonoWhite
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MonoWhite
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    selectedDisappearing = when (selectedDisappearing) {
                                        DisappearingTimer.OFF -> DisappearingTimer.SEC_30
                                        DisappearingTimer.SEC_30 -> DisappearingTimer.MIN_5
                                        DisappearingTimer.MIN_5 -> DisappearingTimer.HOUR_1
                                        DisappearingTimer.HOUR_1 -> DisappearingTimer.OFF
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Disappearing Messages Banner
            AnimatedVisibility(visible = selectedDisappearing != DisappearingTimer.OFF) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MonoSurface)
                        .border(1.dp, MonoBorderSubtle)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MonoTextPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Disappearing messages: ${selectedDisappearing.label}",
                        color = MonoTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MonoSurface)
                                .border(1.dp, MonoBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { showSafetyDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MonoTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "End-to-End Encrypted Session • Tap to verify",
                                color = MonoTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        onInspectCrypto = { inspectingMessage = message }
                    )
                }

                if (isPeerTyping) {
                    item {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MonoSurface)
                                .border(1.dp, MonoBorderSubtle, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${connectionStats.peerName} is typing...",
                                color = MonoTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MonoSurface)
                    .border(1.dp, MonoBorderSubtle)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onSendVoiceNote,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MonoBlack)
                        .border(1.dp, MonoBorderMedium, CircleShape)
                        .testTag("record_voice_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Send Voice Note",
                        tint = MonoWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    placeholder = {
                        Text(
                            text = "Message...",
                            color = MonoTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonoWhite,
                        unfocusedBorderColor = MonoBorderSubtle,
                        focusedTextColor = MonoWhite,
                        unfocusedTextColor = MonoWhite,
                        focusedContainerColor = MonoBlack,
                        unfocusedContainerColor = MonoBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput.trim(), selectedDisappearing)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) MonoWhite else MonoSurfaceElevated)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (textInput.isNotBlank()) MonoBlack else MonoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onInspectCrypto: () -> Unit
) {
    val isMe = message.isFromMe
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isMe) MonoWhite else MonoSurfaceCard
    val textColor = if (isMe) MonoBlack else MonoWhite
    val metaColor = if (isMe) Color(0xFF52525B) else MonoTextMuted
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = if (isMe) MonoWhite else MonoBorderSubtle,
                    shape = shape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (message.isVoiceNote) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isMe) MonoBlack else MonoWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Voice Note",
                                tint = if (isMe) MonoWhite else MonoBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Encrypted Voice Note",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "PCM • ${message.audioDurationMs / 1000}s",
                                color = metaColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = message.plaintext,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )
                }

                // Metadata row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        color = metaColor,
                        fontSize = 10.sp
                    )

                    if (message.disappearingSeconds > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing",
                            tint = metaColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    // Crypto inspect pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .clickable { onInspectCrypto() }
                            .padding(horizontal = 3.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Crypto Details",
                            tint = metaColor,
                            modifier = Modifier.size(9.dp)
                        )
                        Text(
                            text = "AES",
                            color = metaColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isMe) {
                        Icon(
                            imageVector = when (message.status) {
                                MessageStatus.SENT -> Icons.Default.Check
                                MessageStatus.DELIVERED, MessageStatus.READ -> Icons.Default.DoneAll
                            },
                            contentDescription = "Status",
                            tint = metaColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
