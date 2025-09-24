package com.moneyweather.util.encryption

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object Aes256Crypto {
    // 🔐 암호화/복호화에 사용할 256비트(32바이트) 비밀키
    private const val SECRET_KEY = "DonseeSecretKeyForAES256Using!!!" // 정확히 32자리여야 함

    // 🧊 CBC 모드에서 사용하는 128비트(16바이트) 초기화 벡터 (IV)
    private const val INIT_VECTOR = "DonseeInitVector" // 정확히 16자리여야 함

    // 🔧 사용할 암호화 방식 (AES + CBC 모드 + PKCS5Padding 패딩)
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"

    private const val KEY_FACTORY_ALGORITHM = "PBKDF2withHmacSHA1"
    private const val KEY_SPEC_ALGORITHM = "AES"
    private const val KEY_SIZE = 256
    private const val ITERATION_COUNT = 65536

    // key 생성을 위한 salt (고정값보다 무작위로 관리하는 게 좋음)
    private val SALT = "DonseeSaltString".toByteArray(Charsets.UTF_8)

    /**
     * 문자열에서 AES 키를 생성
     */
    private fun generateKey(key: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        val spec = PBEKeySpec(key.toCharArray(), SALT, ITERATION_COUNT, KEY_SIZE)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, KEY_SPEC_ALGORITHM)
    }

    /**
     * 평문(plain text)을 AES-256 방식으로 암호화하여 Base64 문자열로 반환
     * @param plainText 평문 입력 문자열
     * @return Base64 인코딩된 암호문
     */
    @Throws(Exception::class)
    fun encrypt(plainText: String): String {
        // IV를 UTF-8 바이트 배열로 변환하여 초기화 벡터 생성
        val iv = IvParameterSpec(INIT_VECTOR.toByteArray(StandardCharsets.UTF_8))

        // 비밀키를 UTF-8 바이트 배열로 변환하여 AES용 키 스펙 생성
        val keySpec = generateKey(SECRET_KEY)

        // Cipher 객체를 AES/CBC/PKCS5Padding 알고리즘으로 초기화
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // 암호화 모드로 설정하고, 키와 IV로 초기화
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

        // 입력된 평문을 바이트 배열로 암호화
        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        // 암호화된 결과를 Base64 인코딩하여 문자열로 반환
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    /**
     * AES-256 방식으로 암호화된 Base64 문자열을 복호화하여 평문으로 반환
     * @param encryptedText Base64 인코딩된 암호문
     * @return 복호화된 평문 문자열
     */
    @Throws(Exception::class)
    fun decrypt(encryptedText: String?): String {
        // IV를 UTF-8 바이트 배열로 변환하여 초기화 벡터 생성
        val iv = IvParameterSpec(INIT_VECTOR.toByteArray(StandardCharsets.UTF_8))

        // 비밀키를 UTF-8 바이트 배열로 변환하여 AES용 키 스펙 생성
        val keySpec = generateKey(SECRET_KEY)

        // Cipher 객체를 AES/CBC/PKCS5Padding 알고리즘으로 초기화
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // 복호화 모드로 설정하고, 키와 IV로 초기화
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)

        // 암호문(Base64 인코딩 문자열)을 디코딩하여 바이트 배열로 변환
        val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)

        // 암호문 바이트 배열을 복호화
        val decrypted = cipher.doFinal(decodedBytes)

        // 복호화된 결과를 UTF-8 문자열로 변환하여 반환
        return String(decrypted, StandardCharsets.UTF_8)
    }
}