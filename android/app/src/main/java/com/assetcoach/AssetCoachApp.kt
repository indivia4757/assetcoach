package com.assetcoach

import android.app.Application
import com.assetcoach.data.db.AssetCoachDatabase
import com.assetcoach.data.repo.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 앱 단 DI 컨테이너 (Hilt 없이 manual).
 *
 * Phase 2: DB + Repository
 * Phase 3: Gemma 4 추론 매니저, 캐시 매니저 추가
 * Phase 4: SMS 파서, WorkManager 스케줄
 */
class AssetCoachApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AssetCoachDatabase by lazy { AssetCoachDatabase.get(this) }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao(), database.categoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        // 첫 실행 시 sample CSV 자동 import
        appScope.launch {
            transactionRepository.importSampleIfEmpty(this@AssetCoachApp)
        }
    }
}
