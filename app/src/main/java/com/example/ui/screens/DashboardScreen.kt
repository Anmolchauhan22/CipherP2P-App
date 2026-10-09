package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStats
import com.example.model.PeerHandshakePayload
import com.example.protocol.ShortCodeEngine
import com.example.ui.components.AlgorithmExplainerDialog
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

@Composable
fun DashboardScreen(
    myShortCode: String,
    myFingerprint: String,
    myPublicKeyBase64: String,
    connectionStats: ConnectionStats,
    onConnectWithCode: (String) -> Unit,
    onRegenerateCode: () -> Unit,
    onOpenChat: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var inputCode by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    var showAlgoDialog by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }

    if (showAlgoDialog) {
        val dummyPayload = PeerHandshakePayload(
            shortCode = myShortCode.ifEmpty { "K9X2B" },
            peerName = "My Device",
            publicKeyBase64 = myPublicKeyBase64,
            lanIp = "192.168.1.104",
            port = 8988,
            sessionNonce = "NONCE_TEST",
            fingerprint = myFingerprint
        )
        val breakdown = ShortCodeEngine.getAlgorithmBreakdown(dummyPayload)
        AlgorithmExplainerDialog(breakdown = breakdown) {
            showAlgoDialog = false
        }
    }

    if (showSafetyDialog && connectionStats.isConnected) {
        SafetyNumberDialog(
            safetyNumber = connectionStats.safetyNumber,
            peerName = connectionStats.peerName
        ) {
            showSafetyDialog = false
        }
    }

    Scaffold(
        containerColor = MonoBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Minimalist Monochrome Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MonoWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = MonoBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CIPHER P2P",
                            color = MonoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (connectionStats.isConnected) MonoWhite else MonoTextMuted)
                            )
                            Text(
                                text = if (connectionStats.isConnected) "LINK ENCRYPTED" else "SYSTEM READY",
                                color = if (connectionStats.isConnected) MonoTextPrimary else MonoTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Info / Algorithm Explainer Button
                IconButton(
                    onClick = { showAlgoDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MonoBorderSubtle, CircleShape)
                        .testTag("open_algo_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Algorithm Details",
                        tint = MonoTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Card 1: My 4-5 Alphanumeric Pairing Code
            Card(
                colors = CardDefaults.cardColors(containerColor = MonoSurfaceCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MonoBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PAIRING CODE",
                                color = MonoTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "4-5 character short-code identity",
                                color = MonoTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = onRegenerateCode,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MonoBorderSubtle, RoundedCornerShape(8.dp))
                                    .testTag("regenerate_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate Code",
                                    tint = MonoTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Pairing Code", myShortCode))
                                    Toast.makeText(context, "Copied: $myShortCode", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MonoBorderSubtle, RoundedCornerShape(8.dp))
                                    .testTag("copy_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = MonoTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Monospaced 5-digit character boxes in minimal high contrast
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayCode = myShortCode.ifEmpty { "P2P01" }
                        displayCode.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(width = 46.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MonoBlack)
                                    .border(1.dp, MonoBorderHigh, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    color = MonoWhite,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Footer metrics & algorithm inspection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FP: ${myFingerprint.take(9)}",
                            color = MonoTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, MonoBorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { showAlgoDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Algorithm Details",
                                color = MonoTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Card 2: Connect via Peer's 4-5 Short Code
            Card(
                colors = CardDefaults.cardColors(containerColor = MonoSurfaceCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MonoBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "PEER CONNECTION",
                            color = MonoTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Enter a 4-5 character code to begin encrypted session",
                            color = MonoTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                if (it.length <= 5) {
                                    inputCode = it.uppercase()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("peer_code_input"),
                            placeholder = { Text("e.g. B0B99", color = MonoTextMuted, fontSize = 14.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (inputCode.isNotBlank()) {
                                        isConnecting = true
                                        onConnectWithCode(inputCode)
                                        isConnecting = false
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MonoWhite,
                                unfocusedBorderColor = MonoBorderSubtle,
                                focusedTextColor = MonoWhite,
                                unfocusedTextColor = MonoWhite,
                                focusedContainerColor = MonoBlack,
                                unfocusedContainerColor = MonoBlack
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (inputCode.isNotBlank()) {
                                    isConnecting = true
                                    onConnectWithCode(inputCode)
                                    isConnecting = false
                                }
                            },
                            enabled = inputCode.length >= 4 && !isConnecting,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("connect_peer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoWhite,
                                contentColor = MonoBlack,
                                disabledContainerColor = MonoSurfaceElevated,
                                disabledContentColor = MonoTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MonoBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Connect", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    // Test Peer Quick Connect
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MonoBlack)
                            .border(1.dp, MonoBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Emulator Test Peers:",
                            color = MonoTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    inputCode = "B0B99"
                                    onConnectWithCode("B0B99")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_connect_bob_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MonoWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorderMedium),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Bob (B0B99)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = {
                                    inputCode = "ALICE"
                                    onConnectWithCode("ALICE")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_connect_alice_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MonoWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorderMedium),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Alice (ALICE)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Card 3: Active Encrypted Session (when connected)
            if (connectionStats.isConnected) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MonoSurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MonoWhite, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MonoWhite),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock",
                                        tint = MonoBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = connectionStats.peerName,
                                        color = MonoTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Code: ${connectionStats.peerCode} • RTT: ${connectionStats.latencyMs}ms",
                                        color = MonoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showSafetyDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MonoBorderSubtle, CircleShape)
                                    .testTag("open_safety_dialog_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Safety",
                                    tint = MonoTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Session Actions: Open Chat, Voice Call, Disconnect
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenChat,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("open_chat_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MonoWhite,
                                    contentColor = MonoBlack
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    tint = MonoBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onStartVoiceCall,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("start_voice_call_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MonoSurfaceElevated,
                                    contentColor = MonoWhite
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Voice Call",
                                    tint = MonoWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            IconButton(
                                onClick = onDisconnect,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MonoBorderMedium, RoundedCornerShape(10.dp))
                                    .testTag("disconnect_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Disconnect",
                                    tint = MonoWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 4: Architecture Specifications
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MonoSurface)
                    .border(1.dp, MonoBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SPECIFICATIONS",
                    color = MonoTextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "• Key Agreement: ECDH SECP256R1\n• Packet Cipher: Authenticated AES-256-GCM\n• Code Scheme: Crockford Base32 (33.5M Keyspace)\n• Voice: 16kHz PCM encrypted frames",
                    color = MonoTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
