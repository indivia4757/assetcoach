package com.assetcoach.data.db

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * SQLCipher 패스프레이즈 관리.
 *
 * 정책:
 *  - 첫 실행 시 32 byte 랜덤 패스프레이즈 생성 (SecureRandom)
 *  - EncryptedSharedPreferences (Android Keystore 백업) 에 보관
 *  - 이후 실행에서는 동일 패스프레이즈 재사용
 *
 * Phase 2.5: EncryptedSharedPreferences 사용 (security-crypto)
 * Phase 2.6: BiometricPrompt 결합 — 생체 인증 후에만 패스프레이즈 release
 */
class DatabaseKeyManager(private val context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getOrCreatePassphrase(): ByteArray {
        val stored = prefs.getString(KEY_PASSPHRASE, null)
        if (stored != null) {
            return Base64.decode(stored, Base64.NO_WRAP)
        }
        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val b64 = Base64.encodeToString(random, Base64.NO_WRAP)
        prefs.edit().putString(KEY_PASSPHRASE, b64).apply()
        return random
    }

    companion object {
        private const val PREFS_NAME = "ac_secure_prefs"
        private const val KEY_PASSPHRASE = "db_passphrase_b64"
    }
}
