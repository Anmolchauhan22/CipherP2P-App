package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallState
import com.example.model.CallStatus
import com.example.ui.theme.MonoBackground
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderHigh
import com.example.ui.theme.MonoBorderMedium
import com.example.ui.theme.MonoBorderSubtle
import com.example.ui.theme.MonoSurfaceCard
import com.example.ui.theme.MonoSurfaceElevated
import com.example.ui.theme.MonoTextMuted
import com.example.ui.theme.MonoTextPrimary
import com.example.ui.theme.MonoTextSecondary
import com.example.ui.theme.MonoWhite
import kotlin.math.sin

@Composable
fun VoiceCallOverlay(
    callState: CallState,
    audioLevel: Float,
    onAnswerCall: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    if (callState.status == CallStatus.IDLE) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("voice_call_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Minimal Encrypted Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .border(1.dp, MonoBorderMedium, RoundedCornerShape(100.dp))
                    .background(MonoSurfaceCard)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MonoWhite,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "ENCRYPTED AUDIO • AES-256-GCM",
                    color = MonoWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            // Peer Identity & Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CallAvatarWithPulsingRipples(
                    peerName = callState.peerName,
                    audioLevel = audioLevel,
                    isRinging = callState.status != CallStatus.CONNECTED
                )

                Text(
                    text = callState.peerName.ifEmpty { "Peer Node" },
                    color = MonoTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // 4-5 Short Code Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, MonoBorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CODE:",
                        color = MonoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = callState.peerCode,
                        color = MonoWhite,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Call status or duration
                val statusText = when (callState.status) {
                    CallStatus.OUTGOING_RINGING -> "Calling..."
                    CallStatus.INCOMING_RINGING -> "Incoming Encrypted Call"
                    CallStatus.CONNECTED -> {
                        val minutes = callState.durationSeconds / 60
                        val seconds = callState.durationSeconds % 60
                        "%02d:%02d".format(minutes, seconds)
                    }
                    else -> "Connecting..."
                }

                Text(
                    text = statusText,
                    color = MonoTextSecondary,
                    fontSize = if (callState.status == CallStatus.CONNECTED) 22.sp else 14.sp,
                    fontFamily = if (callState.status == CallStatus.CONNECTED) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = FontWeight.Medium
                )

                if (callState.status == CallStatus.CONNECTED) {
                    LiveMonochromeAudioWaveformCanvas(audioLevel = audioLevel)
                }
            }

            // Controls
            if (callState.status == CallStatus.INCOMING_RINGING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reject Button: Minimal bordered circle
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = MonoBlack,
                        contentColor = MonoWhite,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .border(1.5.dp, MonoWhite, CircleShape)
                            .testTag("reject_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline Call",
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Accept Button: Stark solid white circle
                    FloatingActionButton(
                        onClick = onAnswerCall,
                        containerColor = MonoWhite,
                        contentColor = MonoBlack,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("accept_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept Call",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (callState.isMuted) MonoWhite else MonoSurfaceCard)
                            .border(1.dp, MonoBorderMedium, CircleShape)
                            .testTag("mute_call_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (callState.isMuted) MonoBlack else MonoWhite
                        )
                    ) {
                        Icon(
                            imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (callState.isMuted) "Unmute Microphone" else "Mute Microphone",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Hang Up Button: Solid white with black icon
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = MonoWhite,
                        contentColor = MonoBlack,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(66.dp)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (callState.isSpeakerOn) MonoWhite else MonoSurfaceCard)
                            .border(1.dp, MonoBorderMedium, CircleShape)
                            .testTag("speaker_call_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (callState.isSpeakerOn) MonoBlack else MonoWhite
                        )
                    ) {
                        Icon(
                            imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (callState.isSpeakerOn) "Disable Speaker" else "Enable Speaker",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallAvatarWithPulsingRipples(
    peerName: String,
    audioLevel: Float,
    isRinging: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRinging) 1.25f else 1f + (audioLevel * 0.35f),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rippleScale"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = (0.2f * (1.3f - pulseScale)).coerceIn(0.04f, 0.2f)),
                radius = (size.minDimension / 2f) * pulseScale
            )
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MonoSurfaceElevated)
                .border(1.5.dp, MonoWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = peerName.firstOrNull()?.uppercase() ?: "P",
                color = MonoWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LiveMonochromeAudioWaveformCanvas(audioLevel: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount = 26
            val barWidth = 3.dp.toPx()
            val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
            val centerY = size.height / 2f

            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing)
                val waveOffset = sin((i.toDouble() / barCount) * Math.PI).toFloat()
                val heightMultiplier = (0.15f + (waveOffset * 0.85f * (0.2f + audioLevel * 1.5f))).coerceIn(0.1f, 1f)
                val barHeight = size.height * heightMultiplier

                drawLine(
                    color = MonoWhite,
                    start = Offset(x, centerY - barHeight / 2f),
                    end = Offset(x, centerY + barHeight / 2f),
                    strokeWidth = barWidth
                )
            }
        }
    }
}
