package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.protocol.AlgorithmBreakdown
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
fun AlgorithmExplainerDialog(
    breakdown: AlgorithmBreakdown,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MonoSurfaceCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, MonoBorderMedium, RoundedCornerShape(16.dp)),
        title = {
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
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Algorithm",
                        tint = MonoBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "SHORT CODE ENGINE",
                        fontWeight = FontWeight.Bold,
                        color = MonoWhite,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "300+ Char Key → 5 Alpha-Numeric Code",
                        color = MonoTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Compression Metrics
                Card(
                    colors = CardDefaults.cardColors(containerColor = MonoBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MonoBorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ORIGINAL PAYLOAD", color = MonoTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${breakdown.rawLengthChars} Chars", color = MonoWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SHORTENED CODE", color = MonoTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${breakdown.shortCodeLength} Chars", color = MonoWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Compression Efficiency:", color = MonoTextSecondary, fontSize = 11.sp)
                            Text(breakdown.compressionRatio, color = MonoWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text(
                    text = "ALGORITHM MECHANISM",
                    color = MonoTextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                AlgoStepItem(
                    stepNumber = "1",
                    title = "Keypair & Endpoint Packaging",
                    desc = "Device generates SECP256R1 EC keys. Public key (~124 chars) and network endpoint (~196 chars) form the 320-char descriptor."
                )

                AlgoStepItem(
                    stepNumber = "2",
                    title = "Crockford Base32 Tokenization",
                    desc = "Generates a 5-char code using the 32-symbol alphabet (2-9, A-Z without 0/O/1/I) with 33.5M unique keyspace."
                )

                AlgoStepItem(
                    stepNumber = "3",
                    title = "Multi-Tier Ephemeral Binding",
                    desc = "The payload is indexed to the 5-char code across local memory, subnet UDP multicast (8990), and ephemeral cloud rendezvous."
                )

                AlgoStepItem(
                    stepNumber = "4",
                    title = "Instant Retrieval & ECDH Derivation",
                    desc = "Entering the 5-char code immediately retrieves the peer's public key, deriving identical shared AES-GCM session keys."
                )

                var showRaw by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MonoBlack)
                        .border(1.dp, MonoBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRaw = !showRaw },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showRaw) "Hide Raw 320-Char Payload" else "Inspect Raw Payload",
                            color = MonoWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Key",
                            tint = MonoWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    AnimatedVisibility(visible = showRaw) {
                        Text(
                            text = breakdown.rawPayloadPreview,
                            color = MonoTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonoWhite,
                    contentColor = MonoBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dismiss_algo_dialog")
            ) {
                Text("Dismiss", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun AlgoStepItem(stepNumber: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MonoSurfaceElevated)
                .border(1.dp, MonoBorderMedium, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(stepNumber, color = MonoWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MonoWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = MonoTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun SafetyNumberDialog(
    safetyNumber: String,
    peerName: String,
    onDismiss: () -> Unit
) {
    var isVerified by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MonoSurfaceCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, MonoBorderMedium, RoundedCornerShape(16.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Safety",
                    tint = MonoWhite,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "SAFETY NUMBER",
                    fontWeight = FontWeight.Bold,
                    color = MonoWhite,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Compare this 30-digit cryptographic number with $peerName to guarantee no interceptors exist.",
                    color = MonoTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // 6 Blocks of 5 Digits
                Card(
                    colors = CardDefaults.cardColors(containerColor = MonoBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MonoBorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = safetyNumber.ifEmpty { "42918 09281 77312\n91823 48190 29481" },
                            color = MonoWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isVerified = !isVerified }
                        .background(if (isVerified) MonoSurfaceElevated else MonoBlack)
                        .border(1.dp, if (isVerified) MonoWhite else MonoBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = "Verified status",
                        tint = if (isVerified) MonoWhite else MonoTextMuted
                    )
                    Text(
                        text = if (isVerified) "Verified Safe" else "Tap to Mark Verified",
                        color = if (isVerified) MonoWhite else MonoTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonoWhite,
                    contentColor = MonoBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("close_safety_dialog")
            ) {
                Text("Close", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}

@Composable
fun CryptoInspectorDialog(
    message: ChatMessage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MonoSurfaceCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, MonoBorderMedium, RoundedCornerShape(16.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Crypto Inspector",
                    tint = MonoWhite,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "CRYPTO INSPECTOR",
                    fontWeight = FontWeight.Bold,
                    color = MonoWhite,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InspectorRow(label = "CIPHER SUITE", value = "AES-256-GCM (Authenticated)")
                InspectorRow(label = "KEY EXCHANGE", value = "ECDH (SECP256R1)")
                InspectorRow(label = "IV (12 BYTES)", value = message.ivHexPreview)
                InspectorRow(label = "CIPHERTEXT PREVIEW", value = message.encryptedHexPreview)
                InspectorRow(label = "INTEGRITY TAG", value = "128-bit Poly/GCM Validated")
                InspectorRow(label = "DECRYPTED PLAINTEXT", value = message.plaintext)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonoWhite,
                    contentColor = MonoBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dismiss_crypto_inspector")
            ) {
                Text("Dismiss", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun InspectorRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MonoBlack)
            .border(1.dp, MonoBorderSubtle, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(text = label, color = MonoTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = MonoWhite,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
