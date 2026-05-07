package com.assetcoach

import android.app.Application
import com.assetcoach.data.db.AssetCoachDatabase
import com.assetcoach.data.repo.TransactionRepository
import com.assetcoach.security.AppLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase

/**
 * 앱 단 DI 컨테이너 (Hilt 없이 manual).
 */
class AssetCoachApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val lockManager: AppLockManager = AppLockManager()

    val database: AssetCoachDatabase by lazy { AssetCoachDatabase.get(this) }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao(), database.categoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        // SQLCipher native lib 로드 — DB 빌드 전 1회 필수
        SQLiteDatabase.loadLibs(this)

        // 첫 실행 시 sample CSV 자동 import — DB 가 비어있을 때만
        appScope.launch {
            transactionRepository.importSampleIfEmpty(this@AssetCoachApp)
        }
    }
}
