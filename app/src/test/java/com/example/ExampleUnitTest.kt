package com.example

import com.example.crypto.CryptoManager
import com.example.model.PeerHandshakePayload
import com.example.protocol.ShortCodeEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testEcKeyAgreementAndAesGcm() {
        // Generate keys for Alice and Bob
        val aliceKeyPair = CryptoManager.generateKeyPair()
        val bobKeyPair = CryptoManager.generateKeyPair()

        // Derive ECDH shared secrets
        val aliceSharedSecret = CryptoManager.deriveSharedSecret(aliceKeyPair.private, bobKeyPair.public)
        val bobSharedSecret = CryptoManager.deriveSharedSecret(bobKeyPair.private, aliceKeyPair.public)

        // Verify both compute identical shared secrets
        assertTrue("ECDH shared secrets must match", aliceSharedSecret.contentEquals(bobSharedSecret))

        // Derive AES session keys
        val aliceKey = CryptoManager.deriveSessionKey(aliceSharedSecret)
        val bobKey = CryptoManager.deriveSessionKey(bobSharedSecret)

        // Encrypt message from Alice to Bob
        val originalText = "Hello Bob! Top secret P2P message."
        val encrypted = CryptoManager.encrypt(originalText.toByteArray(Charsets.UTF_8), aliceKey)

        // Verify ciphertext is non-empty and IV is 12 bytes
        assertEquals(12, encrypted.iv.size)
        assertTrue(encrypted.ciphertext.isNotEmpty())

        // Decrypt on Bob's side
        val decryptedBytes = CryptoManager.decrypt(encrypted.iv, encrypted.ciphertext, bobKey)
        val decryptedText = String(decryptedBytes, Charsets.UTF_8)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun testAesGcmTamperResistance() {
        val keyPairA = CryptoManager.generateKeyPair()
        val keyPairB = CryptoManager.generateKeyPair()
        val sharedSecret = CryptoManager.deriveSharedSecret(keyPairA.private, keyPairB.public)
        val key = CryptoManager.deriveSessionKey(sharedSecret)

        val plaintext = "Tamper proof test message"
        val encrypted = CryptoManager.encrypt(plaintext.toByteArray(Charsets.UTF_8), key)

        // Tamper with ciphertext by flipping a byte
        val tamperedCiphertext = encrypted.ciphertext.clone()
        tamperedCiphertext[0] = (tamperedCiphertext[0].toInt() xor 0xFF).toByte()

        var caughtException = false
        try {
            CryptoManager.decrypt(encrypted.iv, tamperedCiphertext, key)
        } catch (e: Exception) {
            caughtException = true
        }
        assertTrue("Decryption must fail when ciphertext is modified", caughtException)
    }

    @Test
    fun testSafetyNumberComputation() {
        val alice = CryptoManager.generateKeyPair()
        val bob = CryptoManager.generateKeyPair()

        val safetyAlice = CryptoManager.computeSafetyNumber(alice.public.encoded, bob.public.encoded)
        val safetyBob = CryptoManager.computeSafetyNumber(bob.public.encoded, alice.public.encoded)

        assertEquals("Safety numbers must be identically computed on both sides", safetyAlice, safetyBob)
        // 6 blocks of 5 digits separated by spaces = 35 chars
        assertEquals(35, safetyAlice.length)
    }

    @Test
    fun testShortCodeGenerationAndAlgorithm() = runBlocking {
        // Test short code generator generates valid 5-char Crockford Base32
        val code = ShortCodeEngine.generateShortCode()
        assertEquals(5, code.length)
        assertTrue(code.matches(Regex("^[2-9A-Z]{5}$")))

        // Test shortening long handshake payload (300+ chars)
        val longPublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7Z5f8cE1oK9w2A5bQmN0h4P9rT1lV8xG3kL7pQ0sR5aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890abcdef=="
        val payload = PeerHandshakePayload(
            shortCode = "",
            peerName = "TestPeer",
            publicKeyBase64 = longPublicKey,
            lanIp = "192.168.1.10",
            port = 8988,
            sessionNonce = "NONCE_TEST_1234",
            fingerprint = "8E41-C290-7F11-4B9A"
        )

        // Execute shortening algorithm
        val generatedCode = ShortCodeEngine.shortenAndRegister(payload)
        assertEquals(5, generatedCode.length)

        // Execute retrieval algorithm
        val retrieved = ShortCodeEngine.retrievePayload(generatedCode)
        assertNotNull(retrieved)
        assertEquals(longPublicKey, retrieved?.publicKeyBase64)
        assertEquals("TestPeer", retrieved?.peerName)

        // Test breakdown metrics
        val breakdown = ShortCodeEngine.getAlgorithmBreakdown(payload.copy(shortCode = generatedCode))
        assertTrue(breakdown.rawLengthChars > 200)
        assertEquals(5, breakdown.shortCodeLength)
        assertTrue(breakdown.compressionRatio.endsWith("%"))
    }
}
