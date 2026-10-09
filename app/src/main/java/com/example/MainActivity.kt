package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.model.CallStatus
import com.example.network.P2PConnectionManager
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.VoiceCallOverlay
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    CHAT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val connectionManager = remember {
        P2PConnectionManager(context, scope)
    }

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "Microphone permission not granted. Running in test audio mode.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val myShortCode by connectionManager.myShortCode.collectAsState()
    val connectionStats by connectionManager.connectionStats.collectAsState()
    val messages by connectionManager.messages.collectAsState()
    val isPeerTyping by connectionManager.isPeerTyping.collectAsState()
    val callState by connectionManager.callState.collectAsState()
    val audioLevel by connectionManager.audioLevel.collectAsState()

    // Automatically navigate to chat when connection is formed if user was on dashboard
    LaunchedEffect(connectionStats.isConnected) {
        if (connectionStats.isConnected && currentScreen == AppScreen.DASHBOARD) {
            currentScreen = AppScreen.CHAT
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    myShortCode = myShortCode,
                    myFingerprint = connectionManager.myFingerprint,
                    myPublicKeyBase64 = connectionManager.myPublicKeyBase64,
                    connectionStats = connectionStats,
                    onConnectWithCode = { code ->
                        scope.launch {
                            val result = connectionManager.connectWithShortCode(code)
                            result.onSuccess { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                currentScreen = AppScreen.CHAT
                            }.onFailure { err ->
                                Toast.makeText(
                                    context,
                                    err.message ?: "Connection failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onRegenerateCode = {
                        connectionManager.regenerateShortCode()
                        Toast.makeText(context, "New 5-char code generated & registered!", Toast.LENGTH_SHORT).show()
                    },
                    onOpenChat = { currentScreen = AppScreen.CHAT },
                    onStartVoiceCall = {
                        if (!hasAudioPermission) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        connectionManager.startVoiceCall(hasAudioPermission)
                    },
                    onDisconnect = {
                        connectionManager.disconnect()
                        Toast.makeText(context, "Session disconnected", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            AppScreen.CHAT -> {
                ChatScreen(
                    connectionStats = connectionStats,
                    messages = messages,
                    isPeerTyping = isPeerTyping,
                    onSendMessage = { text, disappearing ->
                        connectionManager.sendMessage(text, disappearing)
                    },
                    onSendVoiceNote = {
                        // Creates 3-second encrypted voice note sample
                        val dummyVoiceData = ByteArray(16000 * 2 * 3) // 3 seconds PCM
                        for (i in dummyVoiceData.indices step 2) {
                            val sample = (kotlin.math.sin(i.toDouble() * 0.05) * 8000).toInt().toShort()
                            dummyVoiceData[i] = (sample.toInt() and 0xFF).toByte()
                            dummyVoiceData[i + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        connectionManager.sendVoiceNote(dummyVoiceData, 3000L)
                        Toast.makeText(context, "Encrypted voice note sent!", Toast.LENGTH_SHORT).show()
                    },
                    onStartVoiceCall = {
                        if (!hasAudioPermission) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        connectionManager.startVoiceCall(hasAudioPermission)
                    },
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }
        }

        // Real-time Voice Call Overlay (shown when call is active or ringing)
        if (callState.status != CallStatus.IDLE) {
            VoiceCallOverlay(
                callState = callState,
                audioLevel = audioLevel,
                onAnswerCall = {
                    if (!hasAudioPermission) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    connectionManager.answerCall(hasAudioPermission)
                },
                onEndCall = { connectionManager.rejectOrEndCall() },
                onToggleMute = { connectionManager.toggleCallMute() },
                onToggleSpeaker = { connectionManager.toggleCallSpeaker() }
            )
        }
    }
}
