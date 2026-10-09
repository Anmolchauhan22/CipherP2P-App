package com.example.model

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

enum class DisappearingTimer(val seconds: Long, val label: String) {
    OFF(0, "Off"),
    SEC_30(30, "30s"),
    MIN_5(300, "5m"),
    HOUR_1(3600, "1h")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val isFromMe: Boolean,
    val plaintext: String,
    val encryptedHexPreview: String,
    val ivHexPreview: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val disappearingSeconds: Long = 0,
    val isVoiceNote: Boolean = false,
    val audioDurationMs: Long = 0,
    val audioData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChatMessage
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class CallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    ENDED
}

data class CallState(
    val status: CallStatus = CallStatus.IDLE,
    val peerName: String = "",
    val peerCode: String = "",
    val durationSeconds: Long = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val audioLevel: Float = 0f, // 0.0 to 1.0 for waveform animation
    val encryptionVerified: Boolean = true
)

data class PeerHandshakePayload(
    val shortCode: String,
    val peerName: String,
    val publicKeyBase64: String,
    val lanIp: String,
    val port: Int,
    val sessionNonce: String,
    val fingerprint: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConnectionStats(
    val isConnected: Boolean = false,
    val peerName: String = "",
    val peerCode: String = "",
    val safetyNumber: String = "",
    val encryptionType: String = "ECDH P-256 + AES-256-GCM",
    val latencyMs: Long = 12,
    val bytesEncrypted: Long = 0,
    val packetsSent: Long = 0,
    val packetsReceived: Long = 0
)
