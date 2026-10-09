package com.example.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedResult(
    val iv: ByteArray,
    val ciphertext: ByteArray,
    val ivBase64: String = Base64.encodeToString(iv, Base64.NO_WRAP),
    val ciphertextBase64: String = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedResult
        if (!iv.contentEquals(other.iv)) return false
        if (!ciphertext.contentEquals(other.ciphertext)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }
}

object CryptoManager {
    private const val EC_CURVE = "secp256r1"
    private const val AES_GCM_ALGO = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12

    private val secureRandom = SecureRandom()

    /**
     * Generates an Elliptic Curve (EC SECP256R1) KeyPair for ECDH.
     */
    fun generateKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        val ecSpec = ECGenParameterSpec(EC_CURVE)
        kpg.initialize(ecSpec, secureRandom)
        return kpg.generateKeyPair()
    }

    /**
     * Performs Elliptic Curve Diffie-Hellman (ECDH) key agreement.
     */
    fun deriveSharedSecret(privateKey: PrivateKey, peerPublicKey: PublicKey): ByteArray {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(peerPublicKey, true)
        return keyAgreement.generateSecret()
    }

    /**
     * Derives a 256-bit AES session key using SHA-256 over shared secret and session salt.
     */
    fun deriveSessionKey(sharedSecret: ByteArray, sessionSalt: ByteArray = ByteArray(0)): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(sharedSecret)
        if (sessionSalt.isNotEmpty()) {
            digest.update(sessionSalt)
        }
        val keyBytes = digest.digest()
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM authenticated encryption.
     */
    fun encrypt(plaintext: ByteArray, secretKey: SecretKeySpec): EncryptedResult {
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance(AES_GCM_ALGO)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val ciphertext = cipher.doFinal(plaintext)
        return EncryptedResult(iv, ciphertext)
    }

    /**
     * Decrypts ciphertext bytes using AES-256-GCM authenticated encryption.
     */
    fun decrypt(iv: ByteArray, ciphertext: ByteArray, secretKey: SecretKeySpec): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM_ALGO)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    /**
     * Converts a PublicKey to a Base64 X.509 encoded string.
     */
    fun publicKeyToBase64(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Parses a Base64 string back into an EC PublicKey.
     */
    fun base64ToPublicKey(base64: String): PublicKey {
        val keyBytes = Base64.decode(base64, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * Computes a SHA-256 fingerprint for a public key.
     */
    fun computeFingerprint(publicKeyBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(publicKeyBytes)
        val hex = hash.take(16).joinToString("") { "%02X".format(it) }
        return hex.chunked(4).joinToString("-")
    }

    /**
     * Computes a 30-digit Safety Number (Signal-style) derived from both public keys.
     * Peers compare this number to verify the connection is authentic and unintercepted.
     */
    fun computeSafetyNumber(myPubKey: ByteArray, peerPubKey: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-512")
        // Canonical ordering of keys to ensure both peers get the exact same number
        val comparison = compareByteArrays(myPubKey, peerPubKey)
        if (comparison < 0) {
            digest.update(myPubKey)
            digest.update(peerPubKey)
        } else {
            digest.update(peerPubKey)
            digest.update(myPubKey)
        }
        val hash = digest.digest()

        // Extract 6 blocks of 5 decimal digits
        val blocks = mutableListOf<String>()
        for (i in 0 until 6) {
            val offset = i * 4
            val value = ((hash[offset].toInt() and 0xFF) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)
            val positiveVal = Math.abs(value.toLong()) % 100000
            blocks.add("%05d".format(positiveVal))
        }
        return blocks.joinToString(" ")
    }

    private fun compareByteArrays(a: ByteArray, b: ByteArray): Int {
        val len = minOf(a.size, b.size)
        for (i in 0 until len) {
            val diff = (a[i].toInt() and 0xFF) - (b[i].toInt() and 0xFF)
            if (diff != 0) return diff
        }
        return a.size - b.size
    }
}
