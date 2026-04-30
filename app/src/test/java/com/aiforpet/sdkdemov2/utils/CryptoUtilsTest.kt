package com.aiforpet.sdkdemov2.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoUtilsTest {

    @Test
    fun testEncryptionAndDecryption() {
        val originalText = """
            {
                "service_version": 4,
                "service_region": "KR",
                "service_host": "https://api.sdk.sandbox.ai4pet.co.kr",
                "clientId": "aiforpet-business",
                "clientKeyId": "TEST_KEY_ID"
            }
        """.trimIndent()

        val encryptedText = CryptoUtils.encrypt(originalText)
        val decryptedText = CryptoUtils.decrypt(encryptedText)

        assertEquals(originalText, decryptedText)
    }
}
