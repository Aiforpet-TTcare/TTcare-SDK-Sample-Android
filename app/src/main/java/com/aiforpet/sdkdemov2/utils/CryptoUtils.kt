package com.aiforpet.sdkdemov2.utils

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    // AES-256 Key (32 bytes)
    private val KEY = byteArrayOf(
        0x5A, 0x1B, 0x4E, 0x72, 0x11, 0x3F, 0x2A, 0x8C.toByte(),
        0x19, 0xB3.toByte(), 0xDF.toByte(), 0x5C, 0x87.toByte(), 0x31, 0xA2.toByte(), 0xF1.toByte(),
        0x6C, 0x4D, 0x90.toByte(), 0x23, 0x5E, 0x8B.toByte(), 0x1A, 0x76,
        0xC4.toByte(), 0x9F.toByte(), 0x33, 0x68, 0x15, 0x7D, 0x2B, 0x9A.toByte()
    )

    // AES IV (16 bytes)
    private val IV = byteArrayOf(
        0x10, 0x21, 0x32, 0x43, 0x54, 0x65, 0x76, 0x87.toByte(),
        0x98.toByte(), 0xA9.toByte(), 0xBA.toByte(), 0xCB.toByte(), 0xDC.toByte(), 0xED.toByte(), 0xFE.toByte(), 0x0F
    )

    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val ALGORITHM = "AES"

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(KEY, ALGORITHM)
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(encryptedText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(KEY, ALGORITHM)
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val decoded = Base64.getDecoder().decode(encryptedText)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted, Charsets.UTF_8)
    }
}
