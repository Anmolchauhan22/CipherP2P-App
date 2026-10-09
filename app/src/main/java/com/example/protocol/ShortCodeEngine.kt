package com.example.protocol

import android.util.Base64
import android.util.Log
import com.example.model.PeerHandshakePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * ShortCodeEngine: Implements the Shortening and Retrieval algorithm that transforms
 * large 300+ character cryptographic handshake payloads (ECDH Public Keys, IP, Port, Nonces)
 * into compact 4 to 5 character alphanumeric pairing codes.
 */
object ShortCodeEngine {
    private const val TAG = "ShortCodeEngine"

    // Crockford Base32 alphabet (32 characters, excluding ambiguous 0, O, 1, I)
    private const val ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val CODE_LENGTH = 5
    private const val UDP_DISCOVERY_PORT = 8990

    private val secureRandom = SecureRandom()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    // Tier 1: Local In-Memory & Process Cache
    private val localRegistry = ConcurrentHashMap<String, PeerHandshakePayload>()

    // Ephemeral cloud KV rendezvous endpoint for cross-network / emulator resolution
    // Fallback public key-value store endpoint
    private const val CLOUD_RENDEZVOUS_URL = "https://kvdb.io/CipherP2P_App_Relay/"

    init {
        // Pre-populate simulated peers for testing in single-device emulator environments
        registerSimulatedPeers()
    }

    private fun registerSimulatedPeers() {
        val simulatedKeyAlice = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7Z5f8cE1oK9w2A5bQmN0h4P9rT1lV8xG3kL7pQ0sR5aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890abcdef=="
        val simulatedKeyBob = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEd2P3kL8mN1oQ5rT7vX9zB0cF4hJ6aE8gK2mS4uW6yA1cD3fH5jL7nO9qS1uV3wX5yZ7aB9cD1eF3gH5jK7mN=="

        val alice = PeerHandshakePayload(
            shortCode = "ALICE",
            peerName = "Alice (Secure Node)",
            publicKeyBase64 = simulatedKeyAlice,
            lanIp = "127.0.0.1",
            port = 8988,
            sessionNonce = "NONCE_ALICE_7788",
            fingerprint = "8E41-C290-7F11-4B9A"
        )
        val bob = PeerHandshakePayload(
            shortCode = "B0B99",
            peerName = "Bob (Encrypted Relay)",
            publicKeyBase64 = simulatedKeyBob,
            lanIp = "127.0.0.1",
            port = 8989,
            sessionNonce = "NONCE_BOB_3344",
            fingerprint = "F512-3D8B-9E04-A78C"
        )
        localRegistry["ALICE"] = alice
        localRegistry["B0B99"] = bob
    }

    /**
     * Generates a 5-character high-entropy alphanumeric short code.
     */
    fun generateShortCode(): String {
        val chars = CharArray(CODE_LENGTH)
        for (i in 0 until CODE_LENGTH) {
            val index = secureRandom.nextInt(ALPHABET.length)
            chars[i] = ALPHABET[index]
        }
        return String(chars)
    }

    /**
     * Shortening Algorithm:
     * Takes the long 300+ character handshake profile, binds it to a 4-5 char short code,
     * and publishes it across Local Cache, LAN UDP broadcast, and Cloud Rendezvous.
     */
    suspend fun shortenAndRegister(
        payload: PeerHandshakePayload,
        customCode: String? = null
    ): String = withContext(Dispatchers.IO) {
        val code = (customCode ?: generateShortCode()).uppercase().trim()
        val boundPayload = payload.copy(shortCode = code)

        // Tier 1: Local In-Memory Registry
        localRegistry[code] = boundPayload
        Log.d(TAG, "Shortening algorithm: bound long payload (${boundPayload.publicKeyBase64.length} chars) to short code '$code'")

        // Tier 2: LAN Multicast / UDP Broadcast Announcement
        try {
            announceOnSubnet(code, boundPayload)
        } catch (e: Exception) {
            Log.w(TAG, "LAN announcement skipped: ${e.message}")
        }

        // Tier 3: Ephemeral Cloud Rendezvous Registry
        try {
            publishToCloudRendezvous(code, boundPayload)
        } catch (e: Exception) {
            Log.w(TAG, "Cloud rendezvous publishing skipped: ${e.message}")
        }

        return@withContext code
    }

    /**
     * Retrieval Algorithm:
     * Retrieves the complete long cryptographic connection profile using only the 4-5 char code.
     */
    suspend fun retrievePayload(inputCode: String): PeerHandshakePayload? = withContext(Dispatchers.IO) {
        val code = inputCode.uppercase().trim().replace("-", "").replace(" ", "")
        if (code.length < 4 || code.length > 5) {
            Log.w(TAG, "Retrieval failed: code length must be 4 or 5 characters: '$code'")
            return@withContext null
        }

        // Stage 1: Check Local In-Memory / Subnet cache
        localRegistry[code]?.let {
            Log.d(TAG, "Retrieval success via Local Tier for code: $code")
            return@withContext it
        }

        // Stage 2: Query LAN Subnet via UDP Broadcast
        try {
            val lanPayload = querySubnetForCode(code)
            if (lanPayload != null) {
                localRegistry[code] = lanPayload
                Log.d(TAG, "Retrieval success via LAN Discovery for code: $code")
                return@withContext lanPayload
            }
        } catch (e: Exception) {
            Log.w(TAG, "LAN query skipped: ${e.message}")
        }

        // Stage 3: Query Cloud Ephemeral Rendezvous
        try {
            val cloudPayload = fetchFromCloudRendezvous(code)
            if (cloudPayload != null) {
                localRegistry[code] = cloudPayload
                Log.d(TAG, "Retrieval success via Cloud Rendezvous for code: $code")
                return@withContext cloudPayload
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cloud rendezvous query failed: ${e.message}")
        }

        return@withContext null
    }

    /**
     * Broadcasts short code announcement over local Wi-Fi / subnet.
     */
    private fun announceOnSubnet(code: String, payload: PeerHandshakePayload) {
        val socket = DatagramSocket()
        socket.broadcast = true
        val json = serializePayload(payload).toString()
        val data = "ANNOUNCE:$code:$json".toByteArray(Charsets.UTF_8)
        val packet = DatagramPacket(
            data,
            data.size,
            InetAddress.getByName("255.255.255.255"),
            UDP_DISCOVERY_PORT
        )
        socket.send(packet)
        socket.close()
    }

    /**
     * Queries local subnet for a peer advertising the given short code.
     */
    private fun querySubnetForCode(code: String): PeerHandshakePayload? {
        val socket = DatagramSocket()
        socket.broadcast = true
        socket.soTimeout = 400 // fast timeout for responsiveness
        val query = "QUERY:$code".toByteArray(Charsets.UTF_8)
        val packet = DatagramPacket(
            query,
            query.size,
            InetAddress.getByName("255.255.255.255"),
            UDP_DISCOVERY_PORT
        )
        socket.send(packet)

        val buffer = ByteArray(2048)
        val responsePacket = DatagramPacket(buffer, buffer.size)
        return try {
            socket.receive(responsePacket)
            val response = String(responsePacket.data, 0, responsePacket.length, Charsets.UTF_8)
            if (response.startsWith("REPLY:$code:")) {
                val jsonStr = response.substring("REPLY:$code:".length)
                deserializePayload(JSONObject(jsonStr))
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            socket.close()
        }
    }

    /**
     * Publishes payload mapping to ephemeral cloud KV relay.
     */
    private fun publishToCloudRendezvous(code: String, payload: PeerHandshakePayload) {
        val json = serializePayload(payload).toString()
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$CLOUD_RENDEZVOUS_URL$code")
            .put(requestBody)
            .build()
        httpClient.newCall(request).execute().close()
    }

    /**
     * Fetches payload from ephemeral cloud KV relay.
     */
    private fun fetchFromCloudRendezvous(code: String): PeerHandshakePayload? {
        val request = Request.Builder()
            .url("$CLOUD_RENDEZVOUS_URL$code")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                return deserializePayload(JSONObject(body))
            }
        }
        return null
    }

    private fun serializePayload(payload: PeerHandshakePayload): JSONObject {
        return JSONObject().apply {
            put("shortCode", payload.shortCode)
            put("peerName", payload.peerName)
            put("publicKeyBase64", payload.publicKeyBase64)
            put("lanIp", payload.lanIp)
            put("port", payload.port)
            put("sessionNonce", payload.sessionNonce)
            put("fingerprint", payload.fingerprint)
            put("timestamp", payload.timestamp)
        }
    }

    private fun deserializePayload(json: JSONObject): PeerHandshakePayload {
        return PeerHandshakePayload(
            shortCode = json.optString("shortCode"),
            peerName = json.optString("peerName"),
            publicKeyBase64 = json.optString("publicKeyBase64"),
            lanIp = json.optString("lanIp", "127.0.0.1"),
            port = json.optInt("port", 8988),
            sessionNonce = json.optString("sessionNonce"),
            fingerprint = json.optString("fingerprint"),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }

    /**
     * Returns algorithm diagnostics explaining the compression ratio and security guarantees.
     */
    fun getAlgorithmBreakdown(payload: PeerHandshakePayload): AlgorithmBreakdown {
        val rawSerialized = serializePayload(payload).toString()
        val rawLength = rawSerialized.length
        val shortCodeLength = payload.shortCode.length
        val compressionRatio = "%.1f%%".format((1f - (shortCodeLength.toFloat() / rawLength.toFloat())) * 100)

        return AlgorithmBreakdown(
            rawLengthChars = rawLength,
            shortCode = payload.shortCode,
            shortCodeLength = shortCodeLength,
            compressionRatio = compressionRatio,
            keyspaceCombinations = "33,554,432 (32^5 Crockford Base32)",
            rawPayloadPreview = rawSerialized.take(60) + "..." + rawSerialized.takeLast(40)
        )
    }
}

data class AlgorithmBreakdown(
    val rawLengthChars: Int,
    val shortCode: String,
    val shortCodeLength: Int,
    val compressionRatio: String,
    val keyspaceCombinations: String,
    val rawPayloadPreview: String
)
